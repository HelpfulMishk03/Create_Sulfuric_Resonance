package io.hxneyw.repo.mixin;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltHeatResolver;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltExposure;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
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
    private static final String
            SULFURICRESONANCE$HEAT_TIER_KEY =
            "CombustionBeltHeatTier";

    @Unique
    private static final String
            SULFURICRESONANCE$HEAT_SOURCE_KEY =
            "CombustionBeltHeatSourcePos";

    @Unique
    private static final String
            SULFURICRESONANCE$HEAT_FROM_CONDUIT_KEY =
            "CombustionBeltHeatFromConduit";

    @Unique
    private static final int
            SULFURICRESONANCE$HEAT_SCAN_INTERVAL =
            20;

    /*
     * Used for client synchronization and as a fallback.
     * Persistent data remains authoritative for the belt marker.
     */
    @Unique
    private boolean sulfuricresonance$combustionBelt;

    @Unique
    private int sulfuricresonance$heatScanTicks =
            SULFURICRESONANCE$HEAT_SCAN_INTERVAL;

    @Unique
    private MoltenRotorBlockEntity.RotorHeatLevel
            sulfuricresonance$receivedHeatTier =
            MoltenRotorBlockEntity.RotorHeatLevel.NONE;

    @Unique
    @Nullable
    private BlockPos sulfuricresonance$heatSourcePos;

    @Unique
    private boolean sulfuricresonance$heatFromConduit;

    /*
     * Authoritative heat for recipe processing during the current server tick.
     * Cached belt fields are never used as the processing permission.
     */
    @Unique
    private CombustionBeltHeatResolver.Result
            sulfuricresonance$liveHeatThisTick =
            CombustionBeltHeatResolver.Result.NONE;

    @Unique
    private BeltBlockEntity sulfuricresonance$belt() {
        return (BeltBlockEntity) (Object) this;
    }

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
        sulfuricresonance$combustionBelt =
                combustionBelt;

        sulfuricresonance$self()
                .getPersistentData()
                .putBoolean(
                        SULFURICRESONANCE$COMBUSTION_BELT_KEY,
                        combustionBelt
                );

        if (!combustionBelt) {
            sulfuricresonance$clearHeatState();
        }
    }

    @Override
    public MoltenRotorBlockEntity.RotorHeatLevel
    sulfuricresonance$getReceivedHeatTier() {
        return sulfuricresonance$receivedHeatTier;
    }

    @Override
    public void sulfuricresonance$setReceivedHeatTier(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier
    ) {
        sulfuricresonance$receivedHeatTier =
                heatTier == null
                        ? MoltenRotorBlockEntity.RotorHeatLevel.NONE
                        : heatTier;
    }

    @Override
    public @Nullable BlockPos
    sulfuricresonance$getHeatSourcePos() {
        return sulfuricresonance$heatSourcePos;
    }

    @Override
    public void sulfuricresonance$setHeatSourcePos(
            @Nullable BlockPos sourcePosition
    ) {
        sulfuricresonance$heatSourcePos =
                sourcePosition == null
                        ? null
                        : sourcePosition.immutable();
    }

    @Override
    public boolean sulfuricresonance$isHeatFromConduit() {
        return sulfuricresonance$heatFromConduit;
    }

    @Override
    public void sulfuricresonance$setHeatFromConduit(
            boolean heatFromConduit
    ) {
        sulfuricresonance$heatFromConduit =
                heatFromConduit;
    }

    /*
     * Only the belt controller resolves heat. It scans the complete physical
     * belt chain and distributes one selected source to every marked segment.
     *
     * This makes the Combustion Belt itself a continuous heated processing
     * surface without allowing shafts or unrelated kinetic blocks to relay
     * heat.
     */
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            remap = false
    )
    private void sulfuricresonance$tickCombustionBeltHeat(
            CallbackInfo ci
    ) {
        BeltBlockEntity controller =
                sulfuricresonance$belt();

        Level level = controller.getLevel();

        /*
         * Clear first so an early return can never reuse the previous tick's
         * valid heat result.
         */
        sulfuricresonance$liveHeatThisTick =
                CombustionBeltHeatResolver.Result.NONE;

        if (level == null
                || level.isClientSide()
                || !controller.isController()
                || !sulfuricresonance$isCombustionBelt()) {
            return;
        }

        /*
         * This live result is the only authority allowed to advance or finish
         * a Combustion Belt recipe during this server tick.
         */
        CombustionBeltHeatResolver.Result result =
                CombustionBeltHeatResolver.resolveChain(
                        level,
                        controller
                );

        sulfuricresonance$liveHeatThisTick =
                result;

        /*
         * Cached fields still refresh at the lower rate for client visuals,
         * packets, goggles/debug data, and reduced network traffic.
         */
        sulfuricresonance$heatScanTicks++;

        if (sulfuricresonance$heatScanTicks
                < SULFURICRESONANCE$HEAT_SCAN_INTERVAL) {
            return;
        }

        sulfuricresonance$heatScanTicks = 0;

        sulfuricresonance$applyHeatToChain(
                controller,
                result
        );
    }

    @Unique
    private static void
    sulfuricresonance$applyHeatToChain(
            BeltBlockEntity controller,
            CombustionBeltHeatResolver.Result result
    ) {
        Level level = controller.getLevel();

        if (level == null) {
            return;
        }

        for (int segment = 0;
             segment < controller.beltLength;
             segment++) {

            BlockPos segmentPosition =
                    BeltHelper.getPositionForOffset(
                            controller,
                            segment
                    );

            if (!level.isLoaded(segmentPosition)) {
                continue;
            }

            BlockEntity blockEntity =
                    level.getBlockEntity(segmentPosition);

            if (!(blockEntity
                    instanceof CombustionBeltAccessor accessor)) {
                continue;
            }

            MoltenRotorBlockEntity.RotorHeatLevel
                    nextHeatTier =
                    accessor
                            .sulfuricresonance$isCombustionBelt()
                            ? result.heatTier()
                            : MoltenRotorBlockEntity
                            .RotorHeatLevel.NONE;

            BlockPos nextSourcePosition =
                    accessor
                            .sulfuricresonance$isCombustionBelt()
                            ? result.sourcePosition()
                            : null;

            boolean nextFromConduit =
                    accessor
                            .sulfuricresonance$isCombustionBelt()
                            && result.fromConduit();

            boolean changed =
                    accessor
                            .sulfuricresonance$getReceivedHeatTier()
                            != nextHeatTier
                            || !Objects.equals(
                                    accessor
                                            .sulfuricresonance$getHeatSourcePos(),
                                    nextSourcePosition
                            )
                            || accessor
                            .sulfuricresonance$isHeatFromConduit()
                            != nextFromConduit;

            if (!changed) {
                continue;
            }

            accessor.sulfuricresonance$setReceivedHeatTier(
                    nextHeatTier
            );

            accessor.sulfuricresonance$setHeatSourcePos(
                    nextSourcePosition
            );

            accessor.sulfuricresonance$setHeatFromConduit(
                    nextFromConduit
            );

            if (blockEntity
                    instanceof BeltBlockEntity segmentBelt) {
                segmentBelt.setChanged();
                segmentBelt.sendData();
            }
        }
    }

    /*
     * Process exposure after Create moves the item. Segment distance and active
     * heating time accumulate while the stack continues travelling.
     */
    @Inject(
            method = "tick",
            at = @At("TAIL"),
            remap = false
    )
    private void sulfuricresonance$tickTransportedItemExposure(
            CallbackInfo ci
    ) {
        BeltBlockEntity belt = sulfuricresonance$belt();

        if (!belt.isController()) {
            return;
        }

        CombustionBeltExposure.tickControllerInventory(
                belt,
                sulfuricresonance$liveHeatThisTick.heatTier()
        );

        /*
         * Prevent accidental reuse if Create ever invokes an unusual partial
         * tick path.
         */
        sulfuricresonance$liveHeatThisTick =
                CombustionBeltHeatResolver.Result.NONE;
    }

    @Unique
    private void sulfuricresonance$clearHeatState() {
        sulfuricresonance$receivedHeatTier =
                MoltenRotorBlockEntity.RotorHeatLevel.NONE;

        sulfuricresonance$heatSourcePos = null;
        sulfuricresonance$heatFromConduit = false;
        sulfuricresonance$liveHeatThisTick =
                CombustionBeltHeatResolver.Result.NONE;
        sulfuricresonance$heatScanTicks =
                SULFURICRESONANCE$HEAT_SCAN_INTERVAL;
    }

    /*
     * Include the marker in normal saved/update data.
     *
     * The heat fields are derived cache. They are written so client packets and
     * /data get block can inspect the live state, but server-side disk loading
     * deliberately clears them and performs a fresh scan.
     */
    @Inject(
            method = "write",
            at = @At("TAIL"),
            remap = false
    )
    private void sulfuricresonance$writeCombustionBeltState(
            CompoundTag compound,
            HolderLookup.Provider registries,
            boolean clientPacket,
            CallbackInfo ci
    ) {
        compound.putBoolean(
                SULFURICRESONANCE$COMBUSTION_BELT_KEY,
                sulfuricresonance$isCombustionBelt()
        );

        if (!sulfuricresonance$isCombustionBelt()) {
            compound.remove(
                    SULFURICRESONANCE$HEAT_TIER_KEY
            );
            compound.remove(
                    SULFURICRESONANCE$HEAT_SOURCE_KEY
            );
            compound.remove(
                    SULFURICRESONANCE$HEAT_FROM_CONDUIT_KEY
            );
            return;
        }

        compound.putString(
                SULFURICRESONANCE$HEAT_TIER_KEY,
                sulfuricresonance$receivedHeatTier.serializedId
        );

        compound.putBoolean(
                SULFURICRESONANCE$HEAT_FROM_CONDUIT_KEY,
                sulfuricresonance$heatFromConduit
        );

        if (sulfuricresonance$heatSourcePos != null) {
            compound.putLong(
                    SULFURICRESONANCE$HEAT_SOURCE_KEY,
                    sulfuricresonance$heatSourcePos.asLong()
            );
        } else {
            compound.remove(
                    SULFURICRESONANCE$HEAT_SOURCE_KEY
            );
        }
    }

    /*
     * Receive the persistent belt marker on both server and client.
     * Heat is accepted only from client update packets. A server world load
     * clears the derived cache and scans the live sources again immediately.
     */
    @Inject(
            method = "read",
            at = @At("TAIL"),
            remap = false
    )
    private void sulfuricresonance$readCombustionBeltState(
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
        } else if (persistentData.contains(
                SULFURICRESONANCE$COMBUSTION_BELT_KEY,
                Tag.TAG_BYTE
        )) {
            sulfuricresonance$combustionBelt =
                    persistentData.getBoolean(
                            SULFURICRESONANCE$COMBUSTION_BELT_KEY
                    );
        }

        if (!clientPacket) {
            sulfuricresonance$clearHeatState();
            return;
        }

        sulfuricresonance$receivedHeatTier =
                MoltenRotorBlockEntity.RotorHeatLevel
                        .fromSerializedId(
                                compound.getString(
                                        SULFURICRESONANCE$HEAT_TIER_KEY
                                )
                        );

        sulfuricresonance$heatSourcePos =
                compound.contains(
                        SULFURICRESONANCE$HEAT_SOURCE_KEY,
                        Tag.TAG_LONG
                )
                        ? BlockPos.of(
                                compound.getLong(
                                        SULFURICRESONANCE$HEAT_SOURCE_KEY
                                )
                        )
                        : null;

        sulfuricresonance$heatFromConduit =
                compound.getBoolean(
                        SULFURICRESONANCE$HEAT_FROM_CONDUIT_KEY
                );
    }
}
