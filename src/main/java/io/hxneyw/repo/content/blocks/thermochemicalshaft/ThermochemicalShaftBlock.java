package io.hxneyw.repo.content.blocks.thermochemicalshaft;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.placement.PoleHelper;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.function.Predicate;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermochemicalShaftBlock
        extends RotatedPillarKineticBlock
        implements IBE<ThermochemicalShaftBlockEntity>,
        ProperWaterloggedBlock,
        ThermochemicalConnection {

    public static final EnumProperty<CasingType> CASING = EnumProperty.create(
            "casing",
            CasingType.class
    );

    public static final int PLACEMENT_HELPER_ID = PlacementHelpers.register(
            new PlacementHelper()
    );

    private static final VoxelShape X_SHAPE = Block.box(0, 6, 6, 16, 10, 10);
    private static final VoxelShape Y_SHAPE = Block.box(6, 0, 6, 10, 16, 10);
    private static final VoxelShape Z_SHAPE = Block.box(6, 6, 0, 10, 10, 16);

    public ThermochemicalShaftBlock(@NotNull Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(BlockStateProperties.WATERLOGGED, false)
                        .setValue(CASING, CasingType.NONE)
        );
    }

    @Override
    protected boolean isPathfindable(
            @NotNull BlockState state,
            @NotNull PathComputationType pathComputationType
    ) {
        return false;
    }

    @Override
    public boolean hasShaftTowards(
            @NotNull LevelReader level,
            @NotNull BlockPos position,
            @NotNull BlockState state,
            @NotNull Direction face
    ) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            @NotNull BlockState state,
            @NotNull Direction face
    ) {
        return face.getAxis() != state.getValue(AXIS);
    }

    @Override
    public @NotNull Axis getRotationAxis(@NotNull BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public @NotNull Class<ThermochemicalShaftBlockEntity> getBlockEntityClass() {
        return ThermochemicalShaftBlockEntity.class;
    }

    @Override
    public @NotNull BlockEntityType<? extends ThermochemicalShaftBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.THERMOCHEMICAL_SHAFT.get();
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction(
            @NotNull BlockState state
    ) {
        return PushReaction.NORMAL;
    }

    @Override
    public @NotNull FluidState getFluidState(@NotNull BlockState state) {
        return fluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(
            @NotNull StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.WATERLOGGED, CASING);
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
        updateWater(level, state, position);
        return state;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        return withWater(super.getStateForPlacement(context), context);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack,
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos position,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit
    ) {
        CasingType currentCasing = state.getValue(CASING);

        if (stack.getItem() instanceof WrenchItem
                && currentCasing != CasingType.NONE) {
            if (!level.isClientSide()) {
                level.setBlock(
                        position,
                        state.setValue(CASING, CasingType.NONE),
                        Block.UPDATE_ALL
                );
                if (!player.getAbilities().instabuild) {
                    Block.popResource(
                            level,
                            position,
                            currentCasing.asStack()
                    );
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (!player.isShiftKeyDown() && player.mayBuild()) {
            CasingType requestedCasing = CasingType.fromStack(stack);
            if (requestedCasing != CasingType.NONE
                    && requestedCasing != currentCasing) {
                if (!level.isClientSide()) {
                    level.setBlock(
                            position,
                            state.setValue(CASING, requestedCasing),
                            Block.UPDATE_ALL
                    );
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                        if (currentCasing != CasingType.NONE) {
                            Block.popResource(
                                    level,
                                    position,
                                    currentCasing.asStack()
                            );
                        }
                    }
                }
                return ItemInteractionResult.SUCCESS;
            }

            IPlacementHelper helper = PlacementHelpers.get(
                    PLACEMENT_HELPER_ID
            );
            if (currentCasing == CasingType.NONE
                    && helper.matchesItem(stack)) {
                return helper.getOffset(
                                player,
                                level,
                                state,
                                position,
                                hit
                        )
                        .placeInWorld(
                                level,
                                (BlockItem) stack.getItem(),
                                player,
                                hand,
                                hit
                        );
            }
        }

        return super.useItemOn(
                stack,
                state,
                level,
                position,
                player,
                hand,
                hit
        );
    }

    @Override
    public @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        if (state.getValue(CASING) != CasingType.NONE) {
            return Block.box(0, 0, 0, 16, 16, 16);
        }
        return shapeForAxis(state.getValue(AXIS));
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        if (state.getValue(CASING) != CasingType.NONE) {
            return Block.box(0, 0, 0, 16, 16, 16);
        }
        return shapeForAxis(state.getValue(AXIS));
    }

    private static @NotNull VoxelShape shapeForAxis(@NotNull Axis axis) {
        return switch (axis) {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };
    }

    @Override
    public float getParticleTargetRadius() {
        return 0.35F;
    }

    @Override
    public float getParticleInitialRadius() {
        return 0.125F;
    }

    @MethodsReturnNonnullByDefault
    private static class PlacementHelper extends PoleHelper<Axis> {

        private PlacementHelper() {
            super(
                    state -> state.getBlock()
                            instanceof ThermochemicalShaftBlock
                            && state.getValue(CASING) == CasingType.NONE,
                    state -> state.getValue(AXIS),
                    AXIS
            );
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock()
                    instanceof ThermochemicalShaftBlock;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock()
                    instanceof ThermochemicalShaftBlock
                    && state.getValue(CASING) == CasingType.NONE;
        }
    }

    public enum CasingType implements StringRepresentable {
        NONE("none", null),
        ANDESITE("andesite", "andesite_casing"),
        BRASS("brass", "brass_casing"),
        COPPER("copper", "copper_casing"),
        RAILWAY("railway", "railway_casing");

        private final String serializedName;
        @Nullable
        private final ResourceLocation blockId;

        CasingType(
                @NotNull String serializedName,
                @Nullable String blockPath
        ) {
            this.serializedName = serializedName;
            this.blockId = blockPath == null
                    ? null
                    : ResourceLocation.fromNamespaceAndPath(
                            "create",
                            blockPath
                    );
        }

        @Override
        public @NotNull String getSerializedName() {
            return serializedName;
        }

        public @NotNull String translationKey() {
            return "tooltip.sulfuricresonance.thermochemical.casing."
                    + serializedName;
        }

        public @NotNull ItemStack asStack() {
            if (blockId == null) {
                return ItemStack.EMPTY;
            }
            Block block = BuiltInRegistries.BLOCK.get(blockId);
            return block == Blocks.AIR
                    ? ItemStack.EMPTY
                    : new ItemStack(block);
        }

        public static @NotNull CasingType fromStack(
                @NotNull ItemStack stack
        ) {
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                return NONE;
            }
            ResourceLocation itemBlockId = BuiltInRegistries.BLOCK.getKey(
                    blockItem.getBlock()
            );
            for (CasingType casingType : values()) {
                if (casingType.blockId != null
                        && casingType.blockId.equals(itemBlockId)) {
                    return casingType;
                }
            }
            return NONE;
        }
    }
}
