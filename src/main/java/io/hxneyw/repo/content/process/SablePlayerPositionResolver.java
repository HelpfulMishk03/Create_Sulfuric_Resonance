package io.hxneyw.repo.content.process;

import java.lang.reflect.InvocationTargetException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** Resolves a server player's position into the block view used by Sable sublevels. */
public final class SablePlayerPositionResolver {
    private SablePlayerPositionResolver() { }

    public static Context resolve(ServerPlayer player) {
        LevelAccessor parent = player.serverLevel();
        BlockPos parentPosition = BlockPos.containing(
                player.getX(), player.getY() + 0.01D, player.getZ());
        try {
            ClassLoader loader = SablePlayerPositionResolver.class.getClassLoader();
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable", false, loader);
            Object helper = sableClass.getField("HELPER").get(null);
            Object subLevel = helper.getClass()
                    .getMethod("getTrackingOrVehicleSubLevel", net.minecraft.world.entity.Entity.class)
                    .invoke(helper, player);
            if (subLevel == null) return new Context(parent, parentPosition, player.position(), false);

            Object pose = subLevel.getClass().getMethod("logicalPose").invoke(subLevel);
            Vector3d storedPosition = new Vector3d(
                    player.getX(), player.getY() + 0.01D, player.getZ());
            pose.getClass().getMethod("transformPositionInverse", Vector3d.class)
                    .invoke(pose, storedPosition);

            BlockPos plotPosition = BlockPos.containing(storedPosition.x, storedPosition.y, storedPosition.z);
            Object plot = subLevel.getClass().getMethod("getPlot").invoke(subLevel);
            BlockPos center = (BlockPos) plot.getClass().getMethod("getCenterBlock").invoke(plot);
            Object embeddedLevel = plot.getClass().getMethod("getEmbeddedLevelAccessor").invoke(plot);
            if (embeddedLevel instanceof LevelAccessor accessor) {
                return new Context(accessor, plotPosition.subtract(center),
                        new Vec3(storedPosition.x - center.getX(),
                                storedPosition.y - center.getY(),
                                storedPosition.z - center.getZ()), true);
            }
        } catch (ClassNotFoundException unavailable) {
            // Sable is an optional integration.
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                 | NoSuchFieldException | LinkageError | ClassCastException failure) {
            io.hxneyw.repo.CreateSulfuricResonance.LOGGER.debug(
                    "Unable to resolve player position inside Sable sublevel", failure);
        }
        return new Context(parent, parentPosition, player.position(), false);
    }

    public record Context(LevelAccessor level, BlockPos blockPosition, Vec3 precisePosition,
                          boolean inSubLevel) { }
}
