package io.hxneyw.repo.content.blocks;

import com.mojang.serialization.MapCodec;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import io.hxneyw.repo.content.items.BlockCaddyInventory;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class BlockCaddyBlock extends BaseEntityBlock {
    public static final MapCodec<BlockCaddyBlock> CODEC = simpleCodec(BlockCaddyBlock::new);
    public static final BooleanProperty HAS_CARGO = BooleanProperty.create("has_cargo");
    public static final IntegerProperty CARGO_SHAPE_ID = IntegerProperty.create("cargo_shape", 0, 127);
    private static final VoxelShape CADDY_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 1.1, 0.5),
            Block.box(0, 0, 15.5, 16, 1.1, 16),
            Block.box(0, 0, 0.5, 0.5, 1.1, 15.5),
            Block.box(15.5, 0, 0.5, 16, 1.1, 15.5),
            Block.box(0, 1.1, 0, 2.5, 2.7, 2.5),
            Block.box(13.5, 1.1, 0, 16, 2.7, 2.5),
            Block.box(0, 1.1, 13.5, 2.5, 2.7, 16),
            Block.box(13.5, 1.1, 13.5, 16, 2.7, 16)
    );

    public BlockCaddyBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HAS_CARGO, false).setValue(CARGO_SHAPE_ID, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_CARGO, CARGO_SHAPE_ID);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeAt(state, level, pos, context, true);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return shapeAt(state, level, pos, context, false);
    }

    public static VoxelShape getCargoAwareShape(BlockState state) {
        if (!state.hasProperty(HAS_CARGO) || !state.getValue(HAS_CARGO)) {
            return CADDY_SHAPE;
        }
        int shapeId = state.hasProperty(CARGO_SHAPE_ID)
                ? state.getValue(CARGO_SHAPE_ID) : 0;
        VoxelShape registered = CaddyCollisionShapeRegistry.get(shapeId);
        if (registered != null) return registered;
        // The synthetic getter used by Sable intentionally has no block entity.
        // If its cargo data has not been rebound yet, keep cargo solid until it is.
        return Shapes.or(CADDY_SHAPE, Block.box(0, 1.1, 0, 16, 17.1, 16));
    }

    private static VoxelShape shapeAt(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context, boolean outline) {
        VoxelShape shape = CADDY_SHAPE;
        if (!(level.getBlockEntity(pos) instanceof BlockCaddyBlockEntity caddy)) {
            // Sable's synthetic sublevel getter intentionally has no block
            // entities. In that path the cargo state and shape id are carried
            // by the blockstate, so still expose the cached cargo collider.
            return getCargoAwareShape(state);
        }
        if (caddy.getCargo().isEmpty()) {
            return shape;
        }
        BlockState cargo = caddy.getCargoBlockState();
        if (cargo == null || cargo.isAir()) return shape;
        BlockGetter cargoLevel = new CargoShapeGetter(
                level, pos, cargo, caddy.getCargoBlockEntity(cargo));
        if (outline) {
            VoxelShape cached = caddy.getCachedCargoOutline(cargo);
            if (cached != null) return Shapes.or(shape, cached);
            VoxelShape cargoShape = cargoShape(cargo, cargoLevel, pos, context, true);
            caddy.cacheCargoOutline(cargo, cargoShape);
            return Shapes.or(shape, cargoShape);
        }
        return Shapes.or(shape, cargoShape(cargo, cargoLevel, pos, context, false));
    }

    private static VoxelShape cargoShape(BlockState cargo, BlockGetter level, BlockPos pos,
            CollisionContext context, boolean outline) {
        // A caddy used as cargo must use its fixed frame shape here. Asking its
        // state for a world shape at this position resolves the outer caddy's
        // block entity again, recursively evaluating this method until the
        // chunk renderer overflows the stack.
        VoxelShape cargoShape = cargo.getBlock() instanceof BlockCaddyBlock
                ? CADDY_SHAPE
                : outline
                        ? cargo.getShape(level, pos, context)
                        : cargo.getCollisionShape(level, pos, context);
        // A number of decorative and modded blocks provide a useful placed
        // selection shape but leave the collision shape empty. Inside a
        // Caddy, use that authored block outline as the cargo's physical
        // shape so visible cargo does not become intangible.
        if (!outline && cargoShape.isEmpty()
                && !(cargo.getBlock() instanceof BlockCaddyBlock)) {
            cargoShape = cargo.getShape(level, pos, context);
        }
        // Match the transform used by BlockCaddyBeltRenderer: cargo is inset
        // 1/32 on X/Z, starts 1.1/16 above the Caddy floor, and is scaled to
        // 15/16 wide by 14/16 high. Keeping collision in full-block space
        // created an invisible ledge above and around the rendered cargo.
        if (cargo.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            cargoShape = transformCargoShape(cargoShape, 1.1 / 16d, 7d / 16d);
            BlockState upper = cargo.setValue(
                    net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF,
                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
            VoxelShape upperShape = outline
                    ? upper.getShape(level, pos, context)
                    : upper.getCollisionShape(level, pos, context);
            cargoShape = Shapes.or(cargoShape,
                    transformCargoShape(upperShape, 1 + 1.1 / 16d, 7d / 16d));
        } else {
            cargoShape = transformCargoShape(cargoShape, 1.1 / 16d, 14d / 16d);
        }
        return cargoShape;
    }

    private static VoxelShape transformCargoShape(VoxelShape shape, double yOffset,
            double yScale) {
        VoxelShape transformed = Shapes.empty();
        double inset = 1d / 32d;
        double horizontalScale = 15d / 16d;
        for (net.minecraft.world.phys.AABB box : shape.toAabbs()) {
            transformed = Shapes.or(transformed, Block.box(
                    (box.minX * horizontalScale + inset) * 16,
                    (box.minY * yScale + yOffset) * 16,
                    (box.minZ * horizontalScale + inset) * 16,
                    (box.maxX * horizontalScale + inset) * 16,
                    (box.maxY * yScale + yOffset) * 16,
                    (box.maxZ * horizontalScale + inset) * 16));
        }
        return transformed;
    }

    /** Presents the cargo state and saved block entity to shape queries at the Caddy position. */
    private record CargoShapeGetter(BlockGetter delegate, BlockPos cargoPos,
            BlockState cargoState, @Nullable BlockEntity cargoBlockEntity) implements BlockGetter {
        @Override
        @Nullable
        public BlockEntity getBlockEntity(BlockPos pos) {
            return cargoPos.equals(pos) ? cargoBlockEntity : delegate.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return cargoPos.equals(pos) ? cargoState : delegate.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return cargoPos.equals(pos) ? cargoState.getFluidState() : delegate.getFluidState(pos);
        }

        @Override
        public int getHeight() {
            return delegate.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return delegate.getMinBuildHeight();
        }
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level,
            BlockPos pos, @Nullable Entity entity) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BlockCaddyBlockEntity caddy && !caddy.getCargo().isEmpty()) {
            BlockState cargoState = BlockCaddyItem.getCargoBlockState(
                    BlockCaddyItem.createStack(caddy.getCargo()));
            if (!cargoState.isAir()
                    && !(cargoState.getBlock() instanceof BlockCaddyBlock)) {
                return cargoState.getSoundType(level, pos, entity);
            }
        }
        return super.getSoundType(state, level, pos, entity);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockCaddyBlockEntity(pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level,
            BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof BlockCaddyBlockEntity caddy) {
            return BlockCaddyItem.createStack(caddy.getCargo());
        }
        return BlockCaddyItem.createStack(ItemStack.EMPTY);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return null;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        Entity breaker = params.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (breaker instanceof Player player && player.isCreative()) {
            return List.of();
        }

        BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        ItemStack drop = new ItemStack(Items.BLOCK_CADDY.get());
        if (entity instanceof BlockCaddyBlockEntity caddy && !caddy.getCargo().isEmpty()) {
            BlockCaddyItem.setCargoStack(drop, caddy.getCargo());
        }
        return List.of(drop);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        BlockCaddyBlockEntity storageCargo = getOpenableChestCargo(level, pos, player, hand);
        if (storageCargo != null) {
            if (!level.isClientSide) {
                BlockCaddyInventory.openForCargo(
                        player, storageCargo.getCargo(), storageCargo::setCargo);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!player.isShiftKeyDown()
                && BlockCaddyItem.isSupportedCargoStack(stack)
                && level.getBlockEntity(pos) instanceof BlockCaddyBlockEntity caddy
                && caddy.getCargo().isEmpty()) {
            ItemStack candidate = BlockCaddyItem.createStack(ItemStack.EMPTY);
            BlockState placedState = BlockCaddyItem.getPlacementState(
                    level, player, hand, stack, hit);
            if (!BlockCaddyItem.tryLoadFromBlockItem(candidate, stack, placedState)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                caddy.setCargo(BlockCaddyItem.getCargoStack(candidate));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        BlockCaddyBlockEntity storageCargo = getOpenableChestCargo(
                level, pos, player, InteractionHand.MAIN_HAND);
        if (storageCargo == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            BlockCaddyInventory.openForCargo(
                    player, storageCargo.getCargo(), storageCargo::setCargo);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    private static BlockCaddyBlockEntity getOpenableChestCargo(
            Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
            return null;
        }
        if (!(level.getBlockEntity(pos) instanceof BlockCaddyBlockEntity caddy)
                || !BlockCaddyItem.isStorageCargo(
                        BlockCaddyItem.createStack(caddy.getCargo()))) {
            return null;
        }
        return caddy;
    }
}
