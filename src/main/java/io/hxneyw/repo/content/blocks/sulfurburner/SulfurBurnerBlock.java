package io.hxneyw.repo.content.blocks.sulfurburner;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.content.blocks.WrenchInteractionHelper;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SulfurBurnerBlock
        extends HorizontalDirectionalBlock
        implements EntityBlock, IWrenchable {

    public static final MapCodec<SulfurBurnerBlock> CODEC =
            simpleCodec(SulfurBurnerBlock::new);

    /*
     * Critical:
     * use Create's exact heat property.
     */
    public static final EnumProperty<HeatLevel> HEAT_LEVEL =
            BlazeBurnerBlock.HEAT_LEVEL;

    public SulfurBurnerBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(
                                FACING,
                                Direction.NORTH
                        )
                        .setValue(
                                HEAT_LEVEL,
                                HeatLevel.NONE
                        )
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
        return defaultBlockState()
                .setValue(
                        FACING,
                        context
                                .getHorizontalDirection()
                                .getOpposite()
                )
                .setValue(
                        HEAT_LEVEL,
                        HeatLevel.NONE
                );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    Block,
                    BlockState
                    > builder
    ) {
        builder.add(
                FACING,
                HEAT_LEVEL
        );
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

        if (!SulfurBurnerFuel.isFuel(stack)) {
            return ItemInteractionResult
                    .PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(level.getBlockEntity(pos)
                instanceof SulfurBurnerBlockEntity burner)) {
            return ItemInteractionResult
                    .SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (!burner.insertOneFuel(
                stack,
                false
        )) {
            return ItemInteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public BlockState getRotatedBlockState(
            BlockState originalState,
            Direction targetedFace
    ) {
        return originalState.setValue(
                FACING,
                originalState
                        .getValue(FACING)
                        .getClockWise()
        );
    }

    @Override
    public @NotNull BlockState rotate(
            @NotNull BlockState state,
            @NotNull Rotation rotation
    ) {
        return state.setValue(
                FACING,
                rotation.rotate(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    public @NotNull BlockState mirror(
            @NotNull BlockState state,
            @NotNull Mirror mirror
    ) {
        return state.setValue(
                FACING,
                mirror.mirror(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        return new SulfurBurnerBlockEntity(
                pos,
                state
        );
    }



    @Override
    public @Nullable <T extends BlockEntity>
    BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        if (type
                != AllBlockEntities
                .SULFUR_BURNER
                .get()) {
            return null;
        }

        if (level.isClientSide) {
            return (
                    tickerLevel,
                    tickerPos,
                    tickerState,
                    blockEntity
            ) ->
                    SulfurBurnerBlockEntity.clientTick(
                            tickerLevel,
                            tickerPos,
                            (SulfurBurnerBlockEntity) blockEntity
                    );
        }

        return (
                tickerLevel,
                tickerPos,
                tickerState,
                blockEntity
        ) ->
                SulfurBurnerBlockEntity.serverTick(
                        tickerLevel,
                        tickerPos,
                        (SulfurBurnerBlockEntity)
                                blockEntity
                );
    }



    @Override
    public void onPlace(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState oldState,
            boolean isMoving
    ) {
        super.onPlace(
                state,
                level,
                pos,
                oldState,
                isMoving
        );

        if (level.isClientSide) {
            return;
        }

        BlockEntity above =
                level.getBlockEntity(pos.above());

        if (above instanceof BasinBlockEntity basin) {
            basin.notifyChangeOfContents();
        }
    }

    @Override
    public void onRemove(
            BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            BlockState newState,
            boolean isMoving
    ) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide
                && level.getBlockEntity(pos)
                instanceof SulfurBurnerBlockEntity burner) {

            ItemStack queued =
                    burner.drainQueuedFuelForDrop();

            if (!queued.isEmpty()) {
                Containers.dropItemStack(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        queued
                );
            }
        }

        super.onRemove(
                state,
                level,
                pos,
                newState,
                isMoving
        );
    }
}