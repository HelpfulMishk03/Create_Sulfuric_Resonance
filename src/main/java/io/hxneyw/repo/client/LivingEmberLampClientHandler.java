package io.hxneyw.repo.client;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import java.util.Optional;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;


@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class LivingEmberLampClientHandler {

    private static final int DARK_BLUE = 0x708DAD;
    private static final int LIGHT_BLUE = 0x90ADCD;

    private static final double MAX_DISTANCE_SQUARED =
            64.0D * 64.0D;

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

        Optional<LivingEmberLampItem.FurnaceLink>
                optionalLink =
                LivingEmberLampItem.getLink(heldLamp);

        if (optionalLink.isEmpty()) {
            return;
        }

        LivingEmberLampItem.FurnaceLink link =
                optionalLink.get();

        int color =
                AnimationTickHolder.getTicks() % 16 < 8
                        ? DARK_BLUE
                        : LIGHT_BLUE;

        renderFurnace(
                level,
                player,
                link,
                color
        );

        for (LivingEmberLampBlockEntity lamp :
                LivingEmberLampBlockEntity
                        .getLoadedClientLamps()) {
            if (lamp.getLevel() != level
                    || lamp.doesNotMatchLink(link)) {
                continue;
            }

            renderLamp(
                    level,
                    player,
                    lamp.getBlockPos(),
                    link,
                    color
            );
        }
    }

    private static ItemStack getHeldLampStack(
            LocalPlayer player
    ) {
        ItemStack mainHand =
                player.getMainHandItem();

        if (mainHand.getItem()
                instanceof LivingEmberLampItem) {
            return mainHand;
        }

        ItemStack offHand =
                player.getOffhandItem();

        if (offHand.getItem()
                instanceof LivingEmberLampItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private static void renderFurnace(
            ClientLevel level,
            LocalPlayer player,
            LivingEmberLampItem.FurnaceLink link,
            int color
    ) {
        if (!level.dimension()
                .location()
                .toString()
                .equals(link.dimension())) {
            return;
        }

        BlockPos pos = link.position();

        if (isHidden(level, player, pos)) {
            return;
        }

        if (!(level.getBlockEntity(pos)
                instanceof MoltenRotorBlockEntity furnace)
                || !link.furnaceIdentity().equals(
                furnace.getFurnaceIdentity()
        )) {
            return;
        }

        renderShape(
                level,
                pos,
                new FurnaceOutlineKey(link),
                color
        );
    }

    private static void renderLamp(
            ClientLevel level,
            LocalPlayer player,
            BlockPos pos,
            LivingEmberLampItem.FurnaceLink link,
            int color
    ) {
        if (isHidden(level, player, pos)) {
            return;
        }

        if (!(level.getBlockEntity(pos)
                instanceof LivingEmberLampBlockEntity lamp)
                || lamp.doesNotMatchLink(link)) {
            return;
        }

        renderShape(
                level,
                pos,
                new LampOutlineKey(
                        link,
                        pos.immutable()
                ),
                color
        );
    }

    private static boolean isHidden(
            ClientLevel level,
            LocalPlayer player,
            BlockPos pos
    ) {
        return !level.isLoaded(pos)
                || !(player.distanceToSqr(
                Vec3.atCenterOf(pos)
        ) <= MAX_DISTANCE_SQUARED);
    }

    private static void renderShape(
            ClientLevel level,
            BlockPos pos,
            Object key,
            int color
    ) {
        VoxelShape shape =
                level.getBlockState(pos)
                        .getShape(level, pos);

        if (shape.isEmpty()) {
            return;
        }

        AABB box = shape.bounds()
                .inflate(-1.0D / 128.0D)
                .move(pos);

        Outliner.getInstance()
                .showAABB(key, box, 2)
                .lineWidth(1.0F / 32.0F)
                .disableLineNormals()
                .colored(color);
    }

    private record FurnaceOutlineKey(
            LivingEmberLampItem.FurnaceLink link
    ) {
    }

    private record LampOutlineKey(
            LivingEmberLampItem.FurnaceLink link,
            BlockPos lampPosition
    ) {
    }
}
