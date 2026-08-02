package io.hxneyw.repo.content.blocks.thermalrelay;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermalRelaySwitchBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<ThermalRelaySwitchBlock> CODEC =
            simpleCodec(ThermalRelaySwitchBlock::new);

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 4.0, 14.0, 1.0, 12.0),
            Block.box(3.0, 1.0, 5.0, 13.0, 2.0, 11.0),

            Block.box(4.0, 2.0, 6.0, 12.0, 4.0, 10.0),

            Block.box(4.0, 2.0, 4.3, 12.0, 7.0, 6.0),
            Block.box(4.0, 2.0, 10.0, 12.0, 7.0, 11.5),
            Block.box(3.0, 2.0, 6.0, 5.0, 7.5, 10.0),
            Block.box(11.0, 2.0, 6.0, 13.0, 7.5, 10.0),
            Block.box(4.0, 6.0, 5.0, 12.0, 7.5, 11.0),

            Block.box(6.0, 0.0, 0.0, 10.0, 1.0, 4.0),
            Block.box(6.0, 0.0, 12.0, 10.0, 1.0, 16.0),
            Block.box(1.0, 0.0, 6.0, 2.0, 1.0, 10.0),
            Block.box(14.0, 0.0, 6.0, 15.0, 1.0, 10.0)
    );

    private static final VoxelShape EAST_SHAPE = rotateClockwise(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateClockwise(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateClockwise(SOUTH_SHAPE);

    public ThermalRelaySwitchBlock(Properties properties) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return shapeFor(state);
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return shapeFor(state);
    }

    private static VoxelShape shapeFor(BlockState state) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    private static VoxelShape rotateClockwise(VoxelShape source) {
        VoxelShape[] result = {Shapes.empty()};

        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                result[0] = Shapes.or(
                        result[0],
                        Shapes.box(
                                1.0 - maxZ,
                                minY,
                                minX,
                                1.0 - minZ,
                                maxY,
                                maxX
                        )
                )
        );

        return result[0];
    }
}