package io.hxneyw.repo.content.blocks.thermalgauge;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public record ThermalGaugeHostPayload(
        @NotNull BlockPos pos,
        @NotNull CompoundTag state
) implements CustomPacketPayload {

    public static final Type<ThermalGaugeHostPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "thermal_gauge_host"
            ));

    public static final StreamCodec<ByteBuf, ThermalGaugeHostPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ThermalGaugeHostPayload::pos,
                    ByteBufCodecs.COMPOUND_TAG,
                    ThermalGaugeHostPayload::state,
                    ThermalGaugeHostPayload::new
            );

    public static ThermalGaugeHostPayload create(
            ThermalGaugeBlockEntity gauge
    ) {
        Level level = gauge.getLevel();
        if (level == null) {
            throw new IllegalStateException("Thermal Gauge host is not attached to a level");
        }

        return new ThermalGaugeHostPayload(
                gauge.getBlockPos().immutable(),
                gauge.writeClient(
                        new CompoundTag(),
                        level.registryAccess()
                )
        );
    }

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
        ThermalGaugeBlockEntity.applyClientHostState(
                level,
                payload.pos(),
                payload.state()
        );
    }
}
