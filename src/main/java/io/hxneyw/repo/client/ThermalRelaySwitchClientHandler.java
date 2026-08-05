package io.hxneyw.repo.client;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlockEntity;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import java.util.LinkedHashMap;
import java.util.Map;
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

        if (networkId == null) {
            return;
        }

        Map<FurnaceKey,
                ThermalRelaySwitchItem.FurnaceLink>
                uniqueFurnaces = new LinkedHashMap<>();

        Map<BlockPos, ThermalRelaySwitchBlockEntity>
                uniqueRelays = new LinkedHashMap<>();


        for (ThermalRelaySwitchItem.FurnaceLink link :
                ThermalRelaySwitchItem.getLinks(heldStack)) {
            addEndpoint(
                    uniqueFurnaces,
                    link
            );
        }


        for (ThermalRelaySwitchBlockEntity relay :
                ThermalRelaySwitchBlockEntity
                        .getLoadedClientRelays()) {
            if (relay.getLevel() != level
                    || !networkId.equals(
                    relay.getNetworkId()
            )) {
                continue;
            }

            uniqueRelays.putIfAbsent(
                    relay.getBlockPos(),
                    relay
            );

            for (ThermalRelaySwitchItem.FurnaceLink link :
                    relay.getFurnaceLinks()) {
                addEndpoint(
                        uniqueFurnaces,
                        link
                );
            }
        }

        int color =
                AnimationTickHolder.getTicks() % 16 < 8
                        ? DARK_BLUE
                        : LIGHT_BLUE;

        for (Map.Entry<
                BlockPos,
                ThermalRelaySwitchBlockEntity
                > entry : uniqueRelays.entrySet()) {
            renderRelay(
                    level,
                    player,
                    networkId,
                    entry.getKey(),
                    color
            );
        }

        for (Map.Entry<
                FurnaceKey,
                ThermalRelaySwitchItem.FurnaceLink
                > entry : uniqueFurnaces.entrySet()) {
            renderEndpoint(
                    level,
                    player,
                    networkId,
                    entry.getKey(),
                    entry.getValue(),
                    color
            );
        }
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

    private static void addEndpoint(
            Map<FurnaceKey,
                    ThermalRelaySwitchItem.FurnaceLink> endpoints,
            ThermalRelaySwitchItem.FurnaceLink link
    ) {
        endpoints.putIfAbsent(
                FurnaceKey.from(link),
                link
        );
    }

    private static void renderRelay(
            ClientLevel level,
            LocalPlayer player,
            UUID networkId,
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
                || !networkId.equals(relay.getNetworkId())) {
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
                                networkId,
                                pos.immutable()
                        ),
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
            FurnaceKey key,
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
                                key
                        ),
                        box,
                        2
                )
                .lineWidth(1.0F / 32.0F)
                .disableLineNormals()
                .colored(color);
    }

    private record FurnaceKey(
            String dimension,
            BlockPos position,
            UUID furnaceIdentity
    ) {
        private static FurnaceKey from(
                ThermalRelaySwitchItem.FurnaceLink link
        ) {
            return new FurnaceKey(
                    link.dimension(),
                    link.position(),
                    link.furnaceIdentity()
            );
        }
    }

    private record RelayOutlineKey(
            UUID networkId,
            BlockPos relayPosition
    ) {
    }

    private record OutlineKey(
            UUID networkId,
            FurnaceKey furnace
    ) {
    }
}
