package io.hxneyw.repo.content.process;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = "sulfuricresonance")
public final class ProcessMonitorInteractionHandler {
    private ProcessMonitorInteractionHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event
    ) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        var player = event.getEntity();
        var blockEntity = level.getBlockEntity(event.getPos());

        if (!(blockEntity instanceof IProcessStateProvider provider)
                || !ProcessMonitorLinking.isPending(player)) {
            return;
        }

        if (level.isClientSide) {
            ProcessMonitorLinking.cancel(player);
        } else {
            ProcessMonitorLinking.tryComplete(
                    player,
                    level,
                    event.getPos(),
                    provider
            );
        }

        consume(event, level);
    }

    private static void consume(
            PlayerInteractEvent.RightClickBlock event,
            Level level
    ) {
        event.setCancellationResult(
                InteractionResult.sidedSuccess(level.isClientSide)
        );
        event.setCanceled(true);
    }
}
