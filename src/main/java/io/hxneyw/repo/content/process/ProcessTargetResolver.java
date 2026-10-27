package io.hxneyw.repo.content.process;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ProcessTargetResolver {
    private ProcessTargetResolver() {
    }

    public static @NotNull Resolution resolve(
            @NotNull Level observerLevel,
            @NotNull ProcessTargetRef target
    ) {
        if (observerLevel.isClientSide) {
            if (!observerLevel.dimension().location().toString()
                    .equals(target.dimension())) {
                return Resolution.unavailable();
            }

            if (!observerLevel.isLoaded(target.position())) {
                return Resolution.unavailable();
            }

            return resolveLoadedEntity(
                    observerLevel.getBlockEntity(target.position()),
                    target
            );
        }

        if (observerLevel.dimension().location().toString()
                .equals(target.dimension())
                && observerLevel.isLoaded(target.position())) {
            Resolution local = resolveLoadedEntity(
                    observerLevel.getBlockEntity(target.position()),
                    target
            );
            if (local.provider() != null) {
                return local;
            }
        }
        if (observerLevel.dimension().location().toString()
                .equals(target.dimension())) {
            Resolution plotResolution = resolveLoadedEntity(
                    SablePlotBlockEntityLookup.find(observerLevel, target.position()),
                    target
            );
            if (plotResolution.provider() != null) {
                return plotResolution;
            }
        }

        ServerLevel serverLevel = observerLevel instanceof ServerLevel directLevel
                ? directLevel
                : observerLevel instanceof ServerLevelAccessor accessor
                ? accessor.getLevel()
                : null;
        if (serverLevel == null) {
            return Resolution.invalid();
        }

        ResourceLocation dimensionId = ResourceLocation.tryParse(
                target.dimension()
        );
        if (dimensionId == null) {
            return Resolution.invalid();
        }

        ResourceKey<Level> dimensionKey = ResourceKey.create(
                Registries.DIMENSION,
                dimensionId
        );
        ServerLevel targetLevel = serverLevel.getServer().getLevel(dimensionKey);
        if (targetLevel == null) {
            return Resolution.invalid();
        }

        if (!targetLevel.isLoaded(target.position())) {
            return Resolution.unavailable();
        }

        return resolveLoadedEntity(
                targetLevel.getBlockEntity(target.position()),
                target
        );
    }

    private static @NotNull Resolution resolveLoadedEntity(
            @Nullable BlockEntity blockEntity,
            @NotNull ProcessTargetRef target
    ) {
        if (!(blockEntity instanceof IProcessStateProvider provider)) {
            return Resolution.invalid();
        }

        if (!target.identity().equals(provider.getProcessIdentity())) {
            return Resolution.invalid();
        }

        return Resolution.resolved(provider);
    }

    public enum Status {
        RESOLVED,
        UNAVAILABLE,
        INVALID
    }

    public record Resolution(
            @NotNull Status status,
            @Nullable IProcessStateProvider provider
    ) {
        public static @NotNull Resolution resolved(
                @NotNull IProcessStateProvider provider
        ) {
            return new Resolution(Status.RESOLVED, provider);
        }

        public static @NotNull Resolution unavailable() {
            return new Resolution(Status.UNAVAILABLE, null);
        }

        public static @NotNull Resolution invalid() {
            return new Resolution(Status.INVALID, null);
        }

    }
}
