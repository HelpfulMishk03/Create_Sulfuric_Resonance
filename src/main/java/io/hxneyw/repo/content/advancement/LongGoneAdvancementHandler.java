package io.hxneyw.repo.content.advancement;

import io.hxneyw.repo.content.registry.AllModBlocks;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
@SuppressWarnings("resource")
@EventBusSubscriber(modid = "sulfuricresonance")
public final class LongGoneAdvancementHandler {

    private static final ResourceLocation THE_SWITCH_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "the_switch"
            );

    private static final ResourceLocation LONG_GONE_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "long_gone"
            );

    private static final double AWARD_RADIUS = 8.0D;
    private static final double AWARD_RADIUS_SQUARED =
            AWARD_RADIUS * AWARD_RADIUS;

    private static final int CHECK_INTERVAL = 10;

    private LongGoneAdvancementHandler() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!villager.onGround()) {
            return;
        }

        if (villager.tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        if (!serverLevel.getBlockState(villager.getOnPos()).is(
                AllModBlocks.THERMAL_RELAY_SWITCH.get()
        )) {
            return;
        }

        MinecraftServer server = serverLevel.getServer();

        AdvancementHolder switchAdvancement =
                server.getAdvancements().get(THE_SWITCH_ADVANCEMENT);

        AdvancementHolder longGoneAdvancement =
                server.getAdvancements().get(LONG_GONE_ADVANCEMENT);

        if (switchAdvancement == null || longGoneAdvancement == null) {
            return;
        }

        for (ServerPlayer player : serverLevel.getPlayers(candidate ->
                !candidate.isSpectator()
                        && candidate.distanceToSqr(villager)
                        <= AWARD_RADIUS_SQUARED
        )) {

            if (!player.getAdvancements()
                    .getOrStartProgress(switchAdvancement)
                    .isDone()) {
                continue;
            }


            if (player.getAdvancements()
                    .getOrStartProgress(longGoneAdvancement)
                    .isDone()) {
                continue;
            }

            player.getAdvancements().award(
                    longGoneAdvancement,
                    "villager_on_relay"
            );
        }
    }
}