package io.hxneyw.repo.content.network;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ThermochemicalNetworkResolver {

    private ThermochemicalNetworkResolver() {
    }

    public static @NotNull Resolution resolve(
            @NotNull Level observerLevel,
            @NotNull ThermalRelaySwitchItem.FurnaceLink link
    ) {
        return resolve(
                observerLevel,
                link.position(),
                link.dimension(),
                link.furnaceIdentity()
        );
    }

    public static @NotNull Resolution resolve(
            @NotNull Level observerLevel,
            @NotNull BlockPos position,
            @NotNull String dimension,
            @NotNull UUID furnaceIdentity
    ) {
        if (observerLevel.isClientSide) {
            if (!observerLevel.dimension().location().toString().equals(dimension)) {
                return Resolution.remote();
            }

            if (!observerLevel.isLoaded(position)) {
                return Resolution.unloaded();
            }

            if (observerLevel.getBlockEntity(position) instanceof MoltenRotorBlockEntity furnace
                    && furnaceIdentity.equals(furnace.getFurnaceIdentity())) {
                return Resolution.resolved(furnace);
            }

            return Resolution.invalid();
        }

        if (!(observerLevel instanceof ServerLevel serverLevel)) {
            return Resolution.invalid();
        }

        ResourceLocation dimensionId = ResourceLocation.tryParse(dimension);
        if (dimensionId == null) {
            return Resolution.invalid();
        }

        ResourceKey<Level> dimensionKey = ResourceKey.create(
                Registries.DIMENSION,
                dimensionId
        );

        ServerLevel sourceLevel = serverLevel.getServer().getLevel(dimensionKey);
        if (sourceLevel == null) {
            return Resolution.invalid();
        }

        if (!sourceLevel.isLoaded(position)) {
            return Resolution.unloaded();
        }

        if (sourceLevel.getBlockEntity(position) instanceof MoltenRotorBlockEntity furnace
                && furnaceIdentity.equals(furnace.getFurnaceIdentity())) {
            return Resolution.resolved(furnace);
        }

        return Resolution.invalid();
    }

    public enum Status {
        RESOLVED,
        UNLOADED,
        REMOTE_CLIENT,
        INVALID
    }

    public record Resolution(
            @NotNull Status status,
            @Nullable MoltenRotorBlockEntity furnace
    ) {
        public static Resolution resolved(MoltenRotorBlockEntity furnace) {
            return new Resolution(Status.RESOLVED, furnace);
        }

        public static Resolution unloaded() {
            return new Resolution(Status.UNLOADED, null);
        }

        public static Resolution remote() {
            return new Resolution(Status.REMOTE_CLIENT, null);
        }

        public static Resolution invalid() {
            return new Resolution(Status.INVALID, null);
        }

        public boolean isResolved() {
            return status == Status.RESOLVED && furnace != null;
        }

        public boolean isLinkedButUnavailable() {
            return status == Status.UNLOADED || status == Status.REMOTE_CLIENT;
        }
    }
}
