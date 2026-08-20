package io.hxneyw.repo.content.blocks.thermalgauge;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockItem;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.utility.CreateLang;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import io.hxneyw.repo.content.items.ThermalGaugeItem;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "sulfuricresonance")
public final class ThermalGaugeMixedInteractionHandler {

    private ThermalGaugeMixedInteractionHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event
    ) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        BlockHitResult hit = event.getHitVec();
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        BlockState clickedState = level.getBlockState(clickedPos);

        boolean clickedFactoryHost = AllBlocks.FACTORY_GAUGE.has(clickedState);
        boolean clickedThermalHost = clickedState.is(AllModBlocks.THERMAL_GAUGE.get());

        if (!clickedFactoryHost && !clickedThermalHost) {
            BlockPos placementPos = clickedPos.relative(hit.getDirection());
            BlockState placementState = level.getBlockState(placementPos);
            boolean placementFactoryHost = AllBlocks.FACTORY_GAUGE.has(placementState);
            boolean placementThermalHost = placementState.is(AllModBlocks.THERMAL_GAUGE.get());

            if (placementFactoryHost
                    && stack.getItem() instanceof ThermalGaugeItem) {
                placeThermalGaugeOnFactory(
                        event,
                        level,
                        placementPos,
                        placementState,
                        FactoryPanelBlock.getTargetedSlot(
                                placementPos,
                                placementState,
                                hit.getLocation()
                        ),
                        player,
                        stack,
                        level.getBlockEntity(placementPos)
                );
            } else if (placementThermalHost
                    && AllBlocks.FACTORY_GAUGE.isIn(stack)) {
                placeFactoryGaugeOnThermal(
                        event,
                        level,
                        placementPos,
                        placementState,
                        FactoryPanelBlock.getTargetedSlot(
                                placementPos,
                                placementState,
                                hit.getLocation()
                        ),
                        player,
                        stack,
                        level.getBlockEntity(placementPos)
                );
            }
            return;
        }

        PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                clickedPos,
                clickedState,
                hit.getLocation()
        );
        BlockEntity current = level.getBlockEntity(clickedPos);

        if (clickedFactoryHost && stack.getItem() instanceof ThermalGaugeItem) {
            placeThermalGaugeOnFactory(
                    event,
                    level,
                    clickedPos,
                    clickedState,
                    slot,
                    player,
                    stack,
                    current
            );
            return;
        }

        if (clickedThermalHost && AllBlocks.FACTORY_GAUGE.isIn(stack)) {
            placeFactoryGaugeOnThermal(
                    event,
                    level,
                    clickedPos,
                    clickedState,
                    slot,
                    player,
                    stack,
                    current
            );
            return;
        }

        if (!(current instanceof ThermalGaugeBlockEntity gauge)
                || !gauge.hasGauge(slot)) {
            return;
        }

        if (stack.getItem() instanceof WrenchItem && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ItemStack drop = gauge.createItemStack(slot);

                if (gauge.removeGauge(slot)) {
                    player.getInventory().placeItemBackInInventory(drop);
                    IWrenchable.playRemoveSound(level, clickedPos);

                    if (gauge.activePanels() == 0) {
                        level.destroyBlock(clickedPos, false);
                    }
                }
            }

            consume(event, level);
            return;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                gauge.clearConnection(slot);
                player.displayClientMessage(
                        Component.translatable(
                                "message.sulfuricresonance.thermal_gauge.network_removed"
                        ),
                        true
                );
            }

            consume(event, level);
            return;
        }

        UUID networkId = gauge.getNetworkId(slot);
        ThermalRelaySwitchItem.FurnaceLink link = gauge.getFurnaceLink(slot);

        if (stack.getItem() instanceof ThermalRelaySwitchItem) {
            if (!level.isClientSide && link != null) {
                ThermalRelaySwitchItem.setConnection(
                        stack,
                        networkId != null ? networkId : link.furnaceIdentity(),
                        link
                );
                player.getInventory().setChanged();
            }

            consume(event, level);
            return;
        }

        if (stack.getItem() instanceof LivingEmberLampItem) {
            if (!level.isClientSide && link != null) {
                LivingEmberLampItem.setLink(
                        stack,
                        new LivingEmberLampItem.FurnaceLink(
                                link.position(),
                                link.dimension(),
                                link.furnaceIdentity()
                        )
                );
                player.getInventory().setChanged();
            }

            consume(event, level);
            return;
        }

        if (stack.isEmpty()) {
            if (!level.isClientSide) {
                Component message = gauge.isNetworkConnected(slot)
                        ? Component.translatable(
                                "message.sulfuricresonance.thermal_gauge.reading",
                                gauge.getDisplayTemperature(slot)
                        )
                        : Component.translatable(
                                "message.sulfuricresonance.thermal_gauge.no_network"
                        );
                player.displayClientMessage(message, true);
            }

            consume(event, level);
        }
    }

    private static void placeThermalGaugeOnFactory(
            PlayerInteractEvent.RightClickBlock event,
            Level level,
            BlockPos pos,
            BlockState state,
            PanelSlot slot,
            Player player,
            ItemStack stack,
            BlockEntity current
    ) {
        boolean occupied;
        if (current instanceof ThermalGaugeBlockEntity gauge) {
            occupied = gauge.isOccupied(slot);
        } else if (current instanceof FactoryPanelBlockEntity factory) {
            occupied = factory.panels.get(slot).isActive();
        } else {
            return;
        }

        if (occupied) {
            consume(event, level);
            return;
        }

        if (!level.isClientSide) {
            ThermalGaugeBlockEntity gauge =
                    ThermalGaugeBlockEntity.upgradeFactoryGauge(level, pos);
            if (gauge == null) {
                consume(event, level);
                return;
            }

            UUID networkId = ThermalRelaySwitchItem.getNetworkId(stack);
            ThermalRelaySwitchItem.FurnaceLink link =
                    ThermalRelaySwitchItem.getLinkedFurnace(stack);

            if (gauge.addGauge(slot, networkId, link)) {
                if (level instanceof ServerLevel serverLevel) {
                    PacketDistributor.sendToPlayersTrackingChunk(
                            serverLevel,
                            new ChunkPos(pos),
                            ThermalGaugeHostPayload.create(gauge)
                    );
                }

                level.playSound(
                        null,
                        pos,
                        state.getSoundType(level, pos, player).getPlaceSound(),
                        SoundSource.BLOCKS
                );

                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
        }

        consume(event, level);
    }

    private static void placeFactoryGaugeOnThermal(
            PlayerInteractEvent.RightClickBlock event,
            Level level,
            BlockPos pos,
            BlockState state,
            PanelSlot slot,
            Player player,
            ItemStack stack,
            BlockEntity current
    ) {
        if (!(current instanceof ThermalGaugeBlockEntity gauge)) {
            return;
        }

        if (gauge.isOccupied(slot)) {
            consume(event, level);
            return;
        }

        if (!FactoryPanelBlockItem.isTuned(stack)) {
            if (!level.isClientSide) {
                AllSoundEvents.DENY.playOnServer(level, pos);
                player.displayClientMessage(
                        CreateLang.translate("factory_panel.tune_before_placing")
                                .component(),
                        true
                );
            }
            consume(event, level);
            return;
        }

        if (!level.isClientSide) {
            ThermalGaugeBlockEntity mixed = gauge.convertToFactoryHost();
            if (mixed == null || mixed.isOccupied(slot)) {
                consume(event, level);
                return;
            }

            ItemStack panelItem = FactoryPanelBlockItem.fixCtrlCopiedStack(stack);
            UUID network = LogisticallyLinkedBlockItem.networkFromStack(panelItem);
            if (mixed.addPanel(slot, network)) {
                if (level instanceof ServerLevel serverLevel) {
                    PacketDistributor.sendToPlayersTrackingChunk(
                            serverLevel,
                            new ChunkPos(pos),
                            ThermalGaugeHostPayload.create(mixed)
                    );
                }

                player.displayClientMessage(
                        CreateLang.translateDirect("logistically_linked.connected"),
                        true
                );
                level.playSound(
                        null,
                        pos,
                        state.getSoundType(level, pos, player).getPlaceSound(),
                        SoundSource.BLOCKS
                );

                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
        }

        consume(event, level);
    }

    private static void consume(
            PlayerInteractEvent.RightClickBlock event,
            Level level
    ) {
        event.setCancellationResult(
                InteractionResult.sidedSuccess(level.isClientSide)
        );
        event.setCanceled(true);
    }
}
