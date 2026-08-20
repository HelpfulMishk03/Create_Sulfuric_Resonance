package io.hxneyw.repo.content.blocks.thermalgauge;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public record ThermalGaugeHostPayload(@NotNull BlockPos pos)
        implements CustomPacketPayload {

    public static final Type<ThermalGaugeHostPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "thermal_gauge_host"
            ));

    public static final StreamCodec<ByteBuf, ThermalGaugeHostPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ThermalGaugeHostPayload::pos,
                    ThermalGaugeHostPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(TYPE, STREAM_CODEC, ThermalGaugeHostPayload::handle);
    }

    private static void handle(
            ThermalGaugeHostPayload payload,
            IPayloadContext context
    ) {
        Level level = context.player().level();
        BlockPos pos = payload.pos();

        if (!level.hasChunkAt(pos)) {
            return;
        }

        ThermalGaugeBlockEntity.upgradeFactoryGauge(level, pos);
    }
}
