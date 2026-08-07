package io.hxneyw.repo.content.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class RollingFireAdvancement {

    private static final ResourceLocation ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "rolling_fire"
            );

    private static final double AWARD_RADIUS = 8.0D;
    private static final double AWARD_RADIUS_SQUARED =
            AWARD_RADIUS * AWARD_RADIUS;

    private RollingFireAdvancement() {
    }

    public static void awardNearby(
            @NotNull ServerLevel level,
            @NotNull Vec3 completionPosition
    ) {
        MinecraftServer server = level.getServer();

        AdvancementHolder advancement =
                server.getAdvancements().get(ADVANCEMENT_ID);

        if (advancement == null) {
            return;
        }

        for (ServerPlayer player : level.getPlayers(candidate ->
                !candidate.isSpectator()
                        && candidate.position()
                        .distanceToSqr(completionPosition)
                        <= AWARD_RADIUS_SQUARED
        )) {
            if (player.getAdvancements()
                    .getOrStartProgress(advancement)
                    .isDone()) {
                continue;
            }

            player.getAdvancements().award(
                    advancement,
                    "processed_item"
            );
        }
    }
}
