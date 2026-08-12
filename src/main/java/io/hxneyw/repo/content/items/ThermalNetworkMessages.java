package io.hxneyw.repo.content.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class ThermalNetworkMessages {

    private ThermalNetworkMessages() {
    }

    public static void showStarted(
            @Nullable Player player
    ) {
        if (player == null) {
            return;
        }

        player.displayClientMessage(
                Component.translatable(
                        "message.sulfuricresonance.thermal_network.started"
                ).withStyle(ChatFormatting.BLUE),
                true
        );
    }
}
