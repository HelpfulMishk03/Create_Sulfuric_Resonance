package io.hxneyw.repo.client;

import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlockEntity;
import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlockEntity;
import io.hxneyw.repo.content.blocks.thermalwarningalarm.ThermalWarningAlarmBlockEntity;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ThermochemicalNetworkOutlineUtil {

    private static final int DARK_BLUE = 0x708DAD;
    private static final int LIGHT_BLUE = 0x90ADCD;
    private static final double MAX_DISTANCE_SQUARED = 64.0D * 64.0D;

    private ThermochemicalNetworkOutlineUtil() {
    }

    public static int currentColor() {
        return AnimationTickHolder.getTicks() % 16 < 8
                ? DARK_BLUE
                : LIGHT_BLUE;
    }

    public static ThermalRelaySwitchItem.FurnaceLink relayLink(
            LivingEmberLampItem.FurnaceLink link
    ) {
        return new ThermalRelaySwitchItem.FurnaceLink(
                link.position(),
                link.dimension(),
                link.furnaceIdentity()
        );
    }

    public static LivingEmberLampItem.FurnaceLink lampLink(
            ThermalRelaySwitchItem.FurnaceLink link
    ) {
        return new LivingEmberLampItem.FurnaceLink(
                link.position(),
                link.dimension(),
                link.furnaceIdentity()
        );
    }

    public static void renderNetwork(
            ClientLevel level,
            LocalPlayer player,
            ThermalRelaySwitchItem.FurnaceLink link,
            int color
    ) {
        renderFurnace(level, player, link, color);

        for (ThermalRelaySwitchBlockEntity relay :
                ThermalRelaySwitchBlockEntity.getLoadedClientRelays()) {
            if (relay.getLevel() != level
                    || !link.equals(relay.getFurnaceLink())) {
                continue;
            }

            BlockPos pos = relay.getBlockPos();
            renderBlockShape(
                    level,
                    player,
                    pos,
                    new RelayOutlineKey(link, pos.immutable()),
                    color
            );
        }

        for (ThermalWarningAlarmBlockEntity alarm :
                ThermalWarningAlarmBlockEntity.getLoadedClientAlarms()) {
            if (alarm.getLevel() != level
                    || !link.equals(alarm.getFurnaceLink())) {
                continue;
            }

            BlockPos pos = alarm.getBlockPos();
            renderBlockShape(
                    level,
                    player,
                    pos,
                    new AlarmOutlineKey(link, pos.immutable()),
                    color
            );
        }

        LivingEmberLampItem.FurnaceLink lampLink = lampLink(link);

        for (LivingEmberLampBlockEntity lamp :
                LivingEmberLampBlockEntity.getLoadedClientLamps()) {
            if (lamp.getLevel() != level
                    || lamp.doesNotMatchLink(lampLink)) {
                continue;
            }

            BlockPos pos = lamp.getBlockPos();
            renderBlockShape(
                    level,
                    player,
                    pos,
                    new LampOutlineKey(link, pos.immutable()),
                    color
            );
        }

        for (ThermalGaugeBlockEntity gauge :
                ThermalGaugeBlockEntity.getLoadedClientGauges()) {
            if (gauge.getLevel() != level) {
                continue;
            }

            for (com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot slot :
                    gauge.getActiveSlots()) {
                if (!link.equals(gauge.getFurnaceLink(slot))) {
                    continue;
                }

                BlockPos pos = gauge.getBlockPos();

                if (isHidden(level, player, pos)) {
                    continue;
                }

                renderShape(
                        pos,
                        gauge.getSlotShape(slot),
                        new GaugeOutlineKey(link, pos.immutable(), slot),
                        color
                );
            }
        }
    }

    public static boolean isNetworkValid(
            ClientLevel level,
            ThermalRelaySwitchItem.FurnaceLink link
    ) {
        if (!level.dimension().location().toString().equals(link.dimension())) {
            return true;
        }

        BlockPos pos = link.position();

        if (!level.isLoaded(pos)) {
            return true;
        }

        return level.getBlockEntity(pos)
                instanceof MoltenRotorBlockEntity furnace
                && link.furnaceIdentity().equals(
                furnace.getFurnaceIdentity()
        );
    }

    private static void renderFurnace(
            ClientLevel level,
            LocalPlayer player,
            ThermalRelaySwitchItem.FurnaceLink link,
            int color
    ) {
        if (!level.dimension().location().toString().equals(link.dimension())) {
            return;
        }

        BlockPos pos = link.position();

        if (isHidden(level, player, pos)
                || !(level.getBlockEntity(pos)
                instanceof MoltenRotorBlockEntity furnace)
                || !link.furnaceIdentity().equals(
                furnace.getFurnaceIdentity()
        )) {
            return;
        }

        renderBlockShape(
                level,
                player,
                pos,
                new FurnaceOutlineKey(link),
                color
        );
    }

    private static void renderBlockShape(
            ClientLevel level,
            LocalPlayer player,
            BlockPos pos,
            Object key,
            int color
    ) {
        if (isHidden(level, player, pos)) {
            return;
        }

        renderShape(
                pos,
                level.getBlockState(pos).getShape(level, pos),
                key,
                color
        );
    }

    private static boolean isHidden(
            ClientLevel level,
            LocalPlayer player,
            BlockPos pos
    ) {
        return !level.isLoaded(pos)
                || player.distanceToSqr(
                Vec3.atCenterOf(pos)
        ) > MAX_DISTANCE_SQUARED;
    }

    private static void renderShape(
            BlockPos pos,
            VoxelShape shape,
            Object key,
            int color
    ) {
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
            ThermalRelaySwitchItem.FurnaceLink link
    ) {
    }

    private record RelayOutlineKey(
            ThermalRelaySwitchItem.FurnaceLink link,
            BlockPos position
    ) {
    }

    private record AlarmOutlineKey(
            ThermalRelaySwitchItem.FurnaceLink link,
            BlockPos position
    ) {
    }

    private record LampOutlineKey(
            ThermalRelaySwitchItem.FurnaceLink link,
            BlockPos position
    ) {
    }

    private record GaugeOutlineKey(
            ThermalRelaySwitchItem.FurnaceLink link,
            BlockPos position,
            com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot slot
    ) {
    }
}
