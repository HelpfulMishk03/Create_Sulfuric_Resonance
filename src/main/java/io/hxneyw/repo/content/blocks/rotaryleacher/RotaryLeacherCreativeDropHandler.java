package io.hxneyw.repo.content.blocks.rotaryleacher;

import io.hxneyw.repo.content.registry.AllModBlocks;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

@EventBusSubscriber(modid = "sulfuricresonance")
@SuppressWarnings("unused")
public final class RotaryLeacherCreativeDropHandler {
    private RotaryLeacherCreativeDropHandler() { }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getState().is(AllModBlocks.ROTARY_LEACHER.get())
                && event.getBreaker() instanceof Player player
                && player.isCreative()) {
            event.setCanceled(true);
        }
    }
}
