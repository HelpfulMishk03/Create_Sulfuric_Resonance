package io.hxneyw.repo.content.blocks.processmonitor;

import io.hxneyw.repo.content.process.ProcessMonitorRef;
import io.hxneyw.repo.content.process.ProcessState;
import io.hxneyw.repo.content.process.ProcessTargetRef;
import io.hxneyw.repo.content.process.ProcessTargetResolver;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

public class ProcessMonitorBlockEntity extends BlockEntity {
    public static final int CHANNEL_COUNT = 5;
    private static final int EVALUATION_INTERVAL = 5;

    private static final String IDENTITY_TAG = "MonitorIdentity";
    private static final String SELECTED_CHANNEL_TAG = "SelectedChannel";
    private static final String CHANNELS_TAG = "Channels";
    private static final String CHANNEL_INDEX_TAG = "Index";
    private static final String TARGET_TAG = "Target";
    private static final String STATE_TAG = "State";
    private static final String AVAILABILITY_TAG = "Availability";

    private UUID monitorIdentity = UUID.randomUUID();
    private int selectedChannel;
    private int evaluationTicker;
    private int bindingPulseTicks;

    private final ProcessTargetRef[] targets =
            new ProcessTargetRef[CHANNEL_COUNT];
    private final ProcessState[] cachedStates =
            new ProcessState[CHANNEL_COUNT];
    private final ChannelAvailability[] availability =
            new ChannelAvailability[CHANNEL_COUNT];

    private float clientPreviousSelectorAngle;
    private float clientSelectorAngle;
    private boolean clientSelectorInitialized;

    public ProcessMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.PROCESS_MONITOR.get(), pos, state);
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            cachedStates[i] = ProcessState.IDLE;
            availability[i] = ChannelAvailability.UNLINKED;
        }
    }

    public static void tick(
            Level level,
            ProcessMonitorBlockEntity monitor
    ) {
        if (level.isClientSide) {
            monitor.clientTick();
        } else {
            monitor.serverTick();
        }
    }

    private void serverTick() {
        if (bindingPulseTicks > 0) {
            bindingPulseTicks--;
        }

        if (++evaluationTicker < EVALUATION_INTERVAL) {
            return;
        }
        evaluationTicker = 0;

        boolean changed = false;
        for (int channel = 0; channel < CHANNEL_COUNT; channel++) {
            changed |= evaluateChannel(channel);
        }

        if (changed) {
            setChanged();
            sync();
        }
    }

    private void clientTick() {
        if (!clientSelectorInitialized) {
            clientSelectorAngle = selectorAngleForChannel(selectedChannel);
            clientPreviousSelectorAngle = clientSelectorAngle;
            clientSelectorInitialized = true;
            return;
        }

        clientPreviousSelectorAngle = clientSelectorAngle;
        float target = selectorAngleForChannel(selectedChannel);
        float delta = Mth.wrapDegrees(target - clientSelectorAngle);
        clientSelectorAngle += Mth.clamp(delta, -8.0F, 8.0F);

        if (bindingPulseTicks > 0) {
            bindingPulseTicks--;
        }
    }

    private boolean evaluateChannel(int channel) {
        ProcessTargetRef target = targets[channel];
        ProcessState previousState = cachedStates[channel];
        ChannelAvailability previousAvailability = availability[channel];

        if (target == null || level == null) {
            cachedStates[channel] = ProcessState.IDLE;
            availability[channel] = ChannelAvailability.UNLINKED;
        } else if (previousAvailability == ChannelAvailability.INVALID) {
            cachedStates[channel] = ProcessState.IDLE;
            availability[channel] = ChannelAvailability.INVALID;
        } else {
            ProcessTargetResolver.Resolution resolution =
                    ProcessTargetResolver.resolve(level, target);

            switch (resolution.status()) {
                case RESOLVED -> {
                    var provider = resolution.provider();
                    if (provider == null) {
                        cachedStates[channel] = ProcessState.IDLE;
                        availability[channel] = ChannelAvailability.INVALID;
                    } else {
                        cachedStates[channel] = provider.getProcessState();
                        availability[channel] = ChannelAvailability.AVAILABLE;
                    }
                }
                case UNAVAILABLE -> {
                    cachedStates[channel] = ProcessState.IDLE;
                    availability[channel] = ChannelAvailability.UNAVAILABLE;
                }
                case INVALID -> {
                    cachedStates[channel] = ProcessState.IDLE;
                    availability[channel] = ChannelAvailability.INVALID;
                }
            }
        }

        return previousState != cachedStates[channel]
                || previousAvailability != availability[channel];
    }

    public UUID getMonitorIdentity() {
        return monitorIdentity;
    }

    public ProcessMonitorRef createReference(@NotNull Level level) {
        return new ProcessMonitorRef(
                worldPosition.immutable(),
                level.dimension().location().toString(),
                monitorIdentity
        );
    }

    public void setSelectedChannel(int channel) {
        int clamped = Mth.clamp(channel, 0, CHANNEL_COUNT - 1);
        if (clamped == selectedChannel) {
            return;
        }
        selectedChannel = clamped;
        setChanged();
        sync();
    }

    public int getSelectedChannel() {
        return selectedChannel;
    }

    public void cycleSelectedChannel() {
        setSelectedChannel((selectedChannel + 1) % CHANNEL_COUNT);
    }

    public String getChannelDisplayLabel(int channel) {
        ChannelSnapshot snapshot = getChannelSnapshot(channel);
        return switch (snapshot.availability()) {
            case UNLINKED -> "--";
            case UNAVAILABLE -> "OFF";
            case INVALID -> "ERR";
            case AVAILABLE -> snapshot.state().rendererLabel();
        };
    }

    public @Nullable ProcessTargetRef getTarget(int channel) {
        return validChannel(channel) ? targets[channel] : null;
    }

    public void setTarget(int channel, @NotNull ProcessTargetRef target) {
        if (!validChannel(channel)) {
            return;
        }
        targets[channel] = target;
        cachedStates[channel] = ProcessState.IDLE;
        availability[channel] = ChannelAvailability.UNAVAILABLE;
        evaluationTicker = EVALUATION_INTERVAL;
        evaluateChannel(channel);
        setChanged();
        sync();
    }

    public ChannelSnapshot getChannelSnapshot(int channel) {
        if (!validChannel(channel)) {
            return new ChannelSnapshot(
                    ProcessState.IDLE,
                    ChannelAvailability.UNLINKED
            );
        }
        return new ChannelSnapshot(
                cachedStates[channel],
                availability[channel]
        );
    }

    public boolean hasTelemetrySignal() {
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            if (targets[i] != null
                    && availability[i] != ChannelAvailability.INVALID) {
                return true;
            }
        }
        return false;
    }

    public boolean hasProcessingChannel() {
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            if (availability[i] == ChannelAvailability.AVAILABLE
                    && cachedStates[i] == ProcessState.PROCESSING) {
                return true;
            }
        }
        return false;
    }

    public void pulseBindingContact() {
        bindingPulseTicks = 8;
        setChanged();
        sync();
    }

    public float getBindingPulse(float partialTicks) {
        if (bindingPulseTicks <= 0) {
            return 0.0F;
        }
        return Mth.clamp((bindingPulseTicks - partialTicks) / 8.0F, 0.0F, 1.0F);
    }

    public float getSelectorAngle(float partialTicks) {
        return Mth.lerp(
                partialTicks,
                clientPreviousSelectorAngle,
                clientSelectorAngle
        );
    }

    public static float selectorAngleForChannel(int channel) {
        return switch (Mth.clamp(channel, 0, CHANNEL_COUNT - 1)) {
            case 0 -> -46.0F;
            case 1 -> -25.0F;
            case 2 -> 0.0F;
            case 3 -> 25.0F;
            default -> 46.0F;
        };
    }

    private static boolean validChannel(int channel) {
        return channel >= 0 && channel < CHANNEL_COUNT;
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
        tag.putUUID(IDENTITY_TAG, monitorIdentity);
        tag.putInt(SELECTED_CHANNEL_TAG, selectedChannel);

        ListTag channels = new ListTag();
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            CompoundTag channelTag = new CompoundTag();
            channelTag.putInt(CHANNEL_INDEX_TAG, i);
            if (targets[i] != null) {
                channelTag.put(TARGET_TAG, targets[i].save());
            }
            channelTag.putInt(STATE_TAG, cachedStates[i].ordinal());
            channelTag.putInt(AVAILABILITY_TAG, availability[i].ordinal());
            channels.add(channelTag);
        }
        tag.put(CHANNELS_TAG, channels);
        tag.putInt("BindingPulseTicks", bindingPulseTicks);
    }

    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        if (tag.hasUUID(IDENTITY_TAG)) {
            monitorIdentity = tag.getUUID(IDENTITY_TAG);
        }
        selectedChannel = Mth.clamp(
                tag.getInt(SELECTED_CHANNEL_TAG),
                0,
                CHANNEL_COUNT - 1
        );

        for (int i = 0; i < CHANNEL_COUNT; i++) {
            targets[i] = null;
            cachedStates[i] = ProcessState.IDLE;
            availability[i] = ChannelAvailability.UNLINKED;
        }

        if (tag.contains(CHANNELS_TAG, Tag.TAG_LIST)) {
            ListTag channels = tag.getList(CHANNELS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < channels.size(); i++) {
                CompoundTag channelTag = channels.getCompound(i);
                int channel = channelTag.getInt(CHANNEL_INDEX_TAG);
                if (!validChannel(channel)) {
                    continue;
                }

                if (channelTag.contains(TARGET_TAG, Tag.TAG_COMPOUND)) {
                    targets[channel] = ProcessTargetRef.load(
                            channelTag.getCompound(TARGET_TAG)
                    );
                }
                cachedStates[channel] = ProcessState.fromOrdinal(
                        channelTag.getInt(STATE_TAG)
                );
                availability[channel] = ChannelAvailability.fromOrdinal(
                        channelTag.getInt(AVAILABILITY_TAG)
                );
            }
        }

        bindingPulseTicks = Math.max(0, tag.getInt("BindingPulseTicks"));
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

    public enum ChannelAvailability {
        UNLINKED,
        AVAILABLE,
        UNAVAILABLE,
        INVALID;

        public static ChannelAvailability fromOrdinal(int ordinal) {
            ChannelAvailability[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return UNLINKED;
            }
            return values[ordinal];
        }
    }

    public record ChannelSnapshot(
            @NotNull ProcessState state,
            @NotNull ChannelAvailability availability
    ) {
        public boolean isLinked() {
            return availability != ChannelAvailability.UNLINKED;
        }

        public boolean isAvailable() {
            return availability == ChannelAvailability.AVAILABLE;
        }
    }
}
