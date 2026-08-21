package io.hxneyw.repo.client;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlockEntity;
import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlockEntity;
import io.hxneyw.repo.content.blocks.thermalwarningalarm.ThermalWarningAlarmBlockEntity;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class ThermochemicalNetworkClientHandler {

    static {
        ThermalGaugeBlockEntity.setTargetedSlotResolver(
                ThermochemicalNetworkClientHandler::getTargetedGaugeSlot
        );
    }

    private ThermochemicalNetworkClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;

        if (player == null
                || level == null
                || !isHoldingWrench(player)) {
            return;
        }

        HitResult hitResult = minecraft.hitResult;

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return;
        }

        ThermalRelaySwitchItem.FurnaceLink link =
                resolveHoveredNetwork(
                        level,
                        blockHitResult
                );

        if (link == null
                || !ThermochemicalNetworkOutlineUtil
                .isNetworkValid(level, link)) {
            return;
        }

        ThermochemicalNetworkOutlineUtil.renderNetwork(
                level,
                player,
                link,
                ThermochemicalNetworkOutlineUtil.currentColor()
        );
    }

    private static boolean isHoldingWrench(LocalPlayer player) {
        return player.getMainHandItem().getItem() instanceof WrenchItem
                || player.getOffhandItem().getItem() instanceof WrenchItem;
    }

    @Nullable
    private static PanelSlot getTargetedGaugeSlot(
            ThermalGaugeBlockEntity gauge
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        HitResult hitResult = minecraft.hitResult;

        if (level == null
                || gauge.getLevel() != level
                || !(hitResult instanceof BlockHitResult blockHitResult)
                || !blockHitResult.getBlockPos()
                .equals(gauge.getBlockPos())) {
            return null;
        }

        PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                gauge.getBlockPos(),
                gauge.getBlockState(),
                blockHitResult.getLocation()
        );

        return gauge.hasGauge(slot)
                ? slot
                : null;
    }

    @Nullable
    private static ThermalRelaySwitchItem.FurnaceLink
    resolveHoveredNetwork(
            ClientLevel level,
            BlockHitResult hit
    ) {
        BlockPos pos = hit.getBlockPos();

        if (!level.isLoaded(pos)) {
            return null;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (blockEntity instanceof MoltenRotorBlockEntity furnace) {
            return new ThermalRelaySwitchItem.FurnaceLink(
                    pos.immutable(),
                    level.dimension().location().toString(),
                    furnace.getFurnaceIdentity()
            );
        }

        if (blockEntity instanceof ThermalRelaySwitchBlockEntity relay) {
            return relay.getFurnaceLink();
        }

        if (blockEntity instanceof ThermalWarningAlarmBlockEntity alarm) {
            return alarm.getFurnaceLink();
        }

        if (blockEntity instanceof LivingEmberLampBlockEntity lamp) {
            LivingEmberLampItem.FurnaceLink link =
                    lamp.getFurnaceLink();

            return link == null
                    ? null
                    : ThermochemicalNetworkOutlineUtil.relayLink(link);
        }

        if (blockEntity instanceof ThermalGaugeBlockEntity gauge) {
            PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                    pos,
                    gauge.getBlockState(),
                    hit.getLocation()
            );

            return gauge.getFurnaceLink(slot);
        }

        return null;
    }
}
