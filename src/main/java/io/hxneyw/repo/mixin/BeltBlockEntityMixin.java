package io.hxneyw.repo.mixin;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    /*
     * Used for client synchronization and as a fallback.
     * Persistent data is authoritative on the server.
     */
    @Unique
    private boolean sulfuricresonance$combustionBelt;

    @Unique
    private BlockEntity sulfuricresonance$self() {
        return (BlockEntity) (Object) this;
    }

    @Override
    public boolean sulfuricresonance$isCombustionBelt() {
        CompoundTag persistentData =
                sulfuricresonance$self().getPersistentData();

        if (persistentData.contains(
                SULFURICRESONANCE$COMBUSTION_BELT_KEY,
                Tag.TAG_BYTE
        )) {
            return persistentData.getBoolean(
                    SULFURICRESONANCE$COMBUSTION_BELT_KEY
            );
        }

        return sulfuricresonance$combustionBelt;
    }

    @Override
    public void sulfuricresonance$setCombustionBelt(
            boolean combustionBelt
    ) {
        sulfuricresonance$combustionBelt = combustionBelt;

        sulfuricresonance$self()
                .getPersistentData()
                .putBoolean(
                        SULFURICRESONANCE$COMBUSTION_BELT_KEY,
                        combustionBelt
                );
    }

    /*
     * Include the value in Create's normal saved/update compound.
     * The getter reads from NeoForge persistent data first.
     */
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
                sulfuricresonance$isCombustionBelt()
        );
    }

    /*
     * Receive the value from disk or a server update packet and place it
     * into both the synchronized field and NeoForge persistent storage.
     */
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
        CompoundTag persistentData =
                sulfuricresonance$self().getPersistentData();

        if (compound.contains(
                SULFURICRESONANCE$COMBUSTION_BELT_KEY,
                Tag.TAG_BYTE
        )) {
            boolean combustionBelt =
                    compound.getBoolean(
                            SULFURICRESONANCE$COMBUSTION_BELT_KEY
                    );

            sulfuricresonance$combustionBelt =
                    combustionBelt;

            persistentData.putBoolean(
                    SULFURICRESONANCE$COMBUSTION_BELT_KEY,
                    combustionBelt
            );

            return;
        }

        /*
         * When the ordinary Create compound lacks the key, recover it
         * from NeoForge's disk-persistent block-entity data.
         */
        if (persistentData.contains(
                SULFURICRESONANCE$COMBUSTION_BELT_KEY,
                Tag.TAG_BYTE
        )) {
            sulfuricresonance$combustionBelt =
                    persistentData.getBoolean(
                            SULFURICRESONANCE$COMBUSTION_BELT_KEY
                    );
        }
    }
}