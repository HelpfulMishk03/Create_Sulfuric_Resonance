package io.hxneyw.repo.content.blocks.rotaryleacher;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RotaryLeacherBlock extends DirectionalKineticBlock implements IBE<RotaryLeacherBlockEntity>, IProxyHoveringInformation, ICogWheel {
    private static final ThreadLocal<Boolean> REMOVING_PAIRED_HALF = ThreadLocal.withInitial(() -> false);
    public static final MapCodec<RotaryLeacherBlock> CODEC = simpleCodec(RotaryLeacherBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.create("half", DoubleBlockHalf.class);
    public static final DirectionProperty HORIZONTAL_FACING = DirectionProperty.create("horizontal_facing", Direction.Plane.HORIZONTAL);

    public RotaryLeacherBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.EAST).setValue(HALF, DoubleBlockHalf.LOWER).setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }

    public static Direction inputSide(BlockState state) { return state.getValue(HORIZONTAL_FACING).getClockWise(); }

    @Override
    public Axis getRotationAxis(BlockState state) { return Axis.Y; }

    @Override
    @SuppressWarnings("unused")
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF, HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos upperPos = context.getClickedPos().above();
        if (upperPos.getY() >= context.getLevel().getMaxBuildHeight()) return null;
        if (!context.getLevel().getBlockState(upperPos).canBeReplaced(context)) return null;
        Direction horizontalFacing = context.getHorizontalDirection().getOpposite();
        Direction inputSide = horizontalFacing.getClockWise();
        return defaultBlockState().setValue(FACING, inputSide).setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(HORIZONTAL_FACING, horizontalFacing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        BlockPos upperPos = pos.above();
        if (upperPos.getY() < level.getMaxBuildHeight() && level.getBlockState(pos).is(this) && level.getBlockState(upperPos).canBeReplaced()) {
            level.setBlock(upperPos, defaultBlockState().setValue(FACING, state.getValue(FACING)).setValue(HALF, DoubleBlockHalf.UPPER)
                    .setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING)), Block.UPDATE_ALL);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos otherPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState other = level.getBlockState(otherPos);
        if (!level.isClientSide && player.isCreative()) {
            BlockEntity source = level.getBlockEntity(pos);
            if (source instanceof RotaryLeacherBlockEntity leacher) leacher.clearItemsForCreativeBreak();
            BlockEntity counterpart = level.getBlockEntity(otherPos);
            if (counterpart instanceof RotaryLeacherBlockEntity leacher) leacher.clearItemsForCreativeBreak();
        }
        if (other.is(this) && other.getValue(HALF) != state.getValue(HALF)
                && other.getValue(HORIZONTAL_FACING) == state.getValue(HORIZONTAL_FACING)) {
            REMOVING_PAIRED_HALF.set(true);
            try {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
            } finally {
                REMOVING_PAIRED_HALF.remove();
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (player.isCreative()) return;
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        Entity breaker = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (breaker instanceof Player player && player.isCreative()) return List.of();
        return super.getDrops(state, builder);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        Direction joinedSide = half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        if (direction == joinedSide && (!neighborState.is(this) || neighborState.getValue(HALF) == half)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockPos otherPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState other = level.getBlockState(otherPos);
            if (!REMOVING_PAIRED_HALF.get() && other.is(this) && other.getValue(HALF) != state.getValue(HALF)
                    && other.getValue(HORIZONTAL_FACING) == state.getValue(HORIZONTAL_FACING)) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
            }
            if (!level.isClientSide) {
                BlockEntity removed = level.getBlockEntity(pos);
                if (removed instanceof RotaryLeacherBlockEntity leacher) {
                    for (ItemStack stack : leacher.getDropsForRemoval()) {
                        if (!stack.isEmpty()) Block.popResource(level, pos, stack);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? RotaryLeacherVoxelShapes.LOWER
                : RotaryLeacherVoxelShapes.UPPER;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        DoubleBlockHalf half = state.getValue(HALF);
        BlockState counterpart = level.getBlockState(half == DoubleBlockHalf.LOWER ? pos.above() : pos.below());
        return half == DoubleBlockHalf.LOWER
                ? counterpart.is(this) && counterpart.getValue(HALF) == DoubleBlockHalf.UPPER
                    && counterpart.getValue(HORIZONTAL_FACING) == state.getValue(HORIZONTAL_FACING)
                    || counterpart.canBeReplaced()
                : counterpart.is(this) && counterpart.getValue(HALF) == DoubleBlockHalf.LOWER
                    && counterpart.getValue(HORIZONTAL_FACING) == state.getValue(HORIZONTAL_FACING);
    }

    @Override
    public Class<RotaryLeacherBlockEntity> getBlockEntityClass() { return RotaryLeacherBlockEntity.class; }

    @Override
    public BlockEntityType<? extends RotaryLeacherBlockEntity> getBlockEntityType() { return AllBlockEntities.ROTARY_LEACHER.get(); }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        BlockEntityType<?> expected = state.getValue(HALF) == DoubleBlockHalf.UPPER
                ? AllBlockEntities.ROTARY_LEACHER_UPPER.get()
                : AllBlockEntities.ROTARY_LEACHER.get();
        if (type != expected) return null;
        return (tickerLevel, tickerPos, tickerState, blockEntity) -> ((RotaryLeacherBlockEntity) blockEntity).tick();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos masterPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (level.getBlockEntity(masterPos) instanceof RotaryLeacherBlockEntity leacher) {
            IItemHandler handler = leacher.getManualItemCapability();
            ItemStack offered = stack.copy();
            ItemStack remainder = handler.insertItem(RotaryLeacherBlockEntity.INPUT, offered, true);
            if (remainder.getCount() != offered.getCount()) {
                if (!level.isClientSide) {
                    int inserted = offered.getCount() - handler.insertItem(RotaryLeacherBlockEntity.INPUT, offered, false).getCount();
                    if (!player.isCreative()) stack.shrink(inserted);
                }
                return ItemInteractionResult.SUCCESS;
            }
            if (FluidUtil.interactWithFluidHandler(player, hand, leacher.getInternalFluidCapability())) {
                return ItemInteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        BlockPos masterPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (level.getBlockEntity(masterPos) instanceof RotaryLeacherBlockEntity leacher) {
            IItemHandler handler = leacher.getManualItemCapability();
            int amount = Integer.MAX_VALUE;
            int slot = handler.getStackInSlot(RotaryLeacherBlockEntity.OUTPUT).isEmpty()
                    ? RotaryLeacherBlockEntity.BYPRODUCT : RotaryLeacherBlockEntity.OUTPUT;
            if (!handler.getStackInSlot(slot).isEmpty()) {
                if (!level.isClientSide) {
                    ItemStack extracted = handler.extractItem(slot, amount, false);
                    if (!player.getInventory().add(extracted)) player.drop(extracted, false);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        BlockEntityType<RotaryLeacherBlockEntity> type = state.getValue(HALF) == DoubleBlockHalf.UPPER
                ? AllBlockEntities.ROTARY_LEACHER_UPPER.get()
                : AllBlockEntities.ROTARY_LEACHER.get();
        return new RotaryLeacherBlockEntity(type, pos, state);
    }
}
