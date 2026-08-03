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
            SULFURICRESONANCE$COMBUSTION_BELT_KEY =
            "combustionBelt";

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
            CallbackInfo ci
    ) {
        compound.putBoolean(
                SULFURICRESONANCE$COMBUSTION_BELT_KEY,
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
            CallbackInfo ci
    ) {
        sulfuricresonance$combustionBelt =
                compound.getBoolean(
                        SULFURICRESONANCE$COMBUSTION_BELT_KEY
                );
    }
}