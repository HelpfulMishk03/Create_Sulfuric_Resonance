package io.hxneyw.repo.client;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import java.util.UUID;
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
public final class ThermalRelaySwitchClientHandler {

    private ThermalRelaySwitchClientHandler() {
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

        ItemStack heldStack = getHeldRelayStack(player);

        if (heldStack.isEmpty()) {
            return;
        }

        UUID networkId =
                ThermalRelaySwitchItem.getNetworkId(heldStack);

        ThermalRelaySwitchItem.FurnaceLink link =
                ThermalRelaySwitchItem.getLinkedFurnace(heldStack);

        if (networkId == null || link == null) {
            return;
        }

        ThermochemicalNetworkOutlineUtil.renderNetwork(
                level,
                player,
                link,
                ThermochemicalNetworkOutlineUtil.currentColor()
        );
    }

    private static ItemStack getHeldRelayStack(
            LocalPlayer player
    ) {
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem() instanceof ThermalRelaySwitchItem) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();

        if (offHand.getItem() instanceof ThermalRelaySwitchItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }
}
