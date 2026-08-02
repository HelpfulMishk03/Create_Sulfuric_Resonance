package io.hxneyw.repo.client;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
@SuppressWarnings("resource")
@EventBusSubscriber(modid = "sulfuricresonance")
public final class OccupationalHazardHandler {

    private static final ResourceLocation ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "occupational_hazard"
            );

    private OccupationalHazardHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Check twice per second.
        if (player.tickCount % 10 != 0) {
            return;
        }

        BlockPos feetPos = BlockPos.containing(
                player.getX(),
                player.getY() + 0.01D,
                player.getZ()
        );

        BlockPos cruciblePos = findCrucible(player, feetPos);

        if (cruciblePos == null) {
            return;
        }

        if (!isInsideCrucible(player, cruciblePos)) {
            return;
        }

        BlockState furnaceState =
                player.level().getBlockState(cruciblePos.below());

        if (!furnaceState.is(AllModBlocks.MOLTEN_ROTOR_FURNACE.get())) {
            return;
        }

        if (!furnaceState.hasProperty(MoltenRotorBlock.HEAT_LEVEL)) {
            return;
        }

        if (furnaceState.getValue(MoltenRotorBlock.HEAT_LEVEL)
                != HeatLevel.SEETHING) {
            return;
        }

        award(player);
    }

    private static BlockPos findCrucible(
            ServerPlayer player,
            BlockPos feetPos
    ) {
        if (player.level()
                .getBlockState(feetPos)
                .is(AllModBlocks.ASH_CERAMIC_CRUCIBLE.get())) {
            return feetPos;
        }

        BlockPos belowFeet = feetPos.below();

        if (player.level()
                .getBlockState(belowFeet)
                .is(AllModBlocks.ASH_CERAMIC_CRUCIBLE.get())) {
            return belowFeet;
        }

        return null;
    }

    private static boolean isInsideCrucible(
            ServerPlayer player,
            BlockPos cruciblePos
    ) {
        double localX = player.getX() - cruciblePos.getX();
        double localY = player.getY() - cruciblePos.getY();
        double localZ = player.getZ() - cruciblePos.getZ();

        double innerMinimum = 2.0D / 16.0D;
        double innerMaximum = 14.0D / 16.0D;
        double floorHeight = 3.0D / 16.0D;
        double rimHeight = 15.0D / 16.0D;

        return localX > innerMinimum
                && localX < innerMaximum
                && localZ > innerMinimum
                && localZ < innerMaximum
                && localY >= floorHeight - 0.05D
                && localY < rimHeight;
    }

    private static void award(ServerPlayer player) {
        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        AdvancementHolder advancement =
                server.getAdvancements().get(ADVANCEMENT_ID);

        if (advancement == null) {
            return;
        }

        if (player.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone()) {
            return;
        }

        player.getAdvancements().award(
                advancement,
                "inside_combustion_crucible"
        );
    }
}