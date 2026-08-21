package io.hxneyw.repo.content.process;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public record ProcessMonitorArmPayload(@NotNull BlockPos pos)
        implements CustomPacketPayload {

    public static final Type<ProcessMonitorArmPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "arm_process_monitor"
            ));

    public static final StreamCodec<ByteBuf, ProcessMonitorArmPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ProcessMonitorArmPayload::pos,
                    ProcessMonitorArmPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(TYPE, STREAM_CODEC, ProcessMonitorArmPayload::handle);
    }

    private static void handle(
            ProcessMonitorArmPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockPos pos = payload.pos();

        if (!level.hasChunkAt(pos)) {
            return;
        }

        double dx = player.getX() - (pos.getX() + 0.5D);
        double dy = player.getY() - (pos.getY() + 0.5D);
        double dz = player.getZ() - (pos.getZ() + 0.5D);
        if (dx * dx + dy * dy + dz * dz > 64.0D) {
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof ProcessMonitorBlockEntity monitor)) {
            return;
        }

        ProcessMonitorLinking.begin(player, level, monitor);
        monitor.pulseBindingContact();
        player.displayClientMessage(
                Component.translatable(
                        "message.sulfuricresonance.process_monitor.link_armed",
                        monitor.getSelectedChannel() + 1
                ),
                true
        );
    }
}
