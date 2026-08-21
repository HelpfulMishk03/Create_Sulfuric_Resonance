package io.hxneyw.repo.content.process;

import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ProcessMonitorLinking {
    private static final String ROOT_TAG = "SulfuricResonanceProcessLink";
    private static final String MONITOR_TAG = "Monitor";
    private static final String CHANNEL_TAG = "Channel";

    private ProcessMonitorLinking() {
    }

    public static void begin(
            @NotNull Player player,
            @NotNull Level level,
            @NotNull ProcessMonitorBlockEntity monitor
    ) {
        CompoundTag pending = new CompoundTag();
        pending.put(MONITOR_TAG, monitor.createReference(level).save());
        pending.putInt(CHANNEL_TAG, monitor.getSelectedChannel());
        player.getPersistentData().put(ROOT_TAG, pending);
    }

    public static void cancel(@NotNull Player player) {
        player.getPersistentData().remove(ROOT_TAG);
    }

    public static boolean isPending(@NotNull Player player) {
        return read(player) != null;
    }

    public static boolean tryComplete(
            @NotNull Player player,
            @NotNull Level targetLevel,
            @NotNull BlockPos targetPos,
            @NotNull IProcessStateProvider provider
    ) {
        if (targetLevel.isClientSide) {
            return false;
        }

        PendingLink pending = read(player);
        if (pending == null) {
            return false;
        }

        ProcessMonitorResolver.Resolution resolution =
                ProcessMonitorResolver.resolve(targetLevel, pending.monitor());
        if (!resolution.isResolved()) {
            cancel(player);
            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance.process_monitor.link_failed"
                    ),
                    true
            );
            return true;
        }

        ProcessMonitorBlockEntity monitor = resolution.monitor();
        if (monitor == null) {
            cancel(player);
            return true;
        }

        ProcessTargetRef target = new ProcessTargetRef(
                targetPos.immutable(),
                targetLevel.dimension().location().toString(),
                provider.getProcessIdentity()
        );

        monitor.setTarget(pending.channel(), target);
        monitor.setSelectedChannel(pending.channel());
        monitor.pulseBindingContact();
        cancel(player);

        player.displayClientMessage(
                Component.translatable(
                        "message.sulfuricresonance.process_monitor.linked",
                        pending.channel() + 1,
                        targetLevel.getBlockState(targetPos).getBlock().getName()
                ),
                true
        );
        return true;
    }

    private static @Nullable PendingLink read(@NotNull Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag pending = root.getCompound(ROOT_TAG);
        if (!pending.contains(MONITOR_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }

        ProcessMonitorRef monitor = ProcessMonitorRef.load(
                pending.getCompound(MONITOR_TAG)
        );
        if (monitor == null) {
            return null;
        }

        int channel = pending.getInt(CHANNEL_TAG);
        if (channel < 0 || channel >= ProcessMonitorBlockEntity.CHANNEL_COUNT) {
            return null;
        }
        return new PendingLink(monitor, channel);
    }

    private record PendingLink(
            @NotNull ProcessMonitorRef monitor,
            int channel
    ) {
    }
}
