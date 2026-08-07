package io.hxneyw.repo.content.blocks.combustionbelt;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface CombustionBeltAccessor {

    boolean sulfuricresonance$isCombustionBelt();

    void sulfuricresonance$setCombustionBelt(boolean combustionBelt);

    boolean sulfuricresonance$isThermochemicalPulley();

    void sulfuricresonance$setThermochemicalPulley(
            boolean thermochemicalPulley
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
}
