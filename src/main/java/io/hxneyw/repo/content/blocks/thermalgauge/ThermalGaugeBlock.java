package io.hxneyw.repo.content.blocks.thermalgauge;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockItem;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.WrenchInteractionHelper;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import io.hxneyw.repo.content.items.ThermalGaugeItem;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.network.PacketDistributor;

public class ThermalGaugeBlock
        extends FaceAttachedHorizontalDirectionalBlock
        implements EntityBlock, IWrenchable {

    public static final MapCodec<ThermalGaugeBlock> CODEC =
            simpleCodec(ThermalGaugeBlock::new);

    private static final VoxelShape FLOOR_SHAPE =
            Block.box(4.0, 0.0, 4.0, 12.0, 2.1, 12.0);
    private static final VoxelShape CEILING_SHAPE =
            Block.box(4.0, 13.9, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape NORTH_SHAPE =
            Block.box(4.0, 4.0, 13.9, 12.0, 12.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE =
            Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 2.1);
    private static final VoxelShape EAST_SHAPE =
            Block.box(0.0, 4.0, 4.0, 2.1, 12.0, 12.0);
    private static final VoxelShape WEST_SHAPE =
            Block.box(13.9, 4.0, 4.0, 16.0, 12.0, 12.0);

    public ThermalGaugeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<
            ? extends FaceAttachedHorizontalDirectionalBlock
            > codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(
            @NotNull StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(builder.add(FACE, FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        if (state.getValue(FACE) == AttachFace.FLOOR) {
            state = state.setValue(
                    FACING,
                    state.getValue(FACING).getOpposite()
            );
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState existingState = level.getBlockState(pos);
        Vec3 location = context.getClickLocation();

        if (existingState.is(this)
                && level.getBlockEntity(pos) instanceof ThermalGaugeBlockEntity gauge) {
            PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                    pos,
                    existingState,
                    location
            );
            ItemStack stack = context.getItemInHand();
            UUID networkId = ThermalRelaySwitchItem.getNetworkId(stack);
            ThermalRelaySwitchItem.FurnaceLink link =
                    ThermalRelaySwitchItem.getLinkedFurnace(stack);

            if (level.isClientSide) {
                if (!gauge.isOccupied(slot)) {
                    gauge.addGauge(
                            slot,
                            networkId,
                            link
                    );
                }
            } else {
                Player player = context.getPlayer();

                if (gauge.addGauge(slot, networkId, link)
                        && player != null
                        && !player.isCreative()) {
                    stack.shrink(1);

                    if (stack.isEmpty()) {
                        player.setItemInHand(
                                context.getHand(),
                                ItemStack.EMPTY
                        );
                    }
                }
            }

            return existingState;
        }

        return state;
    }

    @Override
    public boolean canBeReplaced(
            @NotNull BlockState state,
            @NotNull BlockPlaceContext context
    ) {
        if (!(context.getItemInHand().getItem() instanceof ThermalGaugeItem)) {
            return false;
        }

        BlockPos pos = context.getClickedPos();
        PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                pos,
                state,
                context.getClickLocation()
        );

        if (!(context.getLevel().getBlockEntity(pos)
                instanceof ThermalGaugeBlockEntity gauge)) {
            return false;
        }

        return !gauge.isOccupied(slot);
    }

    @Override
    public boolean canSurvive(
            @NotNull BlockState state,
            @NotNull LevelReader level,
            @NotNull BlockPos pos
    ) {
        Direction connectedDirection = getConnectedDirection(state);
        BlockPos supportPos = pos.relative(connectedDirection.getOpposite());
        return !level.getBlockState(supportPos)
                .getCollisionShape(level, supportPos)
                .isEmpty();
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        if (level.getBlockEntity(pos) instanceof ThermalGaugeBlockEntity gauge
                && gauge.activeGaugeCount() > 0) {
            return gauge.getShape();
        }

        AttachFace face = state.getValue(FACE);

        if (face == AttachFace.FLOOR) {
            return FLOOR_SHAPE;
        }

        if (face == AttachFace.CEILING) {
            return CEILING_SHAPE;
        }

        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> FLOOR_SHAPE;
        };
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        if (context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() == null) {
            return getShape(state, level, pos, context);
        }

        return Shapes.empty();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        return new ThermalGaugeBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState newState,
            boolean isMoving
    ) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public void setPlacedBy(
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @Nullable LivingEntity placer,
            @NotNull ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);

        Vec3 placementClick =
                ThermalGaugeItem.getPlacementClickLocation();

        PanelSlot slot = placementClick != null
                ? FactoryPanelBlock.getTargetedSlot(
                        pos,
                        state,
                        placementClick
                )
                : getPlacementSlot(
                        pos,
                        state,
                        placer
                );

        UUID networkId =
                ThermalRelaySwitchItem.getNetworkId(stack);

        ThermalRelaySwitchItem.FurnaceLink link =
                ThermalRelaySwitchItem.getLinkedFurnace(stack);

        if (level.getBlockEntity(pos)
                instanceof ThermalGaugeBlockEntity gauge
                && !gauge.isOccupied(slot)) {
            gauge.addGauge(
                    slot,
                    networkId,
                    link
            );
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack,
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit
    ) {
        if (AllBlocks.FACTORY_GAUGE.isIn(stack)) {
            if (!(level.getBlockEntity(pos) instanceof ThermalGaugeBlockEntity gauge)) {
                return ItemInteractionResult.FAIL;
            }

            PanelSlot factorySlot = FactoryPanelBlock.getTargetedSlot(
                    pos,
                    state,
                    hit.getLocation()
            );

            if (gauge.isOccupied(factorySlot)) {
                return ItemInteractionResult.FAIL;
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
                return ItemInteractionResult.SUCCESS;
            }

            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            ThermalGaugeBlockEntity mixed = gauge.convertToFactoryHost();
            if (mixed == null || mixed.isOccupied(factorySlot)) {
                return ItemInteractionResult.FAIL;
            }

            if (level instanceof ServerLevel serverLevel) {
                PacketDistributor.sendToPlayersTrackingChunk(
                        serverLevel,
                        new ChunkPos(pos),
                        new ThermalGaugeHostPayload(pos.immutable())
                );
            }

            ItemStack panelItem = FactoryPanelBlockItem.fixCtrlCopiedStack(stack);
            UUID network = LogisticallyLinkedBlockItem.networkFromStack(panelItem);
            if (!mixed.addPanel(factorySlot, network)) {
                return ItemInteractionResult.FAIL;
            }

            player.displayClientMessage(
                    CreateLang.translateDirect("logistically_linked.connected"),
                    true
            );
            level.playSound(
                    null,
                    pos,
                    state.getSoundType().getPlaceSound(),
                    SoundSource.BLOCKS
            );

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            return ItemInteractionResult.SUCCESS;
        }

        ItemInteractionResult wrenchResult = WrenchInteractionHelper.handle(
                this,
                stack,
                state,
                player,
                hand,
                hit
        );

        if (wrenchResult != null) {
            return wrenchResult;
        }

        if (!(level.getBlockEntity(pos) instanceof ThermalGaugeBlockEntity gauge)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                pos,
                state,
                hit.getLocation()
        );

        if (stack.getItem() instanceof ThermalGaugeItem) {
            if (!level.isClientSide && !gauge.isOccupied(slot)) {
                UUID networkId = ThermalRelaySwitchItem.getNetworkId(stack);
                ThermalRelaySwitchItem.FurnaceLink link =
                        ThermalRelaySwitchItem.getLinkedFurnace(stack);

                if (gauge.addGauge(slot, networkId, link)) {
                    level.playSound(
                            null,
                            pos,
                            soundType.getPlaceSound(),
                            SoundSource.BLOCKS,
                            (soundType.getVolume() + 1.0F) / 2.0F,
                            soundType.getPitch() * 0.8F
                    );

                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                }
            }

            return ItemInteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && gauge.hasGauge(slot)) {
                gauge.clearConnection(slot);
                player.displayClientMessage(
                        Component.translatable(
                                "message.sulfuricresonance.thermal_gauge.network_removed"
                        ),
                        true
                );
            }

            return ItemInteractionResult.SUCCESS;
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

            return ItemInteractionResult.SUCCESS;
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

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof ThermalGaugeBlockEntity gauge)) {
            return InteractionResult.PASS;
        }

        PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                pos,
                state,
                hit.getLocation()
        );

        if (!gauge.hasGauge(slot)) {
            return InteractionResult.PASS;
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

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

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

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean onDestroyedByPlayer(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            boolean willHarvest,
            @NotNull FluidState fluid
    ) {
        if (level.getBlockEntity(pos) instanceof ThermalGaugeBlockEntity gauge
                && gauge.activePanels() > 1) {
            PanelSlot slot = getTargetedSlotFromPlayer(pos, state, player);

            if (gauge.hasGauge(slot)) {
                ItemStack drop = gauge.createItemStack(slot);

                if (gauge.removeGauge(slot)) {
                    if (!player.isCreative()) {
                        popResource(level, pos, drop);
                    }
                    return false;
                }
            }
        }

        return super.onDestroyedByPlayer(
                state,
                level,
                pos,
                player,
                willHarvest,
                fluid
        );
    }

    @Override
    public @NotNull InteractionResult onSneakWrenched(
            @NotNull BlockState state,
            @NotNull UseOnContext context
    ) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null
                || !(level.getBlockEntity(pos) instanceof ThermalGaugeBlockEntity gauge)) {
            return IWrenchable.super.onSneakWrenched(state, context);
        }

        PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                pos,
                state,
                context.getClickLocation()
        );

        if (!gauge.hasGauge(slot)) {
            return IWrenchable.super.onSneakWrenched(state, context);
        }

        if (!level.isClientSide) {
            ItemStack drop = gauge.createItemStack(slot);

            if (gauge.removeGauge(slot)) {
                player.getInventory().placeItemBackInInventory(drop);

                IWrenchable.playRemoveSound(level, pos);

                if (gauge.activePanels() == 0) {
                    level.destroyBlock(pos, false);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        if (type != AllBlockEntities.THERMAL_GAUGE.get()) {
            return null;
        }

        return (tickerLevel, ignoredPos, tickerState, blockEntity) ->
                ((ThermalGaugeBlockEntity) blockEntity).tick();
    }

    private static PanelSlot getPlacementSlot(
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer
    ) {
        if (placer == null) {
            return PanelSlot.BOTTOM_LEFT;
        }

        double range = placer.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1.0D;
        HitResult hitResult = placer.pick(range, 1.0F, false);
        Vec3 location = hitResult.getLocation();
        return FactoryPanelBlock.getTargetedSlot(pos, state, location);
    }

    private static PanelSlot getTargetedSlotFromPlayer(
            BlockPos pos,
            BlockState state,
            Player player
    ) {
        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1.0D;
        HitResult hitResult = player.pick(range, 1.0F, false);
        return FactoryPanelBlock.getTargetedSlot(
                pos,
                state,
                hitResult.getLocation()
        );
    }
}
