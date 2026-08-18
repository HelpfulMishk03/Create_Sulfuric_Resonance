package io.hxneyw.repo.content.process;

import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ProcessMonitorResolver {
    private ProcessMonitorResolver() {
    }

    public static @NotNull Resolution resolve(
            @NotNull Level observerLevel,
            @NotNull ProcessMonitorRef reference
    ) {
        if (observerLevel.isClientSide) {
            if (!observerLevel.dimension().location().toString()
                    .equals(reference.dimension())) {
                return Resolution.unavailable();
            }
            if (!observerLevel.isLoaded(reference.position())) {
                return Resolution.unavailable();
            }
            return resolveLoaded(observerLevel, reference);
        }

        if (!(observerLevel instanceof ServerLevel serverLevel)) {
            return Resolution.invalid();
        }

        ResourceLocation dimensionId = ResourceLocation.tryParse(
                reference.dimension()
        );
        if (dimensionId == null) {
            return Resolution.invalid();
        }

        ResourceKey<Level> dimensionKey = ResourceKey.create(
                Registries.DIMENSION,
                dimensionId
        );
        ServerLevel monitorLevel = serverLevel.getServer().getLevel(dimensionKey);
        if (monitorLevel == null) {
            return Resolution.invalid();
        }
        if (!monitorLevel.isLoaded(reference.position())) {
            return Resolution.unavailable();
        }
        return resolveLoaded(monitorLevel, reference);
    }

    private static @NotNull Resolution resolveLoaded(
            @NotNull Level level,
            @NotNull ProcessMonitorRef reference
    ) {
        if (level.getBlockEntity(reference.position())
                instanceof ProcessMonitorBlockEntity monitor
                && reference.identity().equals(monitor.getMonitorIdentity())) {
            return Resolution.resolved(monitor);
        }
        return Resolution.invalid();
    }

    public enum Status {
        RESOLVED,
        UNAVAILABLE,
        INVALID
    }

    public record Resolution(
            @NotNull Status status,
            @Nullable ProcessMonitorBlockEntity monitor
    ) {
        public static @NotNull Resolution resolved(
                @NotNull ProcessMonitorBlockEntity monitor
        ) {
            return new Resolution(Status.RESOLVED, monitor);
        }

        public static @NotNull Resolution unavailable() {
            return new Resolution(Status.UNAVAILABLE, null);
        }

        public static @NotNull Resolution invalid() {
            return new Resolution(Status.INVALID, null);
        }

        public boolean isResolved() {
            return status == Status.RESOLVED && monitor != null;
        }
    }
}
