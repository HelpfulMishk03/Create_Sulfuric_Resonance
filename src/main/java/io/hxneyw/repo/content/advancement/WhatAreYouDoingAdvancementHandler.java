package io.hxneyw.repo.content.advancement;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltHeatResolver;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "sulfuricresonance")
public final class WhatAreYouDoingAdvancementHandler {

    private static final int REQUIRED_TICKS = 600;

    private static final ResourceLocation ROLLING_FIRE_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "rolling_fire"
            );

    private static final ResourceLocation ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "what_are_you_doing"
            );

    private static final Map<ServerPlayer, Progress> PROGRESS =
            new WeakHashMap<>();

    private WhatAreYouDoingAdvancementHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!player.isAlive() || player.isSpectator()) {
            PROGRESS.remove(player);
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        MinecraftServer server = level.getServer();

        AdvancementHolder parent =
                server.getAdvancements().get(ROLLING_FIRE_ID);

        AdvancementHolder advancement =
                server.getAdvancements().get(ADVANCEMENT_ID);

        if (parent == null || advancement == null) {
            PROGRESS.remove(player);
            return;
        }

        if (player.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone()) {
            PROGRESS.remove(player);
            return;
        }

        if (!player.getAdvancements()
                .getOrStartProgress(parent)
                .isDone()) {
            PROGRESS.remove(player);
            return;
        }

        BeltContext beltContext =
                findCombustionBelt(player, level);

        if (beltContext == null) {
            PROGRESS.remove(player);
            return;
        }

        long gameTime = level.getGameTime();
        ResourceKey<Level> dimension = level.dimension();

        Progress previous = PROGRESS.get(player);

        int ticks = previous != null
                && previous.dimension().equals(dimension)
                && previous.controllerPosition().equals(
                        beltContext.controllerPosition()
                )
                && previous.lastGameTime() + 1L == gameTime
                ? previous.ticks() + 1
                : 1;

        if (ticks < REQUIRED_TICKS) {
            PROGRESS.put(
                    player,
                    new Progress(
                            dimension,
                            beltContext.controllerPosition(),
                            gameTime,
                            ticks
                    )
            );
            return;
        }

        player.getAdvancements().award(
                advancement,
                "survive_combustion_belt"
        );

        PROGRESS.remove(player);
    }

    private static BeltContext findCombustionBelt(
            ServerPlayer player,
            ServerLevel level
    ) {
        Set<BlockPos> candidates = new LinkedHashSet<>();

        candidates.add(player.getOnPos());

        candidates.add(BlockPos.containing(
                player.getX(),
                player.getY() - 0.05D,
                player.getZ()
        ));

        candidates.add(BlockPos.containing(
                player.getX(),
                player.getY() - 0.5D,
                player.getZ()
        ));

        for (BlockPos position : candidates) {
            if (!level.isLoaded(position)) {
                continue;
            }

            BlockEntity blockEntity =
                    level.getBlockEntity(position);

            if (!(blockEntity instanceof BeltBlockEntity)
                    || !(blockEntity
                    instanceof CombustionBeltAccessor accessor)
                    || !accessor
                    .sulfuricresonance$isCombustionBelt()) {
                continue;
            }

            BeltBlockEntity controller =
                    BeltHelper.getControllerBE(
                            level,
                            position
                    );

            if (controller == null
                    || !controller.isController()
                    || controller.beltLength <= 0) {
                continue;
            }

            CombustionBeltHeatResolver.Result result =
                    CombustionBeltHeatResolver.resolveChain(
                            level,
                            controller
                    );

            if (result.heatTier() == null
                    || result.heatTier().rank
                    < MoltenRotorBlockEntity
                    .RotorHeatLevel.RADIANT.rank) {
                continue;
            }

            return new BeltContext(
                    controller.getBlockPos().immutable()
            );
        }

        return null;
    }

    private record BeltContext(
            BlockPos controllerPosition
    ) {
    }

    private record Progress(
            ResourceKey<Level> dimension,
            BlockPos controllerPosition,
            long lastGameTime,
            int ticks
    ) {
    }
}
