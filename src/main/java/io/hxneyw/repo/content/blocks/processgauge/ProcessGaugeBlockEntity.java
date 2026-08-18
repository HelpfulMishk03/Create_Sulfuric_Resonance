package io.hxneyw.repo.content.blocks.processgauge;

import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import io.hxneyw.repo.content.process.ProcessMonitorRef;
import io.hxneyw.repo.content.process.ProcessMonitorResolver;
import io.hxneyw.repo.content.process.ProcessState;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProcessGaugeBlockEntity extends BlockEntity {
    private static final int EVALUATION_INTERVAL = 5;

    private static final String MONITOR_TAG = "Monitor";
    private static final String CHANNEL_TAG = "SelectedChannel";
    private static final String OBSERVED_STATE_TAG = "ObservedState";
    private static final String LINK_STATUS_TAG = "LinkStatus";
    private static final String ACTIVE_TAG = "Active";

    private @Nullable ProcessMonitorRef monitorReference;
    private int selectedChannel;
    private ProcessState observedState = ProcessState.IDLE;
    private LinkStatus linkStatus = LinkStatus.UNBOUND;
    private boolean active;
    private int evaluationTicker;

    private float clientPreviousPointerAngle;
    private float clientPointerAngle;
    private float clientPreviousDrumAngle;
    private float clientDrumAngle;
    private boolean clientAnglesInitialized;

    public ProcessGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.PROCESS_GAUGE.get(), pos, state);
    }

    public static void tick(
            Level level,
            ProcessGaugeBlockEntity gauge
    ) {
        if (level.isClientSide) {
            gauge.clientTick();
        } else {
            gauge.serverTick();
        }
    }

    private void serverTick() {
        if (++evaluationTicker < EVALUATION_INTERVAL) {
            return;
        }
        evaluationTicker = 0;
        evaluate();
    }

    private void clientTick() {
        if (!clientAnglesInitialized) {
            clientPointerAngle = pointerAngleForChannel(selectedChannel);
            clientPreviousPointerAngle = clientPointerAngle;
            clientDrumAngle = drumAngleForState(observedState);
            clientPreviousDrumAngle = clientDrumAngle;
            clientAnglesInitialized = true;
            return;
        }

        clientPreviousPointerAngle = clientPointerAngle;
        clientPreviousDrumAngle = clientDrumAngle;

        clientPointerAngle = approachAngle(
                clientPointerAngle,
                pointerAngleForChannel(selectedChannel),
                9.0F
        );
        clientDrumAngle = approachAngle(
                clientDrumAngle,
                drumAngleForState(observedState),
                12.0F
        );
    }

    private static float approachAngle(
            float current,
            float target,
            float step
    ) {
        float delta = Mth.wrapDegrees(target - current);
        return current + Mth.clamp(delta, -step, step);
    }

    public void evaluate() {
        if (level == null || level.isClientSide) {
            return;
        }

        LinkStatus previousStatus = linkStatus;
        ProcessState previousObservedState = observedState;
        boolean previousActive = active;

        if (monitorReference == null) {
            linkStatus = LinkStatus.UNBOUND;
            observedState = ProcessState.IDLE;
            active = false;
        } else {
            ProcessMonitorResolver.Resolution monitorResolution =
                    ProcessMonitorResolver.resolve(level, monitorReference);

            switch (monitorResolution.status()) {
                case UNAVAILABLE -> {
                    linkStatus = LinkStatus.UNAVAILABLE;
                    observedState = ProcessState.IDLE;
                    active = false;
                }
                case INVALID -> {
                    linkStatus = LinkStatus.INVALID;
                    observedState = ProcessState.IDLE;
                    active = false;
                }
                case RESOLVED -> {
                    ProcessMonitorBlockEntity monitor =
                            monitorResolution.monitor();
                    if (monitor == null) {
                        linkStatus = LinkStatus.INVALID;
                        observedState = ProcessState.IDLE;
                        active = false;
                    } else {
                        evaluateResolvedMonitor(monitor);
                    }
                }
            }
        }

        if (previousActive != active) {
            updatePoweredState(active);
        }

        if (previousStatus != linkStatus
                || previousObservedState != observedState
                || previousActive != active) {
            setChanged();
            sync();
        }
    }

    private void evaluateResolvedMonitor(
            @NotNull ProcessMonitorBlockEntity monitor
    ) {
        if (monitor.getTarget(selectedChannel) == null) {
            linkStatus = LinkStatus.UNLINKED;
            observedState = ProcessState.IDLE;
            active = false;
            return;
        }

        ProcessMonitorBlockEntity.ChannelSnapshot snapshot =
                monitor.getChannelSnapshot(selectedChannel);

        switch (snapshot.availability()) {
            case AVAILABLE -> {
                linkStatus = LinkStatus.VALID;
                observedState = snapshot.state();
                active = observedState == ProcessState.BLOCKED;
            }
            case INVALID -> {
                linkStatus = LinkStatus.INVALID;
                observedState = ProcessState.IDLE;
                active = false;
            }
            case UNAVAILABLE, UNLINKED -> {
                linkStatus = LinkStatus.UNAVAILABLE;
                observedState = ProcessState.IDLE;
                active = false;
            }
        }
    }

    private void updatePoweredState(boolean powered) {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();
        if (state.hasProperty(ProcessGaugeBlock.POWERED)
                && state.getValue(ProcessGaugeBlock.POWERED) != powered) {
            level.setBlock(
                    worldPosition,
                    state.setValue(ProcessGaugeBlock.POWERED, powered),
                    Block.UPDATE_ALL
            );
            level.updateNeighborsAt(worldPosition, state.getBlock());
            level.updateNeighborsAt(worldPosition.below(), state.getBlock());
        }
    }

    public void configureFromItem(
            @Nullable ProcessMonitorRef monitor
    ) {
        monitorReference = monitor;
        selectedChannel = 0;
        observedState = ProcessState.IDLE;
        evaluationTicker = EVALUATION_INTERVAL;
        setChanged();
        if (level != null && !level.isClientSide) {
            evaluate();
            sync();
        }
    }

    public int getSelectedChannelNumber() {
        return selectedChannel + 1;
    }

    public String getObservedDisplayLabel() {
        return switch (linkStatus) {
            case UNBOUND, UNLINKED -> "--";
            case UNAVAILABLE -> "OFF";
            case INVALID -> "ERR";
            case VALID -> observedState.rendererLabel();
        };
    }

    public void cycleSelectedChannel() {
        selectedChannel = (selectedChannel + 1)
                % ProcessMonitorBlockEntity.CHANNEL_COUNT;
        evaluationTicker = EVALUATION_INTERVAL;
        setChanged();
        if (level != null && !level.isClientSide) {
            evaluate();
            sync();
        }
    }

    public LinkStatus getLinkStatus() {
        return linkStatus;
    }

    public boolean isActive() {
        return active;
    }

    public float getPointerAngle(float partialTicks) {
        return Mth.lerp(
                partialTicks,
                clientPreviousPointerAngle,
                clientPointerAngle
        );
    }

    public float getDrumAngle(float partialTicks) {
        return Mth.lerp(
                partialTicks,
                clientPreviousDrumAngle,
                clientDrumAngle
        );
    }

    public static float pointerAngleForChannel(int channel) {
        return switch (Mth.clamp(
                channel,
                0,
                ProcessMonitorBlockEntity.CHANNEL_COUNT - 1
        )) {
            case 0 -> -48.0F;
            case 1 -> -27.0F;
            case 2 -> 0.0F;
            case 3 -> 27.0F;
            default -> 48.0F;
        };
    }

    public static float drumAngleForState(ProcessState state) {
        return switch (state) {
            case IDLE -> 0.0F;
            case READY -> 90.0F;
            case PROCESSING -> 180.0F;
            case BLOCKED -> -90.0F;
        };
    }

    private void sync() {
        if (level == null) {
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
        if (monitorReference != null) {
            tag.put(MONITOR_TAG, monitorReference.save());
        }
        tag.putInt(CHANNEL_TAG, selectedChannel);
        tag.putString(OBSERVED_STATE_TAG, observedState.serializedName());
        tag.putInt(LINK_STATUS_TAG, linkStatus.ordinal());
        tag.putBoolean(ACTIVE_TAG, active);
    }

    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);
        monitorReference = tag.contains(MONITOR_TAG, Tag.TAG_COMPOUND)
                ? ProcessMonitorRef.load(tag.getCompound(MONITOR_TAG))
                : null;
        selectedChannel = Mth.clamp(
                tag.getInt(CHANNEL_TAG),
                0,
                ProcessMonitorBlockEntity.CHANNEL_COUNT - 1
        );
        observedState = tag.contains(OBSERVED_STATE_TAG, Tag.TAG_STRING)
                ? ProcessState.fromSerializedName(tag.getString(OBSERVED_STATE_TAG))
                : ProcessState.IDLE;
        linkStatus = LinkStatus.fromOrdinal(tag.getInt(LINK_STATUS_TAG));
        active = tag.getBoolean(ACTIVE_TAG);
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

    public enum LinkStatus {
        UNBOUND,
        VALID,
        UNAVAILABLE,
        INVALID,
        UNLINKED;

        public static LinkStatus fromOrdinal(int ordinal) {
            LinkStatus[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return UNBOUND;
            }
            return values[ordinal];
        }
    }
}
