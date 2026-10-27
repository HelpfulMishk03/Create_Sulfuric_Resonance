package io.hxneyw.repo.content.process;

import java.lang.reflect.InvocationTargetException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

public final class SablePlotBlockEntityLookup {
    private SablePlotBlockEntityLookup() {
    }

    public static @Nullable BlockEntity find(Level level, BlockPos position) {
        ServerLevel parent = level instanceof ServerLevel direct
                ? direct
                : level instanceof ServerLevelAccessor accessor
                ? accessor.getLevel()
                : null;
        if (parent == null && level != null) {
            try {
                Object parentLevel = level.getClass().getMethod("getLevel").invoke(level);
                if (parentLevel instanceof ServerLevel serverLevel) {
                    parent = serverLevel;
                }
            } catch (NoSuchMethodException | IllegalAccessException
                     | InvocationTargetException | LinkageError ignored) {
                return null;
            }
        }
        if (parent == null) {
            return null;
        }

        try {
            ClassLoader loader = SablePlotBlockEntityLookup.class.getClassLoader();
            Class<?> containerType = Class.forName(
                    "dev.ryanhcode.sable.api.sublevel.SubLevelContainer",
                    false,
                    loader
            );
            Object container = containerType.getMethod("getContainer", ServerLevel.class)
                    .invoke(null, parent);
            if (container == null) {
                return null;
            }

            Object plot = containerType.getMethod("getPlot", ChunkPos.class)
                    .invoke(container, new ChunkPos(position));
            if (plot == null) {
                return null;
            }

            BlockPos center = (BlockPos) plot.getClass()
                    .getMethod("getCenterBlock")
                    .invoke(plot);
            Object embeddedLevel = plot.getClass()
                    .getMethod("getEmbeddedLevelAccessor")
                    .invoke(plot);
            return (BlockEntity) embeddedLevel.getClass()
                    .getMethod("getBlockEntity", BlockPos.class)
                    .invoke(embeddedLevel, position.subtract(center));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | ClassCastException
                 | LinkageError ignored) {
            return null;
        }
    }

    public static @Nullable LevelAccessor embeddedLevelAccessor(Object subLevel) {
        try {
            Object plot = subLevel.getClass().getMethod("getPlot").invoke(subLevel);
            Object embeddedLevel = plot.getClass()
                    .getMethod("getEmbeddedLevelAccessor")
                    .invoke(plot);
            return embeddedLevel instanceof LevelAccessor accessor ? accessor : null;
        } catch (NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | LinkageError ignored) {
            return null;
        }
    }

    public static @Nullable BlockPos embeddedBlockPos(Object subLevel, BlockPos parentPosition) {
        try {
            Object plot = subLevel.getClass().getMethod("getPlot").invoke(subLevel);
            BlockPos center = (BlockPos) plot.getClass()
                    .getMethod("getCenterBlock")
                    .invoke(plot);
            return parentPosition.subtract(center);
        } catch (NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | LinkageError ignored) {
            return null;
        }
    }

    public static @Nullable LevelLightEngine embeddedLightEngine(Object subLevel) {
        try {
            Object plot = subLevel.getClass().getMethod("getPlot").invoke(subLevel);
            Object engine = plot.getClass().getMethod("getLightEngine").invoke(plot);
            return engine instanceof LevelLightEngine lightEngine ? lightEngine : null;
        } catch (NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | LinkageError ignored) {
            return null;
        }
    }
}
