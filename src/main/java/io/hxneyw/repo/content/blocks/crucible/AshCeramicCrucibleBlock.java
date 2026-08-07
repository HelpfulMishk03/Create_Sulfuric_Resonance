package io.hxneyw.repo.content.blocks.crucible;

import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class AshCeramicCrucibleBlock extends BasinBlock {


    private static final VoxelShape SHAPE = Shapes.or(
            // Inner floor
            Block.box(2, 0, 2, 14, 4, 14),

// Main outer bowl walls — end at pixel 14
            Block.box(0, 2, 0, 2, 14, 16),
            Block.box(14, 2, 0, 16, 14, 16),
            Block.box(2, 2, 0, 14, 14, 2),
            Block.box(2, 2, 14, 14, 14, 16),

// Raised rim — pixel 14 through 15
            Block.box(0, 14, 0, 16, 15, 2),
            Block.box(0, 14, 14, 16, 15, 16),
            Block.box(0, 14, 2, 2, 15, 14),
            Block.box(14, 14, 2, 16, 15, 14),
            // Four feet
            Block.box(0, 0, 0, 2, 2, 2),
            Block.box(14, 0, 0, 16, 2, 2),
            Block.box(0, 0, 14, 2, 2, 16),
            Block.box(14, 0, 14, 16, 2, 16)
    );

    public AshCeramicCrucibleBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends BasinBlockEntity> getBlockEntityType() {
        return AllBlockEntities.ASH_CERAMIC_CRUCIBLE.get();
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                        @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                 @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
}