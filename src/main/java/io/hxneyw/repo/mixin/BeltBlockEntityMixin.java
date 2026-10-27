package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltHeatResolver;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltExposure;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.List;
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
@SuppressWarnings({"unused", "DataFlowIssue", "ConstantValue"})
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BeltBlockEntityMixin
        implements CombustionBeltAccessor, IHaveGoggleInformation {

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!sulfuricresonance$isCombustionBelt()) return false;
        MoltenRotorBlockEntity.RotorHeatLevel tier =
                sulfuricresonance$getReceivedHeatTier();
        String tierSource = "segment";
        // The controller is the authoritative source for a joined belt. Some
        // Create belt segments can be queried before their per-segment heat
        // sync arrives, so use its synchronized tier as a fallback.
        if (tier == null || tier == MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
            BeltBlockEntity controller = sulfuricresonance$belt().getControllerBE();
            if (controller instanceof CombustionBeltAccessor accessor) {
                MoltenRotorBlockEntity.RotorHeatLevel controllerTier =
                        accessor.sulfuricresonance$getReceivedHeatTier();
                if (controllerTier != null
                        && controllerTier != MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
                    tier = controllerTier;
                    tierSource = "controller-sync";
                }
                // Sable can recreate a client-side belt BE while the ship is
                // moving. In that brief window the tier field may still be
                // NONE even though the synced source position is current.
                // Read the furnace's authoritative heat tier at that synced
                // position before falling back to a client-side network scan.
                if ((tier == null || tier == MoltenRotorBlockEntity.RotorHeatLevel.NONE)
                        && controller instanceof CombustionBeltAccessor sourceAccessor
                        && sourceAccessor.sulfuricresonance$getHeatSourcePos() != null
                        && controller.getLevel() != null
                        && controller.getLevel().isLoaded(
                                sourceAccessor.sulfuricresonance$getHeatSourcePos())) {
                    BlockEntity source = controller.getLevel().getBlockEntity(
                            sourceAccessor.sulfuricresonance$getHeatSourcePos());
                    if (source instanceof MoltenRotorBlockEntity furnace) {
                        MoltenRotorBlockEntity.RotorHeatLevel sourceTier =
                                furnace.getCurrentHeatTier();
                        if (sourceTier != null
                                && sourceTier != MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
                            tier = sourceTier;
                            tierSource = "synced-source";
                        }
                    }
                }
                // Some sublevel belt updates do not deliver the segment's heat
                // sync before Jade asks for its tooltip. Resolve the displayed
                // chain from the local world data as a final fallback.
                if ((tier == null || tier == MoltenRotorBlockEntity.RotorHeatLevel.NONE)
                        && controller.getLevel() != null) {
                    CombustionBeltHeatResolver.Result live =
                            CombustionBeltHeatResolver.resolveChain(
                                    controller.getLevel(), controller);
                    if (live.heatTier() == MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
                        live = CombustionBeltHeatResolver.resolveLocalSource(
                                controller.getLevel(), controller);
                        tierSource = "local-source";
                    } else {
                        tierSource = "chain";
                    }
                    if (live.heatTier() != null) tier = live.heatTier();
                }
            }
        }
        BeltBlockEntity diagnosticController = sulfuricresonance$belt().getControllerBE();
        Level diagnosticLevel = sulfuricresonance$belt().getLevel();
        long gameTime = diagnosticLevel == null ? Long.MIN_VALUE : diagnosticLevel.getGameTime();
        if (Boolean.getBoolean("csr.lightingDiagnostics")
                && diagnosticController != null
                && sulfuricresonance$belt().getBlockPos().equals(diagnosticController.getBlockPos())
                && gameTime % 20 == 0
                && sulfuricresonance$lastGoggleDiagnosticTick != gameTime) {
            sulfuricresonance$lastGoggleDiagnosticTick = gameTime;
            MoltenRotorBlockEntity.RotorHeatLevel controllerTier =
                    diagnosticController instanceof CombustionBeltAccessor accessor
                            ? accessor.sulfuricresonance$getReceivedHeatTier()
                            : null;
            BlockPos sourcePosition = diagnosticController instanceof CombustionBeltAccessor accessor
                    ? accessor.sulfuricresonance$getHeatSourcePos()
                    : null;
            BlockEntity sourceEntity = sourcePosition != null && diagnosticLevel != null
                    && diagnosticLevel.isLoaded(sourcePosition)
                    ? diagnosticLevel.getBlockEntity(sourcePosition)
                    : null;
            MoltenRotorBlockEntity.RotorHeatLevel sourceTier =
                    sourceEntity instanceof MoltenRotorBlockEntity furnace
                            ? furnace.getCurrentHeatTier()
                            : null;
            io.hxneyw.repo.CreateSulfuricResonance.LOGGER.info(
                    "CSR_BELT_GOGGLE pos={} controller={} resultTier={} segmentTier={} controllerTier={} source={} sourcePos={} sourceLoaded={} sourceEntity={} sourceTier={} level={} client={} beltLength={}",
                    sulfuricresonance$belt().getBlockPos(), diagnosticController.getBlockPos(), tier,
                    sulfuricresonance$getReceivedHeatTier(), controllerTier, tierSource, sourcePosition,
                    sourcePosition != null && diagnosticLevel != null && diagnosticLevel.isLoaded(sourcePosition),
                    sourceEntity == null ? "null" : sourceEntity.getClass().getName(), sourceTier,
                    diagnosticLevel == null ? "null" : diagnosticLevel.getClass().getName(),
                    diagnosticLevel != null && diagnosticLevel.isClientSide,
                    diagnosticController.beltLength);
        }
        String stateKey;
        ChatFormatting color;
        if (tier == null || tier == MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
            stateKey = "goggle.sulfuricresonance.combustion_belt.unheated";
            color = ChatFormatting.GRAY;
        } else if (tier.rank >= 4) {
            stateKey = "goggle.sulfuricresonance.combustion_belt.combustion";
            color = ChatFormatting.DARK_PURPLE;
        } else if (tier.rank >= 3) {
            stateKey = "goggle.sulfuricresonance.combustion_belt.superheated";
            color = ChatFormatting.RED;
        } else {
            stateKey = "goggle.sulfuricresonance.combustion_belt.heated";
            color = ChatFormatting.GOLD;
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(
                "goggle.sulfuricresonance.combustion_belt.heat",
                Component.translatable(stateKey)
        ).withStyle(color));
        return true;
    }

    @Unique
    private long sulfuricresonance$lastServerBeltTick = Long.MIN_VALUE;

    @Unique
    private long sulfuricresonance$lastGoggleDiagnosticTick = Long.MIN_VALUE;

    @Unique
    private boolean sulfuricresonance$reportedDuplicateTick;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void sulfuricresonance$tickOncePerGameTick(CallbackInfo ci) {
        BeltBlockEntity belt = (BeltBlockEntity) (Object) this;
        if (belt.getLevel() == null || belt.getLevel().isClientSide || belt.isVirtual()) return;
        long now = belt.getLevel().getGameTime();
        if (sulfuricresonance$lastServerBeltTick == now) {
            if (!sulfuricresonance$reportedDuplicateTick) {
                sulfuricresonance$reportedDuplicateTick = true;
                io.hxneyw.repo.CreateSulfuricResonance.LOGGER.info(
                        "CSR_DUPLICATE_BELT_TICK prevented position={} tick={}", belt.getBlockPos(), now);
            }
            ci.cancel();
            return;
        }
        sulfuricresonance$lastServerBeltTick = now;
    }

    @Unique
    private static final String
            SULFURICRESONANCE$COMBUSTION_BELT_KEY =
            "combustionBelt";

    @Unique
    private static final String
            SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY =
            "ThermochemicalPulley";

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

    @Unique
    private boolean sulfuricresonance$combustionBelt;

    @Unique
    private boolean sulfuricresonance$thermochemicalPulley;

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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
    public boolean sulfuricresonance$isThermochemicalPulley() {
        CompoundTag persistentData =
                sulfuricresonance$self().getPersistentData();

        if (persistentData.contains(
                SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY,
                Tag.TAG_BYTE
        )) {
            return persistentData.getBoolean(
                    SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY
            );
        }

        return sulfuricresonance$thermochemicalPulley;
    }

    @Override
    public void sulfuricresonance$setThermochemicalPulley(
            boolean thermochemicalPulley
    ) {
        sulfuricresonance$thermochemicalPulley =
                thermochemicalPulley;

        sulfuricresonance$self()
                .getPersistentData()
                .putBoolean(
                        SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY,
                        thermochemicalPulley
                );
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

        sulfuricresonance$liveHeatThisTick =
                CombustionBeltHeatResolver.Result.NONE;

        if (level == null
                || level.isClientSide()
                || !controller.isController()) {
            return;
        }

        if (!sulfuricresonance$repairCombustionBeltChain(
                controller
        )) {
            return;
        }

        CombustionBeltHeatResolver.Result result =
                CombustionBeltHeatResolver.resolveChain(
                        level,
                        controller
                );

        sulfuricresonance$liveHeatThisTick =
                result;

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
    private static boolean
    sulfuricresonance$repairCombustionBeltChain(
            BeltBlockEntity controller
    ) {
        Level level = controller.getLevel();

        if (level == null
                || controller.beltLength <= 0) {
            return false;
        }

        boolean combustionChain = false;

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

            if (blockEntity
                    instanceof CombustionBeltAccessor accessor
                    && (accessor
                    .sulfuricresonance$isCombustionBelt()
                    || accessor
                    .sulfuricresonance$isThermochemicalPulley())) {
                combustionChain = true;
                break;
            }
        }

        if (!combustionChain) {
            return false;
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
                    instanceof BeltBlockEntity segmentBelt)
                    || !(segmentBelt
                    instanceof CombustionBeltAccessor accessor)) {
                continue;
            }

            boolean pulley = segmentBelt.hasPulley();
            boolean changed =
                    !accessor
                    .sulfuricresonance$isCombustionBelt()
                    || accessor
                    .sulfuricresonance$isThermochemicalPulley()
                    != pulley;

            if (!changed) {
                continue;
            }

            accessor.sulfuricresonance$setCombustionBelt(
                    true
            );
            accessor.sulfuricresonance$setThermochemicalPulley(
                    pulley
            );
            segmentBelt.setChanged();
            segmentBelt.sendData();
        }

        return true;
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

            if (changed) {
                accessor.sulfuricresonance$setReceivedHeatTier(
                        nextHeatTier
                );

                accessor.sulfuricresonance$setHeatSourcePos(
                        nextSourcePosition
                );

                accessor.sulfuricresonance$setHeatFromConduit(
                        nextFromConduit
                );
            }

            // Sable can rebuild client-side sublevel block entities while a
            // ship is moving. Re-send the authoritative tier on every heat
            // scan, even when unchanged, so a rebuilt client BE cannot remain
            // stuck at its default NONE value.
            if (blockEntity instanceof BeltBlockEntity segmentBelt
                    && accessor.sulfuricresonance$isCombustionBelt()) {
                segmentBelt.setChanged();
                segmentBelt.sendData();
            }
        }
    }

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

        compound.putBoolean(
                SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY,
                sulfuricresonance$isThermochemicalPulley()
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

    // Create returns early for full NBT and unchanged casing updates. Every
    // return must load CSR heat; TAIL only covers the model-refresh branch.
    @Inject(
            method = "read",
            at = @At("RETURN"),
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

        if (compound.contains(
                SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY,
                Tag.TAG_BYTE
        )) {
            boolean thermochemicalPulley =
                    compound.getBoolean(
                            SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY
                    );

            sulfuricresonance$thermochemicalPulley =
                    thermochemicalPulley;

            persistentData.putBoolean(
                    SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY,
                    thermochemicalPulley
            );
        } else if (persistentData.contains(
                SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY,
                Tag.TAG_BYTE
        )) {
            sulfuricresonance$thermochemicalPulley =
                    persistentData.getBoolean(
                            SULFURICRESONANCE$THERMOCHEMICAL_PULLEY_KEY
                    );
        }

        // Sable creates client block entities from full chunk NBT (clientPacket
        // is false). Preserve the serialized heat there as well as in update
        // packets. The server recalculates live heat before processing cargo.

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
