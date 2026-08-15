package io.hxneyw.repo.content.blocks.resonantheatinjector;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResonantHeatInjectorBlock
        extends DirectionalKineticBlock
        implements IBE<ResonantHeatInjectorBlockEntity>,
        IWrenchable,
        ThermochemicalConnection {

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(4, 4, 4, 14, 14, 12),
            Block.box(2, 14, 2, 14, 16, 14),
            Block.box(14, 6, 6, 16, 10, 10)
    );
    private static final VoxelShape EAST_SHAPE = rotate90(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotate90(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotate90(SOUTH_SHAPE);

    public ResonantHeatInjectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    public static Direction inputSide(BlockState state) {
        return state.getValue(FACING).getClockWise();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return inputSide(state).getAxis();
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos position,
            BlockState state,
            Direction face
    ) {
        return face == inputSide(state);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        return face != inputSide(state);
    }

    @Override
    public BlockState getRotatedBlockState(
            BlockState originalState,
            Direction targetedFace
    ) {
        return originalState.setValue(
                FACING,
                originalState.getValue(FACING).getClockWise()
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        Direction preferredInput = findPreferredInputSide(context);
        if (preferredInput != null
                && (context.getPlayer() == null
                || !context.getPlayer().isShiftKeyDown())) {
            return stateForInputSide(preferredInput);
        }

        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    private @Nullable Direction findPreferredInputSide(
            BlockPlaceContext context
    ) {
        BlockPos placementPos = context.getClickedPos();
        Direction clickedInput = context.getClickedFace().getOpposite();

        if (clickedInput.getAxis() != Axis.Y
                && hasValidInputNeighbour(
                context,
                placementPos,
                clickedInput
        )) {
            return clickedInput;
        }

        Direction preferred = null;
        Direction[] horizontal = {
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        };

        for (Direction side : horizontal) {
            if (!hasValidInputNeighbour(context, placementPos, side)) {
                continue;
            }

            if (preferred != null
                    && preferred.getAxis() != side.getAxis()) {
                return null;
            }

            preferred = side;
        }

        return preferred;
    }

    private boolean hasValidInputNeighbour(
            BlockPlaceContext context,
            BlockPos placementPos,
            Direction side
    ) {
        BlockPos neighbourPos = placementPos.relative(side);
        BlockState neighbourState = context.getLevel()
                .getBlockState(neighbourPos);

        if (!(neighbourState.getBlock() instanceof IRotate rotate)
                || !rotate.hasShaftTowards(
                context.getLevel(),
                neighbourPos,
                neighbourState,
                side.getOpposite()
        )) {
            return false;
        }

        if (!(neighbourState.getBlock()
                instanceof ThermochemicalConnection thermochemical)) {
            return false;
        }

        return !thermochemical.doesNotHaveThermochemicalConnection(
                neighbourState,
                side.getOpposite()
        );
    }

    private BlockState stateForInputSide(Direction inputSide) {
        return defaultBlockState().setValue(
                FACING,
                inputSide.getCounterClockWise()
        );
    }

    @Override
    public @NotNull Class<ResonantHeatInjectorBlockEntity> getBlockEntityClass() {
        return ResonantHeatInjectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ResonantHeatInjectorBlockEntity> getBlockEntityType() {
        return AllBlockEntities.RESONANT_HEAT_INJECTOR.get();
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    protected boolean isPathfindable(
            @NotNull BlockState state,
            @NotNull PathComputationType pathComputationType
    ) {
        return false;
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction(
            @NotNull BlockState state
    ) {
        return PushReaction.PUSH_ONLY;
    }

    private static VoxelShape shapeForFacing(Direction facing) {
        return switch (facing) {
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    private static VoxelShape rotate90(VoxelShape source) {
        VoxelShape[] rotated = {Shapes.empty()};
        source.toAabbs().forEach(box -> rotated[0] = Shapes.join(
                rotated[0],
                Shapes.box(
                        1.0D - box.maxZ,
                        box.minY,
                        box.minX,
                        1.0D - box.minZ,
                        box.maxY,
                        box.maxX
                ),
                BooleanOp.OR
        ));
        return rotated[0];
    }
}
