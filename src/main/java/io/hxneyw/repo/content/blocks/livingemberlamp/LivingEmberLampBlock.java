package io.hxneyw.repo.content.blocks.livingemberlamp;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import io.hxneyw.repo.content.blocks.WrenchInteractionHelper;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LivingEmberLampBlock
        extends HorizontalDirectionalBlock
        implements EntityBlock, IWrenchable {

    public static final MapCodec<LivingEmberLampBlock> CODEC =
            simpleCodec(LivingEmberLampBlock::new);

    public static final IntegerProperty LIGHT_LEVEL =
            IntegerProperty.create("light_level", 0, 15);

    private static final VoxelShape SHAPE = Shapes.or(
            
            Block.box(2, 0, 2, 3, 2, 3),
            Block.box(13, 0, 2, 14, 2, 3),
            Block.box(3, 0, 3, 13, 1, 13),
            Block.box(2, 0, 13, 3, 2, 14),
            Block.box(13, 0, 13, 14, 2, 14),
            Block.box(3, 1, 2, 4, 6, 3),
            Block.box(12, 1, 2, 13, 6, 3),
            Block.box(2, 1, 3, 3, 6, 4),
            Block.box(13, 1, 3, 14, 6, 4),
            Block.box(2, 1, 12, 3, 6, 13),
            Block.box(13, 1, 12, 14, 6, 13),
            Block.box(3, 1, 13, 4, 6, 14),
            Block.box(12, 1, 13, 13, 6, 14),
            Block.box(3, 3, 3, 4, 14, 4),
            Block.box(12, 3, 3, 13, 14, 4),
            Block.box(3, 3, 12, 4, 14, 13),
            Block.box(12, 3, 12, 13, 14, 13),
            Block.box(4, 4, 4, 12, 5, 12),
            Block.box(4, 5, 4, 12, 6, 5),
            Block.box(4, 5, 5, 5, 6, 12),
            Block.box(11, 5, 5, 12, 6, 12),
            Block.box(5, 5, 11, 11, 6, 12),
            Block.box(4, 6, 4, 5, 16, 5),
            Block.box(11, 6, 4, 12, 16, 5),
            Block.box(4, 6, 11, 5, 16, 12),
            Block.box(11, 6, 11, 12, 16, 12),
            Block.box(5, 12, 4, 11, 15, 5),
            Block.box(4, 12, 5, 5, 15, 11),
            Block.box(11, 12, 5, 12, 15, 11),
            Block.box(5, 12, 11, 11, 15, 12),
            Block.box(3, 13, 2, 5, 14, 3),
            Block.box(11, 13, 2, 13, 14, 3),
            Block.box(2, 13, 3, 3, 14, 5),
            Block.box(4, 13, 3, 12, 14, 4),
            Block.box(13, 13, 3, 14, 14, 5),
            Block.box(3, 13, 4, 4, 14, 12),
            Block.box(12, 13, 4, 13, 14, 12),
            Block.box(5, 13, 5, 11, 15, 11),
            Block.box(2, 13, 11, 3, 14, 13),
            Block.box(13, 13, 11, 14, 14, 13),
            Block.box(4, 13, 12, 12, 14, 13),
            Block.box(3, 13, 13, 5, 14, 14),
            Block.box(11, 13, 13, 13, 14, 14),
            Block.box(4, 14, 3, 5, 15, 4),
            Block.box(11, 14, 3, 12, 15, 4),
            Block.box(3, 14, 4, 4, 15, 5),
            Block.box(12, 14, 4, 13, 15, 5),
            Block.box(3, 14, 11, 4, 15, 12),
            Block.box(12, 14, 11, 13, 15, 12),
            Block.box(4, 14, 12, 5, 15, 13),
            Block.box(11, 14, 12, 12, 15, 13),
            Block.box(5, 15, 5, 6, 16, 6),
            Block.box(10, 15, 5, 11, 16, 6),
            Block.box(6, 15, 6, 10, 16, 10),
            Block.box(5, 15, 10, 6, 16, 11),
            Block.box(10, 15, 10, 11, 16, 11)
    ).optimize();

    public LivingEmberLampBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LIGHT_LEVEL, 0)
        );
    }

    @Override
    protected @NotNull MapCodec<
            ? extends HorizontalDirectionalBlock
            > codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
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
        builder.add(FACING, LIGHT_LEVEL);
    }

    @Override
    public @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return SHAPE;
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
        ItemInteractionResult wrenchResult =
                WrenchInteractionHelper.handle(
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

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @SuppressWarnings("DuplicatedCode")
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

    @SuppressWarnings("DuplicatedCode")
    @Override
    public @NotNull BlockState rotate(
            @NotNull BlockState state,
            @NotNull Rotation rotation
    ) {
        return state.setValue(
                FACING,
                rotation.rotate(state.getValue(FACING))
        );
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public @NotNull BlockState mirror(
            @NotNull BlockState state,
            @NotNull Mirror mirror
    ) {
        return state.setValue(
                FACING,
                mirror.mirror(state.getValue(FACING))
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        return new LivingEmberLampBlockEntity(pos, state);
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

        if (level.getBlockEntity(pos)
                instanceof LivingEmberLampBlockEntity lamp) {
            LivingEmberLampItem.getLink(stack).ifPresent(link ->
                    lamp.setLinkedFurnace(
                            link.position(),
                            link.dimension(),
                            link.furnaceIdentity()
                    )
            );
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        if (level.isClientSide
                || type != AllBlockEntities.LIVING_EMBER_LAMP.get()) {
            return null;
        }

        return (tickerLevel, tickerPos, tickerState, blockEntity) ->
                LivingEmberLampBlockEntity.serverTick(
                        tickerLevel,
                        tickerPos,
                        tickerState,
                        (LivingEmberLampBlockEntity) blockEntity
                );
    }
}