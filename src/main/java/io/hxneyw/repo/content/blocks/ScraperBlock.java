package io.hxneyw.repo.content.blocks;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import javax.annotation.ParametersAreNonnullByDefault;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllVoxelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ScraperBlock extends BaseEntityBlock {
    public static final MapCodec<ScraperBlock> CODEC = simpleCodec(ScraperBlock::new);
    public ScraperBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection());
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return AllBlocks.BELT.has(below) && below.getValue(BeltBlock.SLOPE) == BeltSlope.HORIZONTAL;
    }

    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
            BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return AllVoxelShapes.Scraper.getShape(state.getValue(HorizontalDirectionalBlock.FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level,
            BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return AllVoxelShapes.Scraper.getShape(state.getValue(HorizontalDirectionalBlock.FACING));
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ScraperBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, AllBlockEntities.SCRAPER.get(), ScraperBlockEntity::tick);
    }
}
