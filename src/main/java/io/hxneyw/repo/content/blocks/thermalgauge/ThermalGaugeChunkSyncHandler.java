package io.hxneyw.repo.content.blocks.thermalgauge;

import com.simibubi.create.AllBlocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "sulfuricresonance")
public final class ThermalGaugeChunkSyncHandler {

    private ThermalGaugeChunkSyncHandler() {
    }

    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        for (BlockEntity blockEntity : event.getChunk().getBlockEntities().values()) {
            if (!(blockEntity instanceof ThermalGaugeBlockEntity gauge)) {
                continue;
            }

            if (!AllBlocks.FACTORY_GAUGE.has(gauge.getBlockState())) {
                continue;
            }

            PacketDistributor.sendToPlayer(
                    event.getPlayer(),
                    ThermalGaugeHostPayload.create(gauge)
            );
        }
    }
}
