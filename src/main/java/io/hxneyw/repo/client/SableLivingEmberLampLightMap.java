package io.hxneyw.repo.client;

import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlock;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlockEntity;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Projects mobile light into the world and into each nearby Sable lighting scene. */
public final class SableLivingEmberLampLightMap {
    private static final boolean DIAGNOSTICS = Boolean.getBoolean("csr.lightingDiagnostics")
            || Boolean.getBoolean("csr.lightingAudit");
    private static ClientLevel currentLevel;
    private static Map<Long, Integer> emissions = Map.of();
    private static Set<Long> sourceSections = Set.of();
    private static SableApi sable;
    private static boolean apiChecked;
    private static boolean reportedResolutionFailure;

    private SableLivingEmberLampLightMap() { }

    public static void tick(ClientLevel level) {
        if (currentLevel != level) {
            clearCurrentLevel();
            currentLevel = level;
        }
        if (level == null) return;

        Map<Long, Integer> next = new HashMap<>();
        try {
            SableApi api = api();
            if (api != null) {
                List<Scene> scenes = api.scenes(level);
                for (LivingEmberLampBlockEntity lamp : LivingEmberLampBlockEntity.getLoadedClientLamps()) {
                    if (lamp.isRemoved() || lamp.getLevel() != level) continue;
                    var state = lamp.getBlockState();
                    if (!state.hasProperty(LivingEmberLampBlock.LIGHT_LEVEL)) continue;
                    int light = state.getValue(LivingEmberLampBlock.LIGHT_LEVEL);
                    if (light <= 0) continue;
                    Object owner = api.containing.invoke(api.helper, lamp);
                    if (owner == null) continue; // Placed lamps already use vanilla lighting.

                    Object sourcePose = api.renderPose.invoke(owner);
                    // Pose3dc already subtracts rotationPoint, which is in plot-grid coordinates.
                    Vec3 world = api.project(sourcePose, Vec3.atCenterOf(lamp.getBlockPos()), false);
                    addSource(level, next, BlockPos.containing(world), light, false);
                    int recipients = 0;
                    for (Scene scene : scenes) {
                        if (!scene.bounds.inflate(light).contains(world)) continue;
                        Vec3 receiver = api.project(scene.pose, world, true);
                        // Separate sublevels have separate, distant block-light grids. A
                        // source in the parent world cannot light another ship's geometry.
                        addSource(level, next, BlockPos.containing(receiver), light, true);
                        recipients++;
                    }
                    if (DIAGNOSTICS && level.getGameTime() % 100 == 0) {
                        io.hxneyw.repo.CreateSulfuricResonance.LOGGER.info(
                                "CSR_LAMP_ROUTE stored={} world={} light={} recipientScenes={}",
                                lamp.getBlockPos(), world, light, recipients);
                    }
                }
            }
        } catch (ReflectiveOperationException | LinkageError | ClassCastException incompatible) {
            if (!reportedResolutionFailure) {
                reportedResolutionFailure = true;
                io.hxneyw.repo.CreateSulfuricResonance.LOGGER.error(
                        "Cannot project Sable lamp lighting scenes", incompatible);
            }
        }

        Map<Long, Integer> previous = emissions;
        emissions = Map.copyOf(next);
        Set<Long> nextSections = new HashSet<>();
        for (long packed : next.keySet()) {
            BlockPos position = BlockPos.of(packed);
            long section = SectionPos.of(position).asLong();
            nextSections.add(section);
            if (!sourceSections.contains(section)) {
                level.getLightEngine().getLayerListener(LightLayer.BLOCK)
                        .updateSectionStatus(SectionPos.of(section), false);
            }
            level.getLightEngine().setLightEnabled(new ChunkPos(position), true);
        }
        for (long section : sourceSections) {
            if (!nextSections.contains(section)) restoreSection(level, section);
        }
        sourceSections = Set.copyOf(nextSections);
        Set<Long> changed = new HashSet<>(previous.keySet());
        changed.addAll(next.keySet());
        boolean refresh = false;
        for (long packed : changed) {
            if (!next.getOrDefault(packed, 0).equals(previous.getOrDefault(packed, 0))) {
                level.getLightEngine().checkBlock(BlockPos.of(packed));
                refresh = true;
            }
        }
        if (refresh) level.getLightEngine().runLightUpdates();
        if (DIAGNOSTICS && level.getGameTime() % 100 == 0 && !next.isEmpty()) {
            var blockLight = level.getLightEngine().getLayerListener(LightLayer.BLOCK);
            for (var source : next.entrySet()) {
                BlockPos position = BlockPos.of(source.getKey());
                int neighbor = 0;
                for (var direction : net.minecraft.core.Direction.values()) {
                    neighbor = Math.max(neighbor, blockLight.getLightValue(position.relative(direction)));
                }
                io.hxneyw.repo.CreateSulfuricResonance.LOGGER.info(
                        "CSR_LAMP_CLIENT source={} expected={} propagated={} neighbor={}",
                        position, source.getValue(), blockLight.getLightValue(position), neighbor);
            }
        }
    }

    private static void addSource(ClientLevel level, Map<Long, Integer> next,
                                  BlockPos source, int light, boolean plot) {
        if (source.getY() < level.getMinBuildHeight() || source.getY() >= level.getMaxBuildHeight()) return;
        int chunkX = source.getX() >> 4;
        int chunkZ = source.getZ() >> 4;
        if (level.getChunkSource().getChunk(chunkX, chunkZ, false) != null) {
            next.merge(source.asLong(), light, Math::max);
            return;
        }
        if (!plot) return;

        // Unallocated plot chunks contain no geometry. Put an attenuated
        // source just outside each loaded neighbor's boundary so light can
        // enter it without creating chunks or shining through solid blocks.
        for (int x = chunkX - 1; x <= chunkX + 1; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                if (level.getChunkSource().getChunk(x, z, false) == null) continue;
                int minX = x << 4;
                int minZ = z << 4;
                int edgeX = Math.clamp(source.getX(), minX, minX + 15);
                int edgeZ = Math.clamp(source.getZ(), minZ, minZ + 15);
                if (edgeX != source.getX()) {
                    addBoundary(next, source, edgeX + Integer.signum(source.getX() - edgeX), edgeZ, light);
                }
                if (edgeZ != source.getZ()) {
                    addBoundary(next, source, edgeX, edgeZ + Integer.signum(source.getZ() - edgeZ), light);
                }
            }
        }
    }

    private static void addBoundary(Map<Long, Integer> next, BlockPos source, int x, int z, int light) {
        int attenuated = light - Math.abs(source.getX() - x) - Math.abs(source.getZ() - z);
        if (attenuated > 0) next.merge(new BlockPos(x, source.getY(), z).asLong(), attenuated, Math::max);
    }

    public static int emissionAt(BlockLightEngine engine, long position) {
        ClientLevel level = currentLevel;
        // Client mixins also run on the integrated server. Never add a
        // temporary client emitter to server lighting or saved light data.
        if (level == null || level.getLightEngine().getLayerListener(LightLayer.BLOCK) != engine) return 0;
        return emissions.getOrDefault(position, 0);
    }

    private static void restoreSection(ClientLevel level, long packed) {
        SectionPos section = SectionPos.of(packed);
        var chunk = level.getChunkSource().getChunk(section.x(), section.z(), false);
        if (chunk == null) return;
        int index = level.getSectionIndexFromSectionY(section.y());
        if (index < 0 || index >= chunk.getSectionsCount()) return;
        level.getLightEngine().getLayerListener(LightLayer.BLOCK)
                .updateSectionStatus(section, chunk.getSection(index).hasOnlyAir());
    }

    private static void clearCurrentLevel() {
        ClientLevel old = currentLevel;
        Set<Long> oldSources = emissions.keySet();
        emissions = Map.of();
        currentLevel = null;
        if (old != null) {
            for (long packed : oldSources) old.getLightEngine().checkBlock(BlockPos.of(packed));
            for (long section : sourceSections) restoreSection(old, section);
            old.getLightEngine().runLightUpdates();
        }
        sourceSections = Set.of();
        reportedResolutionFailure = false;
    }

    private static SableApi api() throws ReflectiveOperationException {
        if (!apiChecked) {
            apiChecked = true;
            try {
                sable = new SableApi();
            } catch (ClassNotFoundException absent) {
                sable = null;
            }
        }
        return sable;
    }

    private record Scene(Object pose, AABB bounds) { }

    /** Keep optional Sable classes out of this class's signatures. */
    private static final class SableApi {
        private final Object helper;
        private final Method containing;
        private final Method container;
        private final Method allScenes;
        private final Method renderPose;
        private final Method bounds;
        private final Method transform;
        private final Method inverse;
        private final Method[] boxCoordinates;

        private SableApi() throws ReflectiveOperationException {
            ClassLoader loader = SableLivingEmberLampLightMap.class.getClassLoader();
            helper = Class.forName("dev.ryanhcode.sable.Sable", false, loader).getField("HELPER").get(null);
            containing = helper.getClass().getMethod("getContainingClient", BlockEntity.class);
            Class<?> containers = Class.forName("dev.ryanhcode.sable.api.sublevel.SubLevelContainer", false, loader);
            container = containers.getMethod("getContainer", ClientLevel.class);
            allScenes = containers.getMethod("getAllSubLevels");
            Class<?> scene = Class.forName("dev.ryanhcode.sable.sublevel.ClientSubLevel", false, loader);
            renderPose = scene.getMethod("renderPose");
            bounds = scene.getMethod("boundingBox");
            Class<?> pose = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc", false, loader);
            transform = pose.getMethod("transformPosition", Vector3dc.class, Vector3d.class);
            inverse = pose.getMethod("transformPositionInverse", Vector3dc.class, Vector3d.class);
            Class<?> box = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3dc", false, loader);
            boxCoordinates = new Method[6];
            String[] names = {"minX", "minY", "minZ", "maxX", "maxY", "maxZ"};
            for (int i = 0; i < names.length; i++) boxCoordinates[i] = box.getMethod(names[i]);
        }

        private Vec3 project(Object pose, Vec3 position, boolean toPlot) throws ReflectiveOperationException {
            Vector3d result = new Vector3d();
            (toPlot ? inverse : transform).invoke(pose,
                    new Vector3d(position.x, position.y, position.z), result);
            return new Vec3(result.x, result.y, result.z);
        }

        private List<Scene> scenes(ClientLevel level) throws ReflectiveOperationException {
            Object owner = container.invoke(null, level);
            if (owner == null) return List.of();
            List<Scene> scenes = new ArrayList<>();
            for (Object scene : (Iterable<?>) allScenes.invoke(owner)) {
                Object box = bounds.invoke(scene);
                double[] b = new double[6];
                for (int i = 0; i < b.length; i++) b[i] = ((Number) boxCoordinates[i].invoke(box)).doubleValue();
                scenes.add(new Scene(renderPose.invoke(scene), new AABB(b[0], b[1], b[2], b[3], b[4], b[5])));
            }
            return scenes;
        }
    }
}
