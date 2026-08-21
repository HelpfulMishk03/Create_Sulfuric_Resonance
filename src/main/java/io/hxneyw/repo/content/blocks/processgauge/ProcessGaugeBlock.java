package io.hxneyw.repo.content.blocks.processgauge;

import com.mojang.serialization.MapCodec;
import io.hxneyw.repo.content.items.ProcessGaugeItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ProcessGaugeBlock extends FaceAttachedHorizontalDirectionalBlock
        implements EntityBlock {

    public static final MapCodec<ProcessGaugeBlock> CODEC =
            simpleCodec(ProcessGaugeBlock::new);
    public static final BooleanProperty POWERED =
            BooleanProperty.create("powered");

    private static final VoxelShape NORTH_SHAPE =
            Block.box(1.5, 0.0, 9.0, 14.5, 13.5, 16.0);
    private static final VoxelShape SOUTH_SHAPE =
            Block.box(1.5, 0.0, 0.0, 14.5, 13.5, 7.0);
    private static final VoxelShape EAST_SHAPE =
            Block.box(0.0, 0.0, 1.5, 7.0, 13.5, 14.5);
    private static final VoxelShape WEST_SHAPE =
            Block.box(9.0, 0.0, 1.5, 16.0, 13.5, 14.5);

    private static final VoxelShape FLOOR_SHAPE =
            Block.box(1.5, 0.0, 1.4, 14.5, 6.0, 14.6);

    public ProcessGaugeBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(FACE, AttachFace.WALL)
                        .setValue(FACING, Direction.NORTH)
                        .setValue(POWERED, false)
        );
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
        super.createBlockStateDefinition(builder.add(FACE, FACING, POWERED));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null || state.getValue(FACE) == AttachFace.CEILING) {
            return null;
        }

        if (state.getValue(FACE) == AttachFace.FLOOR) {
            state = state.setValue(FACING, state.getValue(FACING).getOpposite());
        }

        state = state.setValue(POWERED, false);
        return canSurvive(state, context.getLevel(), context.getClickedPos())
                ? state
                : null;
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
    public void setPlacedBy(
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @Nullable LivingEntity placer,
            @NotNull ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof ProcessGaugeBlockEntity gauge) {
            gauge.configureFromItem(ProcessGaugeItem.getMonitorReference(stack));
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
        if (stack.getItem() instanceof BlockItem) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
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
        if (!(level.getBlockEntity(pos) instanceof ProcessGaugeBlockEntity gauge)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            gauge.cycleSelectedChannel();
            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance.process_gauge.channel_state",
                            gauge.getSelectedChannelNumber(),
                            gauge.getObservedDisplayLabel()
                    ),
                    true
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean isProcessGaugeOutputDirection(
            @NotNull BlockState state,
            @NotNull Direction direction
    ) {
        if (state.getValue(FACE) == AttachFace.FLOOR) {
            return direction == Direction.UP
                    || direction == state.getValue(FACING).getOpposite();
        }

        return direction == Direction.UP
                || direction == state.getValue(FACING);
    }

    @Override
    protected boolean isSignalSource(
            @NotNull BlockState state
    ) {
        return true;
    }

    @Override
    protected int getSignal(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull Direction direction
    ) {
        if (!isProcessGaugeOutputDirection(state, direction)) {
            return 0;
        }

        if (level.getBlockEntity(pos) instanceof ProcessGaugeBlockEntity gauge) {
            return gauge.getRedstoneSignal();
        }

        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull Direction direction
    ) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public void onRemove(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState newState,
            boolean isMoving
    ) {
        if (!state.is(newState.getBlock())) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        if (state.getValue(FACE) == AttachFace.FLOOR) {
            return FLOOR_SHAPE;
        }

        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        return new ProcessGaugeBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        if (type != AllBlockEntities.PROCESS_GAUGE.get()) {
            return null;
        }

        return (tickerLevel, tickerPos, tickerState, blockEntity) ->
                ProcessGaugeBlockEntity.tick(
                        tickerLevel,
                        (ProcessGaugeBlockEntity) blockEntity
                );
    }

    @Override
    public @NotNull BlockState rotate(
            @NotNull BlockState state,
            @NotNull Rotation rotation
    ) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(
            @NotNull BlockState state,
            @NotNull Mirror mirror
    ) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
