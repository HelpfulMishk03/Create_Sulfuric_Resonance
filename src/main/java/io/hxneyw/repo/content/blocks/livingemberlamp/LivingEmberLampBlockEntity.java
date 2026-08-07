package io.hxneyw.repo.content.blocks.livingemberlamp;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LivingEmberLampBlockEntity extends BlockEntity {

    private static final Set<LivingEmberLampBlockEntity>
            CLIENT_LAMPS = Collections.newSetFromMap(
            new WeakHashMap<>()
    );

    private static final String LINKED_POSITION_TAG = "LinkedFurnacePos";
    private static final String LINKED_DIMENSION_TAG = "LinkedFurnaceDimension";
    private static final String LINKED_IDENTITY_TAG = "LinkedFurnaceIdentity";
    private static final int VALIDATION_INTERVAL = 10;
    private static final int LIGHT_STEP_INTERVAL = 1;
    private static final int LOW_FUEL_WARNING_TICKS = 200;
    private static final int LOW_FUEL_PULSE_MIN_LIGHT = 9;
    private static final int LOW_FUEL_PULSE_MAX_LIGHT = 15;
    private static final int LOW_FUEL_PULSE_HALF_PERIOD = 10;

    @Nullable
    private BlockPos linkedFurnacePos;
    @Nullable
    private String linkedFurnaceDimension;
    @Nullable
    private UUID linkedFurnaceIdentity;
    private int targetLight;
    private int validationCountdown;
    private int lightStepCountdown;
    private boolean lowFuelWarning;

    public LivingEmberLampBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                AllBlockEntities.LIVING_EMBER_LAMP.get(),
                pos,
                state
        );
    }

    public void setLinkedFurnace(
            @NotNull BlockPos position,
            @NotNull String dimension,
            @NotNull UUID furnaceIdentity
    ) {
        this.linkedFurnacePos = position.immutable();
        this.linkedFurnaceDimension = dimension;
        this.linkedFurnaceIdentity = furnaceIdentity;
        this.validationCountdown = 0;
        markAndSync();
    }

    public static List<LivingEmberLampBlockEntity>
    getLoadedClientLamps() {
        return List.copyOf(CLIENT_LAMPS);
    }

    public boolean doesNotMatchLink(
            @NotNull LivingEmberLampItem.FurnaceLink link
    ) {
        BlockPos linkedPos = this.linkedFurnacePos;
        String linkedDimension = this.linkedFurnaceDimension;
        UUID linkedIdentity = this.linkedFurnaceIdentity;

        return linkedPos == null
                || linkedDimension == null
                || linkedIdentity == null
                || !linkedPos.equals(link.position())
                || !linkedDimension.equals(link.dimension())
                || !linkedIdentity.equals(
                link.furnaceIdentity()
        );
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.level != null
                && this.level.isClientSide) {
            CLIENT_LAMPS.add(this);
        }
    }

    @Override
    public void setRemoved() {
        CLIENT_LAMPS.remove(this);
        super.setRemoved();
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            LivingEmberLampBlockEntity lamp
    ) {
        if (level.isClientSide) {
            return;
        }

        if (--lamp.validationCountdown <= 0) {
            lamp.validationCountdown = VALIDATION_INTERVAL;
            lamp.targetLight = lamp.findTargetLight(level);
        }

        if (--lamp.lightStepCountdown <= 0) {
            lamp.lightStepCountdown = LIGHT_STEP_INTERVAL;
            lamp.stepLightTowardTarget(level, pos, state);
        }
    }

    private int findTargetLight(Level level) {
        this.lowFuelWarning = false;

        BlockPos linkedPos = this.linkedFurnacePos;
        String linkedDimension = this.linkedFurnaceDimension;
        UUID linkedIdentity = this.linkedFurnaceIdentity;

        if (linkedPos == null
                || linkedIdentity == null
                || !level.dimension().location().toString()
                .equals(linkedDimension)
                || !level.isLoaded(linkedPos)) {
            return 0;
        }

        if (!(level.getBlockEntity(linkedPos)
                instanceof MoltenRotorBlockEntity furnace)) {
            return 0;
        }

        if (!linkedIdentity.equals(furnace.getFurnaceIdentity())) {
            return 0;
        }

        int normalLight = switch (furnace.getCurrentHeatTier()) {
            case NONE -> 0;
            case FADING, SMOULDERING -> 7;
            case KINDLED, SEETHING -> 11;
            case RADIANT -> 13;
        };

        int remainingFuel = furnace.getDisplayFuelTime();
        int remainingHeatedTime = furnace.getDisplayCooldownTime();

        boolean activeFuelEndingSoon =
                !furnace.isCreativeMode()
                        && normalLight >= 10
                        && remainingFuel > 0
                        && remainingFuel <= LOW_FUEL_WARNING_TICKS
                        && furnace.isFuelQueueEmpty();

        boolean heatedStateEndingSoon =
                !furnace.isCreativeMode()
                        && remainingFuel <= 0
                        && normalLight > 0
                        && remainingHeatedTime > 0
                        && remainingHeatedTime
                        <= LOW_FUEL_WARNING_TICKS
                        && furnace.isFuelQueueEmpty();

        this.lowFuelWarning =
                activeFuelEndingSoon
                        || heatedStateEndingSoon;

        return normalLight;
    }
    private void stepLightTowardTarget(
            Level level,
            BlockPos pos,
            BlockState fallbackState
    ) {
        BlockState currentState = level.getBlockState(pos);
        if (!currentState.hasProperty(LivingEmberLampBlock.LIGHT_LEVEL)) {
            currentState = fallbackState;
        }

        int currentLight = currentState.getValue(
                LivingEmberLampBlock.LIGHT_LEVEL
        );
        int effectiveTargetLight = this.lowFuelWarning
                ? this.getLowFuelPulseTarget(level)
                : this.targetLight;

        if (currentLight == effectiveTargetLight) {
            return;
        }

        int nextLight = currentLight
                + Integer.signum(effectiveTargetLight - currentLight);

        level.setBlock(
                pos,
                currentState.setValue(
                        LivingEmberLampBlock.LIGHT_LEVEL,
                        nextLight
                ),
                Block.UPDATE_ALL
        );
    }

    private int getLowFuelPulseTarget(Level level) {
        long pulsePhase = (level.getGameTime()
                / LOW_FUEL_PULSE_HALF_PERIOD) & 1L;

        return pulsePhase == 0L
                ? LOW_FUEL_PULSE_MAX_LIGHT
                : LOW_FUEL_PULSE_MIN_LIGHT;
    }

    private void markAndSync() {
        this.setChanged();

        if (this.level == null
                || this.level.isClientSide) {
            return;
        }

        BlockState state = this.getBlockState();

        this.level.sendBlockUpdated(
                this.worldPosition,
                state,
                state,
                Block.UPDATE_CLIENTS
        );
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(
            @NotNull HolderLookup.Provider registries
    ) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener>
    getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            @NotNull Connection connection,
            @NotNull ClientboundBlockEntityDataPacket packet,
            @NotNull HolderLookup.Provider registries
    ) {
        super.onDataPacket(
                connection,
                packet,
                registries
        );
    }

    @Override
    protected void saveAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        BlockPos linkedPos = this.linkedFurnacePos;
        String linkedDimension = this.linkedFurnaceDimension;
        UUID linkedIdentity = this.linkedFurnaceIdentity;
        if (linkedPos != null
                && linkedDimension != null
                && linkedIdentity != null) {
            tag.putLong(LINKED_POSITION_TAG, linkedPos.asLong());
            tag.putString(
                    LINKED_DIMENSION_TAG,
                    linkedDimension
            );
            tag.putUUID(LINKED_IDENTITY_TAG, linkedIdentity);
        }
    }

    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        if (tag.contains(LINKED_POSITION_TAG)
                && tag.contains(LINKED_DIMENSION_TAG)
                && tag.hasUUID(LINKED_IDENTITY_TAG)) {
            this.linkedFurnacePos = BlockPos.of(
                    tag.getLong(LINKED_POSITION_TAG)
            );
            this.linkedFurnaceDimension = tag.getString(
                    LINKED_DIMENSION_TAG
            );
            this.linkedFurnaceIdentity = tag.getUUID(
                    LINKED_IDENTITY_TAG
            );
        } else {
            this.linkedFurnacePos = null;
            this.linkedFurnaceDimension = null;
            this.linkedFurnaceIdentity = null;
        }

        this.validationCountdown = 0;
    }
}
