package io.hxneyw.repo.content.blocks.catalystbed;

import io.hxneyw.repo.content.registry.AllModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public final class CatalystBedBlock extends Block {

    public static final BooleanProperty CONNECTED =
            BooleanProperty.create("connected");

    private static final VoxelShape OUTLINE_SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(2.0D, 2.0D, 2.0D, 14.0D, 11.0D, 14.0D),
            Block.box(1.0D, 2.0D, 1.0D, 2.0D, 11.0D, 2.0D),
            Block.box(14.0D, 2.0D, 1.0D, 15.0D, 11.0D, 2.0D),
            Block.box(1.0D, 2.0D, 14.0D, 2.0D, 11.0D, 15.0D),
            Block.box(14.0D, 2.0D, 14.0D, 15.0D, 11.0D, 15.0D),
            Block.box(2.0D, 9.0D, 1.0D, 14.0D, 11.0D, 2.0D),
            Block.box(2.0D, 5.0D, 1.0D, 14.0D, 7.0D, 2.0D),
            Block.box(2.0D, 9.0D, 14.0D, 14.0D, 11.0D, 15.0D),
            Block.box(2.0D, 5.0D, 14.0D, 14.0D, 7.0D, 15.0D),
            Block.box(1.0D, 5.0D, 2.0D, 2.0D, 7.0D, 14.0D),
            Block.box(1.0D, 9.0D, 2.0D, 2.0D, 11.0D, 14.0D),
            Block.box(14.0D, 9.0D, 2.0D, 15.0D, 11.0D, 14.0D),
            Block.box(14.0D, 5.0D, 2.0D, 15.0D, 7.0D, 14.0D),
            Block.box(14.0D, 7.0D, 7.0D, 15.0D, 9.0D, 9.0D),
            Block.box(14.0D, 2.0D, 7.0D, 15.0D, 5.0D, 9.0D),
            Block.box(1.0D, 2.0D, 7.0D, 2.0D, 5.0D, 9.0D),
            Block.box(1.0D, 2.0D, 11.0D, 2.0D, 5.0D, 12.0D),
            Block.box(1.0D, 7.0D, 7.0D, 2.0D, 9.0D, 9.0D),
            Block.box(1.0D, 7.0D, 11.0D, 2.0D, 9.0D, 12.0D),
            Block.box(1.0D, 7.0D, 4.0D, 2.0D, 9.0D, 5.0D),
            Block.box(1.0D, 2.0D, 4.0D, 2.0D, 5.0D, 5.0D),
            Block.box(14.0D, 7.0D, 11.0D, 15.0D, 9.0D, 12.0D),
            Block.box(14.0D, 2.0D, 11.0D, 15.0D, 5.0D, 12.0D),
            Block.box(14.0D, 2.0D, 4.0D, 15.0D, 5.0D, 5.0D),
            Block.box(14.0D, 7.0D, 4.0D, 15.0D, 9.0D, 5.0D),
            Block.box(7.0D, 7.0D, 14.0D, 9.0D, 9.0D, 15.0D),
            Block.box(11.0D, 7.0D, 14.0D, 12.0D, 9.0D, 15.0D),
            Block.box(7.0D, 2.0D, 14.0D, 9.0D, 5.0D, 15.0D),
            Block.box(11.0D, 2.0D, 14.0D, 12.0D, 5.0D, 15.0D),
            Block.box(4.0D, 2.0D, 14.0D, 5.0D, 5.0D, 15.0D),
            Block.box(4.0D, 7.0D, 14.0D, 5.0D, 9.0D, 15.0D),
            Block.box(11.0D, 7.0D, 1.0D, 12.0D, 9.0D, 2.0D),
            Block.box(11.0D, 2.0D, 1.0D, 12.0D, 5.0D, 2.0D),
            Block.box(4.0D, 7.0D, 1.0D, 5.0D, 9.0D, 2.0D),
            Block.box(4.0D, 2.0D, 1.0D, 5.0D, 5.0D, 2.0D),
            Block.box(7.0D, 7.0D, 1.0D, 9.0D, 9.0D, 2.0D),
            Block.box(7.0D, 2.0D, 1.0D, 9.0D, 5.0D, 2.0D),
            Block.box(2.0D, 11.0D, 2.0D, 14.0D, 13.0D, 14.0D),
            Block.box(3.0D, 13.0D, 3.0D, 13.0D, 15.0D, 13.0D),
            Block.box(3.0D, 15.0D, 3.0D, 13.0D, 16.0D, 5.5D),
            Block.box(3.0D, 15.0D, 10.5D, 13.0D, 16.0D, 13.0D),
            Block.box(3.0D, 15.0D, 5.5D, 5.5D, 16.0D, 10.5D),
            Block.box(10.5D, 15.0D, 5.5D, 13.0D, 16.0D, 10.5D),
            Block.box(5.5D, 15.0D, 5.5D, 10.5D, 15.25D, 10.5D)
    );

    private static final VoxelShape COLLISION_SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(1.0D, 2.0D, 1.0D, 15.0D, 11.0D, 15.0D),
            Block.box(2.0D, 11.0D, 2.0D, 14.0D, 13.0D, 14.0D),
            Block.box(3.0D, 13.0D, 3.0D, 13.0D, 15.0D, 13.0D),
            Block.box(3.0D, 15.0D, 3.0D, 13.0D, 16.0D, 13.0D)
    );

    public CatalystBedBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(CONNECTED, false)
        );
    }

    public static boolean hasChamberAbove(
            LevelReader level,
            BlockPos position
    ) {
        return level.getBlockState(position.above())
                .is(AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get());
    }

    @Override
    public @NotNull BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        return defaultBlockState().setValue(
                CONNECTED,
                hasChamberAbove(
                        context.getLevel(),
                        context.getClickedPos()
                )
        );
    }

    @Override
    public @NotNull BlockState updateShape(
            @NotNull BlockState state,
            @NotNull Direction direction,
            @NotNull BlockState neighbourState,
            @NotNull LevelAccessor level,
            @NotNull BlockPos position,
            @NotNull BlockPos neighbourPosition
    ) {
        if (direction == Direction.UP) {
            return state.setValue(
                    CONNECTED,
                    neighbourState.is(
                            AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()
                    )
            );
        }
        return super.updateShape(
                state,
                direction,
                neighbourState,
                level,
                position,
                neighbourPosition
        );
    }

    @Override
    public @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        return OUTLINE_SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        return COLLISION_SHAPE;
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position
    ) {
        return COLLISION_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(
            @NotNull StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTED);
    }
}
