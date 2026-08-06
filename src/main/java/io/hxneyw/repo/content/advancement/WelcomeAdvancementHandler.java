package io.hxneyw.repo.content.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "sulfuricresonance")
public final class WelcomeAdvancementHandler {

    private static final int DELAY_TICKS = 40;

    private static final ResourceLocation ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "root"
            );

    private WelcomeAdvancementHandler() {
    }
@SuppressWarnings("resource")
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount < DELAY_TICKS
                || player.tickCount % 20 != 0) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        MinecraftServer server = level.getServer();

        AdvancementHolder advancement =
                server.getAdvancements().get(ADVANCEMENT_ID);

        if (advancement == null
                || player.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone()) {
            return;
        }

        player.getAdvancements().award(
                advancement,
                "enter_world"
        );

        player.sendSystemMessage(
                Component.translatable(
                        "message.sulfuricresonance.progression_unlocked"
                )
        );
    }
}