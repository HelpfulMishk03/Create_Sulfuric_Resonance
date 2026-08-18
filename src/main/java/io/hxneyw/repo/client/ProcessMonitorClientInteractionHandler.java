package io.hxneyw.repo.client;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import io.hxneyw.repo.content.process.ProcessMonitorArmPayload;
import io.hxneyw.repo.content.process.ProcessMonitorLinking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class ProcessMonitorClientInteractionHandler {

    private ProcessMonitorClientInteractionHandler() {
    }

    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(
            InputEvent.InteractionKeyMappingTriggered event
    ) {
        if (!event.isAttack()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var level = minecraft.level;
        HitResult hitResult = minecraft.hitResult;

        if (player == null
                || level == null
                || !player.isShiftKeyDown()
                || !player.getMainHandItem().isEmpty()
                || !(hitResult instanceof BlockHitResult blockHitResult)) {
            return;
        }

        if (!(level.getBlockEntity(blockHitResult.getBlockPos())
                instanceof ProcessMonitorBlockEntity monitor)) {
            return;
        }

        ProcessMonitorLinking.begin(player, level, monitor);
        PacketDistributor.sendToServer(
                new ProcessMonitorArmPayload(blockHitResult.getBlockPos().immutable())
        );
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide) {
            return;
        }

        var player = event.getEntity();
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return;
        }

        if (!(event.getLevel().getBlockEntity(event.getPos())
                instanceof ProcessMonitorBlockEntity)) {
            return;
        }

        event.setCanceled(true);
    }
}
