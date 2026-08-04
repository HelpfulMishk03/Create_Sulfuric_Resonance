package io.hxneyw.repo.content.blocks.combustionbelt;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Server/client state exposed by the BeltBlockEntity mixin.
 */
public interface CombustionBeltAccessor {

    boolean sulfuricresonance$isCombustionBelt();

    void sulfuricresonance$setCombustionBelt(
            boolean combustionBelt
    );

    MoltenRotorBlockEntity.RotorHeatLevel
    sulfuricresonance$getReceivedHeatTier();

    void sulfuricresonance$setReceivedHeatTier(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier
    );

    @Nullable
    BlockPos sulfuricresonance$getHeatSourcePos();

    void sulfuricresonance$setHeatSourcePos(
            @Nullable BlockPos sourcePosition
    );

    boolean sulfuricresonance$isHeatFromConduit();

    void sulfuricresonance$setHeatFromConduit(
            boolean heatFromConduit
    );

    default boolean sulfuricresonance$hasValidHeatSource() {
        return sulfuricresonance$isCombustionBelt()
                && sulfuricresonance$getReceivedHeatTier()
                != MoltenRotorBlockEntity.RotorHeatLevel.NONE
                && sulfuricresonance$getHeatSourcePos() != null;
    }
}
