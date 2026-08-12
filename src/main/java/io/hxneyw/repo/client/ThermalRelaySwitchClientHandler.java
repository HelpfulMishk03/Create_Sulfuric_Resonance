package io.hxneyw.repo.client;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlockEntity;
import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlockEntity;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import java.util.UUID;
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
public final class ThermalRelaySwitchClientHandler {

    private static final int DARK_BLUE = 0x708DAD;
    private static final int LIGHT_BLUE = 0x90ADCD;

    private static final double MAX_DISTANCE_SQUARED =
            64.0D * 64.0D;

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

        ItemStack heldStack =
                getHeldRelayStack(player);

        if (heldStack.isEmpty()) {
            return;
        }

        UUID networkId =
                ThermalRelaySwitchItem.getNetworkId(
                        heldStack
                );

        ThermalRelaySwitchItem.FurnaceLink link =
                ThermalRelaySwitchItem.getLinkedFurnace(
                        heldStack
                );

        if (networkId == null || link == null) {
            return;
        }

        int color =
                AnimationTickHolder.getTicks() % 16 < 8
                        ? DARK_BLUE
                        : LIGHT_BLUE;

        for (ThermalRelaySwitchBlockEntity relay :
                ThermalRelaySwitchBlockEntity
                        .getLoadedClientRelays()) {
            if (relay.getLevel() != level
                    || !link.equals(
                    relay.getFurnaceLink()
            )) {
                continue;
            }

            renderRelay(
                    level,
                    player,
                    link,
                    relay.getBlockPos(),
                    color
            );
        }

        for (ThermalGaugeBlockEntity gauge :
                ThermalGaugeBlockEntity
                        .getLoadedClientGauges()) {
            if (gauge.getLevel() != level
                    || gauge.doesNotMatchLink(link)) {
                continue;
            }

            renderConnectedDevice(
                    level,
                    player,
                    gauge.getBlockPos(),
                    new GaugeOutlineKey(
                            link,
                            gauge.getBlockPos().immutable()
                    ),
                    color
            );
        }

        LivingEmberLampItem.FurnaceLink lampLink =
                new LivingEmberLampItem.FurnaceLink(
                        link.position(),
                        link.dimension(),
                        link.furnaceIdentity()
                );

        for (LivingEmberLampBlockEntity lamp :
                LivingEmberLampBlockEntity
                        .getLoadedClientLamps()) {
            if (lamp.getLevel() != level
                    || lamp.doesNotMatchLink(lampLink)) {
                continue;
            }

            renderConnectedDevice(
                    level,
                    player,
                    lamp.getBlockPos(),
                    new LampOutlineKey(
                            link,
                            lamp.getBlockPos().immutable()
                    ),
                    color
            );
        }

        renderEndpoint(
                level,
                player,
                networkId,
                link,
                color
        );
    }

    private static ItemStack getHeldRelayStack(
            LocalPlayer player
    ) {
        ItemStack mainHand =
                player.getMainHandItem();

        if (mainHand.getItem()
                instanceof ThermalRelaySwitchItem) {
            return mainHand;
        }

        ItemStack offHand =
                player.getOffhandItem();

        if (offHand.getItem()
                instanceof ThermalRelaySwitchItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private static void renderRelay(
            ClientLevel level,
            LocalPlayer player,
            ThermalRelaySwitchItem.FurnaceLink link,
            BlockPos pos,
            int color
    ) {
        if (!level.isLoaded(pos)
                || player.distanceToSqr(
                Vec3.atCenterOf(pos)
        ) > MAX_DISTANCE_SQUARED) {
            return;
        }

        if (!(level.getBlockEntity(pos)
                instanceof ThermalRelaySwitchBlockEntity relay)
                || !link.equals(relay.getFurnaceLink())) {
            return;
        }

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
                .showAABB(
                        new RelayOutlineKey(
                                link,
                                pos.immutable()
                        ),
                        box,
                        2
                )
                .lineWidth(1.0F / 32.0F)
                .disableLineNormals()
                .colored(color);
    }

    private static void renderConnectedDevice(
            ClientLevel level,
            LocalPlayer player,
            BlockPos pos,
            Object key,
            int color
    ) {
        if (!level.isLoaded(pos)
                || player.distanceToSqr(
                Vec3.atCenterOf(pos)
        ) > MAX_DISTANCE_SQUARED) {
            return;
        }

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
                .showAABB(
                        key,
                        box,
                        2
                )
                .lineWidth(1.0F / 32.0F)
                .disableLineNormals()
                .colored(color);
    }

    private static void renderEndpoint(
            ClientLevel level,
            LocalPlayer player,
            UUID networkId,
            ThermalRelaySwitchItem.FurnaceLink link,
            int color
    ) {
        if (!level.dimension()
                .location()
                .toString()
                .equals(link.dimension())) {
            return;
        }

        BlockPos pos = link.position();

        if (!level.isLoaded(pos)
                || player.distanceToSqr(
                Vec3.atCenterOf(pos)
        ) > MAX_DISTANCE_SQUARED) {
            return;
        }

        if (!(level.getBlockEntity(pos)
                instanceof MoltenRotorBlockEntity furnace)) {
            return;
        }

        if (!link.furnaceIdentity()
                .equals(
                        furnace.getFurnaceIdentity()
                )) {
            return;
        }

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
                .showAABB(
                        new OutlineKey(
                                networkId,
                                link
                        ),
                        box,
                        2
                )
                .lineWidth(1.0F / 32.0F)
                .disableLineNormals()
                .colored(color);
    }

    private record GaugeOutlineKey(
            ThermalRelaySwitchItem.FurnaceLink furnace,
            BlockPos gaugePosition
    ) {
    }

    private record LampOutlineKey(
            ThermalRelaySwitchItem.FurnaceLink furnace,
            BlockPos lampPosition
    ) {
    }

    private record RelayOutlineKey(
            ThermalRelaySwitchItem.FurnaceLink furnace,
            BlockPos relayPosition
    ) {
    }

    private record OutlineKey(
            UUID networkId,
            ThermalRelaySwitchItem.FurnaceLink furnace
    ) {
    }
}
