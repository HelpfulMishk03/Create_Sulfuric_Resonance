package io.hxneyw.repo.content.blocks.processmonitor;

import com.mojang.serialization.MapCodec;
import io.hxneyw.repo.content.items.ProcessGaugeItem;
import io.hxneyw.repo.content.process.ProcessMonitorLinking;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProcessMonitorBlock extends FaceAttachedHorizontalDirectionalBlock
        implements EntityBlock {

    public static final MapCodec<ProcessMonitorBlock> CODEC =
            simpleCodec(ProcessMonitorBlock::new);

    private static final VoxelShape NORTH_SHAPE =
            Block.box(1.5, 1.5, 9.5, 14.5, 16.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE =
            Block.box(1.5, 1.5, 0.0, 14.5, 16.0, 6.5);
    private static final VoxelShape EAST_SHAPE =
            Block.box(0.0, 1.5, 1.5, 6.5, 16.0, 14.5);
    private static final VoxelShape WEST_SHAPE =
            Block.box(9.5, 1.5, 1.5, 16.0, 16.0, 14.5);

    private static final VoxelShape FLOOR_BODY_SHAPE =
            Block.box(1.7, 0.0, 1.7, 14.3, 5.8, 14.3);

    private static final VoxelShape NORTH_ANTENNA_SHAPE =
            Block.box(11.65, 13.65, 12.45, 12.95, 18.52, 14.05);
    private static final VoxelShape SOUTH_ANTENNA_SHAPE =
            Block.box(3.05, 13.65, 1.95, 4.35, 18.52, 3.55);
    private static final VoxelShape EAST_ANTENNA_SHAPE =
            Block.box(1.95, 13.65, 11.65, 3.55, 18.52, 12.95);
    private static final VoxelShape WEST_ANTENNA_SHAPE =
            Block.box(12.45, 13.65, 3.05, 14.05, 18.52, 4.35);

    private static final VoxelShape FLOOR_NORTH_ANTENNA_SHAPE =
            Block.box(11.65, 2.75, 12.85, 12.95, 7.62, 14.45);
    private static final VoxelShape FLOOR_SOUTH_ANTENNA_SHAPE =
            Block.box(3.05, 2.75, 1.55, 4.35, 7.62, 3.15);
    private static final VoxelShape FLOOR_EAST_ANTENNA_SHAPE =
            Block.box(1.55, 2.75, 11.65, 3.15, 7.62, 12.95);
    private static final VoxelShape FLOOR_WEST_ANTENNA_SHAPE =
            Block.box(12.85, 2.75, 3.05, 14.45, 7.62, 4.35);

    public ProcessMonitorBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(FACE, AttachFace.WALL)
                        .setValue(FACING, Direction.NORTH)
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
        super.createBlockStateDefinition(builder.add(FACE, FACING));
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
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack,
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof ProcessMonitorBlockEntity monitor)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.getItem() instanceof ProcessGaugeItem) {
            if (!level.isClientSide) {
                ProcessMonitorLinking.cancel(player);
                ProcessGaugeItem.bindToMonitor(stack, level, monitor, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

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
        if (!(level.getBlockEntity(pos) instanceof ProcessMonitorBlockEntity monitor)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            ProcessMonitorLinking.cancel(player);
            monitor.cycleSelectedChannel();

            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance.process_monitor.selected_channel_state",
                            monitor.getSelectedChannel() + 1,
                            monitor.getChannelDisplayLabel(monitor.getSelectedChannel())
                    ),
                    true
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return Shapes.or(getBodyShape(state), getAntennaShape(state));
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return getBodyShape(state);
    }

    private static VoxelShape getBodyShape(BlockState state) {
        if (state.getValue(FACE) == AttachFace.FLOOR) {
            return FLOOR_BODY_SHAPE;
        }

        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    private static VoxelShape getAntennaShape(BlockState state) {
        if (state.getValue(FACE) == AttachFace.FLOOR) {
            return switch (state.getValue(FACING)) {
                case SOUTH -> FLOOR_SOUTH_ANTENNA_SHAPE;
                case EAST -> FLOOR_EAST_ANTENNA_SHAPE;
                case WEST -> FLOOR_WEST_ANTENNA_SHAPE;
                default -> FLOOR_NORTH_ANTENNA_SHAPE;
            };
        }

        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_ANTENNA_SHAPE;
            case EAST -> EAST_ANTENNA_SHAPE;
            case WEST -> WEST_ANTENNA_SHAPE;
            default -> NORTH_ANTENNA_SHAPE;
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        return new ProcessMonitorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        if (type != AllBlockEntities.PROCESS_MONITOR.get()) {
            return null;
        }

        return (tickerLevel, tickerPos, tickerState, blockEntity) ->
                ProcessMonitorBlockEntity.tick(
                        tickerLevel,
                        (ProcessMonitorBlockEntity) blockEntity
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
