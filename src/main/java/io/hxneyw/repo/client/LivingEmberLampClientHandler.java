package io.hxneyw.repo.client;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class LivingEmberLampClientHandler {

    private LivingEmberLampClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(
            ClientTickEvent.Post event
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;

        if (player == null || level == null) {
            return;
        }

        ItemStack heldLamp = getHeldLampStack(player);

        if (heldLamp.isEmpty()) {
            return;
        }

        Optional<LivingEmberLampItem.FurnaceLink> optionalLink =
                LivingEmberLampItem.getLink(heldLamp);

        if (optionalLink.isEmpty()) {
            return;
        }

        ThermochemicalNetworkOutlineUtil.renderNetwork(
                level,
                player,
                ThermochemicalNetworkOutlineUtil.relayLink(
                        optionalLink.get()
                ),
                ThermochemicalNetworkOutlineUtil.currentColor()
        );
    }

    private static ItemStack getHeldLampStack(
            LocalPlayer player
    ) {
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem() instanceof LivingEmberLampItem) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();

        if (offHand.getItem() instanceof LivingEmberLampItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }
}
