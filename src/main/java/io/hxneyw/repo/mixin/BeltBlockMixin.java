package io.hxneyw.repo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import io.hxneyw.repo.Config;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import io.hxneyw.repo.content.blocks.thermochemicalshaft.ThermochemicalShaftBlock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        value = BeltBlock.class,
        remap = false
)
public abstract class BeltBlockMixin {

    @Unique
    private static final ResourceLocation SULFURICRESONANCE$THERMOCHEMICAL_SHAFT_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "thermochemical_shaft"
            );

    @Unique
    private static final ThreadLocal<Deque<Set<BlockPos>>>
            SULFURICRESONANCE$RESTORE_STACK = ThreadLocal.withInitial(
                    ArrayDeque::new
            );

    @Inject(
            method = "getCloneItemStack",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sulfuricresonance$pickCombustionBeltConnector(
            BlockState state,
            HitResult target,
            LevelReader level,
            BlockPos pos,
            Player player,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (!sulfuricresonance$isCombustionBelt(level, pos)) {
            return;
        }

        cir.setReturnValue(
                new ItemStack(
                        Items.COMBUSTION_BELT_CONNECTOR.get()
                )
        );
    }

    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sulfuricresonance$handleCombustionBeltItems(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (!sulfuricresonance$isCombustionBelt(level, pos)) {
            return;
        }

        if (AllItems.BELT_CONNECTOR.isIn(stack)
                || AllBlocks.SHAFT.isIn(stack)) {
            cir.setReturnValue(ItemInteractionResult.FAIL);
            return;
        }

        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock()
                instanceof ThermochemicalShaftBlock)) {
            return;
        }

        if (state.getValue(BeltBlock.PART) != BeltPart.MIDDLE) {
            cir.setReturnValue(ItemInteractionResult.FAIL);
            return;
        }

        if (!level.isClientSide()) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            KineticBlockEntity.switchToBlockState(
                    level,
                    pos,
                    state.setValue(BeltBlock.PART, BeltPart.PULLEY)
            );

            BlockEntity pulleyEntity =
                    level.getBlockEntity(pos);

            if (pulleyEntity
                    instanceof CombustionBeltAccessor accessor) {
                accessor.sulfuricresonance$setCombustionBelt(
                        true
                );
                accessor.sulfuricresonance$setThermochemicalPulley(
                        true
                );

                if (pulleyEntity
                        instanceof BeltBlockEntity belt) {
                    belt.setChanged();
                    belt.sendData();
                }
            }
        }

        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }

    @Inject(
            method = "getDrops",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void sulfuricresonance$replacePulleyDrop(
            BlockState state,
            LootParams.Builder builder,
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        BlockEntity blockEntity = builder.getOptionalParameter(
                LootContextParams.BLOCK_ENTITY
        );

        if (!(blockEntity instanceof BeltBlockEntity belt)
                || !(belt instanceof CombustionBeltAccessor accessor)
                || !accessor.sulfuricresonance$isCombustionBelt()
                || !belt.hasPulley()) {
            return;
        }

        ItemStack thermochemicalShaft =
                sulfuricresonance$thermochemicalShaftStack();

        if (thermochemicalShaft.isEmpty()) {
            return;
        }

        List<ItemStack> drops = new ArrayList<>(cir.getReturnValue());

        for (int index = 0; index < drops.size(); index++) {
            if (AllBlocks.SHAFT.isIn(drops.get(index))) {
                drops.remove(index);
                break;
            }
        }

        drops.add(thermochemicalShaft);
        cir.setReturnValue(drops);
    }

    @Inject(
            method = "onWrenched",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sulfuricresonance$removeCombustionPulley(
            BlockState state,
            UseOnContext context,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (state.getValue(BeltBlock.PART) != BeltPart.PULLEY) {
            return;
        }

        Level level = context.getLevel();
        BlockPos position = context.getClickedPos();

        if (!sulfuricresonance$isCombustionBelt(level, position)) {
            return;
        }

        if (!level.isClientSide()) {
            BlockEntity pulleyEntity =
                    level.getBlockEntity(position);

            if (pulleyEntity
                    instanceof CombustionBeltAccessor accessor) {
                accessor.sulfuricresonance$setThermochemicalPulley(
                        false
                );
            }

            KineticBlockEntity.switchToBlockState(
                    level,
                    position,
                    state.setValue(BeltBlock.PART, BeltPart.MIDDLE)
            );

            Player player = context.getPlayer();
            ItemStack shaft = sulfuricresonance$thermochemicalShaftStack();

            if (player != null
                    && !player.isCreative()
                    && !shaft.isEmpty()) {
                player.getInventory().placeItemBackInInventory(shaft);
            }
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(
            method = "onRemove",
            at = @At("HEAD"),
            remap = false
    )
    private void sulfuricresonance$captureCombustionRemoval(
            BlockState state,
            Level world,
            BlockPos pos,
            BlockState newState,
            boolean isMoving,
            CallbackInfo ci
    ) {
        Set<BlockPos> pulleyPositions = Set.of();

        if (!world.isClientSide()
                && state.getBlock() != newState.getBlock()
                && !isMoving) {
            pulleyPositions =
                    sulfuricresonance$findThermochemicalPulleys(
                            world,
                            state,
                            pos
                    );
        }

        SULFURICRESONANCE$RESTORE_STACK
                .get()
                .push(pulleyPositions);
    }

    @WrapOperation(
            method = "onRemove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            ),
            remap = false
    )
    private boolean sulfuricresonance$replaceCreateShaftDirectly(
            Level level,
            BlockPos p_46601_,
            BlockState p_46602_,
            int p_46603_,
            Operation<Boolean> original
    ) {
        Deque<Set<BlockPos>> stack =
                SULFURICRESONANCE$RESTORE_STACK.get();

        if (!stack.isEmpty()
                && stack.peek().contains(p_46601_)
                && AllBlocks.SHAFT.has(p_46602_)) {
            Block thermochemicalShaft =
                    BuiltInRegistries.BLOCK.get(
                            SULFURICRESONANCE$THERMOCHEMICAL_SHAFT_ID
                    );

            if (thermochemicalShaft
                    instanceof ThermochemicalShaftBlock) {
                BlockState thermochemicalState =
                        thermochemicalShaft
                                .defaultBlockState()
                                .setValue(
                                        BlockStateProperties.AXIS,
                                        p_46602_.getValue(
                                                BlockStateProperties.AXIS
                                        )
                                );

                if (thermochemicalState.hasProperty(
                        BlockStateProperties.WATERLOGGED
                )
                        && p_46602_.hasProperty(
                        BlockStateProperties.WATERLOGGED
                )) {
                    thermochemicalState =
                            thermochemicalState.setValue(
                                    BlockStateProperties.WATERLOGGED,
                                    p_46602_.getValue(
                                            BlockStateProperties.WATERLOGGED
                                    )
                            );
                }

                p_46602_ = thermochemicalState;
            }
        }

        return original.call(
                level,
                p_46601_,
                p_46602_,
                p_46603_
        );
    }

    @Inject(
            method = "onRemove",
            at = @At("RETURN"),
            remap = false
    )
    private void sulfuricresonance$clearCombustionRemoval(
            BlockState state,
            Level world,
            BlockPos pos,
            BlockState newState,
            boolean isMoving,
            CallbackInfo ci
    ) {
        Deque<Set<BlockPos>> stack =
                SULFURICRESONANCE$RESTORE_STACK.get();

        if (!stack.isEmpty()) {
            stack.pop();
        }

        if (stack.isEmpty()) {
            SULFURICRESONANCE$RESTORE_STACK.remove();
        }
    }

    @Unique
    private static Set<BlockPos>
    sulfuricresonance$findThermochemicalPulleys(
            Level level,
            BlockState removedState,
            BlockPos removedPosition
    ) {
        Set<BlockPos> chainPositions =
                new LinkedHashSet<>();

        chainPositions.add(removedPosition.immutable());

        sulfuricresonance$collectBeltDirection(
                level,
                removedState,
                removedPosition,
                true,
                chainPositions
        );

        sulfuricresonance$collectBeltDirection(
                level,
                removedState,
                removedPosition,
                false,
                chainPositions
        );

        boolean combustionChain = false;

        for (BlockPos chainPosition : chainPositions) {
            BlockEntity blockEntity =
                    level.getBlockEntity(chainPosition);

            if (blockEntity
                    instanceof CombustionBeltAccessor accessor
                    && accessor
                    .sulfuricresonance$isCombustionBelt()) {
                combustionChain = true;
                break;
            }
        }

        if (!combustionChain) {
            return Set.of();
        }

        Set<BlockPos> pulleyPositions =
                new LinkedHashSet<>();

        for (BlockPos chainPosition : chainPositions) {
            BlockState chainState =
                    chainPosition.equals(removedPosition)
                            ? removedState
                            : level.getBlockState(chainPosition);

            BlockEntity blockEntity =
                    level.getBlockEntity(chainPosition);

            boolean pulley =
                    chainState.getBlock() instanceof BeltBlock
                    && chainState.getValue(BeltBlock.PART)
                    == BeltPart.PULLEY;

            if (blockEntity instanceof BeltBlockEntity belt
                    && belt.hasPulley()) {
                pulley = true;
            }

            if (blockEntity
                    instanceof CombustionBeltAccessor accessor
                    && accessor
                    .sulfuricresonance$isThermochemicalPulley()) {
                pulley = true;
            }

            if (pulley) {
                pulleyPositions.add(chainPosition.immutable());
            }
        }

        return Set.copyOf(pulleyPositions);
    }

    @Unique
    private static void sulfuricresonance$collectBeltDirection(
            Level level,
            BlockState initialState,
            BlockPos initialPosition,
            boolean forward,
            Set<BlockPos> chainPositions
    ) {
        BlockState currentState = initialState;
        BlockPos currentPosition = initialPosition;

        int traversalLimit = Config.combustionBeltTraversalLimit();
        int maximumSteps = traversalLimit > Integer.MAX_VALUE / 2
                ? Integer.MAX_VALUE
                : traversalLimit * 2;

        for (int step = 0;
             step < maximumSteps;
             step++) {
            BlockPos nextPosition =
                    BeltBlock.nextSegmentPosition(
                            currentState,
                            currentPosition,
                            forward
                    );

            if (nextPosition == null
                    || !level.isLoaded(nextPosition)) {
                return;
            }

            BlockState nextState =
                    level.getBlockState(nextPosition);

            if (!AllBlocks.BELT.has(nextState)
                    || !chainPositions.add(
                            nextPosition.immutable()
                    )) {
                return;
            }

            currentPosition = nextPosition;
            currentState = nextState;
        }
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Unique
    private static boolean sulfuricresonance$isCombustionBelt(
            LevelReader level,
            BlockPos position
    ) {
        BlockEntity blockEntity = level.getBlockEntity(position);
        return blockEntity instanceof CombustionBeltAccessor accessor
                && accessor.sulfuricresonance$isCombustionBelt();
    }

    @Unique
    private static ItemStack sulfuricresonance$thermochemicalShaftStack() {
        Block block = BuiltInRegistries.BLOCK.get(
                SULFURICRESONANCE$THERMOCHEMICAL_SHAFT_ID
        );
        return block == Blocks.AIR
                ? ItemStack.EMPTY
                : new ItemStack(block);
    }
}
