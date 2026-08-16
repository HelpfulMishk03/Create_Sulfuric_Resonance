package io.hxneyw.repo.content.blocks.thermalwarningalarm;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.network.ThermochemicalNetworkResolver;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModSounds;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermalWarningAlarmBlockEntity extends BlockEntity {

    private static final String NETWORK_TAG = "RelayNetwork";
    private static final String POSITION_TAG = "LinkedFurnacePos";
    private static final String DIMENSION_TAG = "LinkedFurnaceDimension";
    private static final String IDENTITY_TAG = "LinkedFurnaceIdentity";

    private static final Set<ThermalWarningAlarmBlockEntity> CLIENT_ALARMS =
            Collections.newSetFromMap(new WeakHashMap<>());

    private static final int EVALUATION_INTERVAL = 5;
    private static final int LOW_FUEL_WARNING_TICKS = 200;

    public static final int STRIKER_PERIOD_TICKS = 4;
    public static final int STRIKE_INTERVAL_TICKS = STRIKER_PERIOD_TICKS / 2;
    public static final int FIRST_STRIKE_TICK = STRIKER_PERIOD_TICKS / 4;

    @Nullable
    private UUID networkId;
    @Nullable
    private ThermalRelaySwitchItem.FurnaceLink linkedFurnace;

    private int evaluationTicker;

    public ThermalWarningAlarmBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        super(AllBlockEntities.THERMAL_WARNING_ALARM.get(), pos, state);
    }

    public static void serverTick(
            @NotNull Level level,
            @NotNull ThermalWarningAlarmBlockEntity alarm
    ) {
        if (level.isClientSide) {
            return;
        }

        alarm.evaluationTicker++;
        if (alarm.evaluationTicker >= EVALUATION_INTERVAL) {
            alarm.evaluationTicker = 0;
            alarm.evaluate(level);
        }

        BlockState state = alarm.getBlockState();
        if (state.hasProperty(ThermalWarningAlarmBlock.ALARMING)
                && state.getValue(ThermalWarningAlarmBlock.ALARMING)
                && shouldStrike(level.getGameTime())) {
            level.playSound(
                    null,
                    alarm.worldPosition,
                    AllModSounds.THERMAL_WARNING_ALARM_STRIKE.get(),
                    SoundSource.BLOCKS,
                    0.85F,
                    1.0F
            );
        }
    }

    public static List<ThermalWarningAlarmBlockEntity> getLoadedClientAlarms() {
        return List.copyOf(CLIENT_ALARMS);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null && level.isClientSide) {
            CLIENT_ALARMS.add(this);
        }
    }

    @Override
    public void setRemoved() {
        CLIENT_ALARMS.remove(this);
        super.setRemoved();
    }

    public void setConnection(
            @NotNull UUID networkId,
            @NotNull ThermalRelaySwitchItem.FurnaceLink link
    ) {
        this.networkId = networkId;
        this.linkedFurnace = link;
        this.evaluationTicker = EVALUATION_INTERVAL;
        setChanged();

        if (level != null && !level.isClientSide) {
            evaluate(level);
            sync();
        }
    }

    public void clearConnection() {
        this.networkId = null;
        this.linkedFurnace = null;
        this.evaluationTicker = 0;
        setChanged();

        if (level != null && !level.isClientSide) {
            updateVisualState(level, false, false);
            sync();
        }
    }

    @Nullable
    public UUID getNetworkId() {
        return networkId;
    }

    @Nullable
    public ThermalRelaySwitchItem.FurnaceLink getFurnaceLink() {
        return linkedFurnace;
    }

    private void evaluate(@NotNull Level level) {
        boolean connected = false;
        boolean alarming = false;

        ThermalRelaySwitchItem.FurnaceLink link = linkedFurnace;
        if (link != null) {
            ThermochemicalNetworkResolver.Resolution resolution =
                    ThermochemicalNetworkResolver.resolve(level, link);

            if (resolution.isLinkedButUnavailable()) {
                boolean previousAlarm = getBlockState().getValue(
                        ThermalWarningAlarmBlock.ALARMING
                );
                updateVisualState(level, true, previousAlarm);
                return;
            }

            MoltenRotorBlockEntity furnace = resolution.furnace();
            if (furnace != null) {
                connected = true;
                alarming = isFuelEndingSoon(furnace);

                UUID headNetworkId = furnace.getOrCreateThermalNetworkId();
                if (!headNetworkId.equals(networkId)) {
                    networkId = headNetworkId;
                    setChanged();
                }
            }
        }

        updateVisualState(level, connected, alarming);
    }

    private static boolean isFuelEndingSoon(
            @NotNull MoltenRotorBlockEntity furnace
    ) {
        if (furnace.isCreativeMode() || !furnace.isFuelQueueEmpty()) {
            return false;
        }

        int remainingFuel = furnace.getDisplayFuelTime();
        if (remainingFuel > 0
                && remainingFuel <= LOW_FUEL_WARNING_TICKS
                && furnace.getCurrentHeatTier()
                != MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
            return true;
        }

        int remainingHeatedTime = furnace.getDisplayCooldownTime();
        return remainingFuel <= 0
                && remainingHeatedTime > 0
                && remainingHeatedTime <= LOW_FUEL_WARNING_TICKS
                && furnace.getCurrentHeatTier()
                != MoltenRotorBlockEntity.RotorHeatLevel.NONE;
    }

    private static boolean shouldStrike(long gameTime) {
        return Math.floorMod(
                gameTime - FIRST_STRIKE_TICK,
                STRIKE_INTERVAL_TICKS
        ) == 0;
    }

    private void updateVisualState(
            @NotNull Level level,
            boolean connected,
            boolean alarming
    ) {
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof ThermalWarningAlarmBlock)) {
            return;
        }

        boolean stateConnected = state.getValue(ThermalWarningAlarmBlock.CONNECTED);
        boolean stateAlarming = state.getValue(ThermalWarningAlarmBlock.ALARMING);

        if (stateConnected == connected && stateAlarming == alarming) {
            return;
        }

        level.setBlock(
                worldPosition,
                state.setValue(ThermalWarningAlarmBlock.CONNECTED, connected)
                        .setValue(ThermalWarningAlarmBlock.ALARMING, alarming),
                Block.UPDATE_ALL
        );
        setChanged();
    }

    private void sync() {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();
        level.sendBlockUpdated(
                worldPosition,
                state,
                state,
                Block.UPDATE_CLIENTS
        );
    }

    @Override
    protected void saveAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        if (networkId != null) {
            tag.putUUID(NETWORK_TAG, networkId);
        }

        if (linkedFurnace != null) {
            tag.putLong(POSITION_TAG, linkedFurnace.position().asLong());
            tag.putString(DIMENSION_TAG, linkedFurnace.dimension());
            tag.putUUID(IDENTITY_TAG, linkedFurnace.furnaceIdentity());
        }
    }

    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        networkId = tag.hasUUID(NETWORK_TAG)
                ? tag.getUUID(NETWORK_TAG)
                : null;

        if (tag.contains(POSITION_TAG)
                && tag.contains(DIMENSION_TAG)
                && tag.hasUUID(IDENTITY_TAG)) {
            linkedFurnace = new ThermalRelaySwitchItem.FurnaceLink(
                    BlockPos.of(tag.getLong(POSITION_TAG)),
                    tag.getString(DIMENSION_TAG),
                    tag.getUUID(IDENTITY_TAG)
            );
        } else {
            linkedFurnace = null;
        }

        evaluationTicker = EVALUATION_INTERVAL;
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
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            @NotNull Connection connection,
            @NotNull ClientboundBlockEntityDataPacket packet,
            @NotNull HolderLookup.Provider registries
    ) {
        super.onDataPacket(connection, packet, registries);
    }
}
