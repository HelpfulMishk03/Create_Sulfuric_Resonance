package io.hxneyw.repo.content.blocks.thermalgauge;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import io.hxneyw.repo.content.blocks.WrenchInteractionHelper;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(builder.add(FACE, FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        BlockState state = super.getStateForPlacement(context);

        if (state != null && state.getValue(FACE) == AttachFace.FLOOR) {
            state = state.setValue(
                    FACING,
                    state.getValue(FACING).getOpposite()
            );
        }

        return state;
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
    public void setPlacedBy(
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @Nullable LivingEntity placer,
            @NotNull ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide) {
            return;
        }

        UUID networkId = ThermalRelaySwitchItem.getNetworkId(stack);
        ThermalRelaySwitchItem.FurnaceLink link =
                ThermalRelaySwitchItem.getLinkedFurnace(stack);

        if (link == null) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof ThermalGaugeBlockEntity gauge) {
            gauge.setConnection(
                    networkId != null ? networkId : link.furnaceIdentity(),
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

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                gauge.clearConnection();
                player.displayClientMessage(
                        Component.translatable(
                                "message.sulfuricresonance.thermal_gauge.network_removed"
                        ),
                        true
                );
            }

            return ItemInteractionResult.SUCCESS;
        }

        UUID networkId = gauge.getNetworkId();
        ThermalRelaySwitchItem.FurnaceLink link = gauge.getFurnaceLink();

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

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                gauge.clearConnection();
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
            Component message = gauge.isNetworkConnected()
                    ? Component.translatable(
                            "message.sulfuricresonance.thermal_gauge.reading",
                            gauge.getDisplayTemperature()
                    )
                    : Component.translatable(
                            "message.sulfuricresonance.thermal_gauge.no_network"
                    );
            player.displayClientMessage(message, true);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        if (level.isClientSide || type != AllBlockEntities.THERMAL_GAUGE.get()) {
            return null;
        }

        return (tickerLevel, ignoredPos, tickerState, blockEntity) ->
                ThermalGaugeBlockEntity.serverTick(
                        tickerLevel,
                        tickerState,
                        (ThermalGaugeBlockEntity) blockEntity
                );
    }
}
