package io.hxneyw.repo.mixin;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        value = BeltBlockEntity.class,
        remap = false
)
public abstract class BeltBlockEntityMixin
        implements CombustionBeltAccessor {

    @Unique
    private static final String
            SULFURICRESONANCE_COMBUSTION_BELT_KEY =
            "SulfuricResonanceCombustionBelt";

    @Unique
    private boolean sulfuricresonance$combustionBelt;

    @Override
    public boolean sulfuricresonance$isCombustionBelt() {
        return sulfuricresonance$combustionBelt;
    }

    @Override
    public void sulfuricresonance$setCombustionBelt(
            boolean combustionBelt
    ) {
        sulfuricresonance$combustionBelt = combustionBelt;
    }

    @Inject(
            method = "write",
            at = @At("TAIL"),
            remap = false
    )
    private void sulfuricresonance$writeCombustionBelt(
            CompoundTag compound,
            HolderLookup.Provider registries,
            boolean clientPacket,
            CallbackInfo callback
    ) {
        compound.putBoolean(
                SULFURICRESONANCE_COMBUSTION_BELT_KEY,
                sulfuricresonance$combustionBelt
        );
    }

    @Inject(
            method = "read",
            at = @At("TAIL"),
            remap = false
    )
    private void sulfuricresonance$readCombustionBelt(
            CompoundTag compound,
            HolderLookup.Provider registries,
            boolean clientPacket,
            CallbackInfo callback
    ) {
        sulfuricresonance$combustionBelt =
                compound.getBoolean(
                        SULFURICRESONANCE_COMBUSTION_BELT_KEY
                );
    }
}