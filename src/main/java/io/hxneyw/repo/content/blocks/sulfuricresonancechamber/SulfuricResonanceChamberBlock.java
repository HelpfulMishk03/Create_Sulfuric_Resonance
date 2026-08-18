package io.hxneyw.repo.content.blocks.sulfuricresonancechamber;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.WrenchInteractionHelper;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.process.ProcessMonitorLinking;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SulfuricResonanceChamberBlock
        extends HorizontalDirectionalBlock
        implements IBE<SulfuricResonanceChamberBlockEntity>,
        IRotate,
        ThermochemicalConnection {

    public static final MapCodec<SulfuricResonanceChamberBlock>
            CODEC = simpleCodec(
            SulfuricResonanceChamberBlock::new
    );

    private static final VoxelShape SUPPORT_SHAPE = Shapes.or(
            box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D),
            box(0.0D, 15.0D, 0.0D, 1.0D, 16.0D, 16.0D),
            box(15.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            box(1.0D, 15.0D, 0.0D, 15.0D, 16.0D, 1.0D),
            box(1.0D, 15.0D, 15.0D, 15.0D, 16.0D, 16.0D)
    );

    public SulfuricResonanceChamberBlock(
            Properties properties
    ) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected @NotNull MapCodec<? extends SulfuricResonanceChamberBlock>
    codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        return defaultBlockState()
                .setValue(
                        FACING,
                        context.getHorizontalDirection().getOpposite()
                );
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos position,
            BlockState state,
            Direction face
    ) {
        return face == heatAndRotationSide(state);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        return face != heatAndRotationSide(state);
    }

    @Override
    public Axis getRotationAxis(
            BlockState state
    ) {
        return heatAndRotationSide(state).getAxis();
    }

    @Override
    public Class<SulfuricResonanceChamberBlockEntity>
    getBlockEntityClass() {
        return SulfuricResonanceChamberBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SulfuricResonanceChamberBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.SULFURIC_RESONANCE_CHAMBER.get();
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
        ItemInteractionResult wrenchResult = WrenchInteractionHelper.handle(
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

        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide
                && level.getBlockEntity(pos)
                instanceof SulfuricResonanceChamberBlockEntity chamber
                && ProcessMonitorLinking.tryComplete(
                        player,
                        level,
                        pos,
                        chamber
                )) {
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.getItem() instanceof BlockItem
                || stack.getItem() instanceof BucketItem) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        openChamberMenu(state, level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hit
    ) {
        if (!level.isClientSide
                && level.getBlockEntity(pos)
                instanceof SulfuricResonanceChamberBlockEntity chamber
                && ProcessMonitorLinking.tryComplete(
                        player,
                        level,
                        pos,
                        chamber
                )) {
            return InteractionResult.SUCCESS;
        }

        openChamberMenu(state, level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void openChamberMenu(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player
    ) {
        if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider != null) {
                serverPlayer.openMenu(provider);
            }
        }
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof SulfuricResonanceChamberBlockEntity chamber)) {
            return null;
        }

        return new SimpleMenuProvider(
                (containerId, playerInventory, player) ->
                        new SulfuricResonanceChamberMenu(
                                containerId,
                                playerInventory,
                                chamber
                        ),
                Component.translatable(
                        "block.sulfuricresonance.sulfuric_resonance_chamber"
                )
        );
    }

    public static Direction heatAndRotationSide(
            BlockState state
    ) {
        return state.getValue(FACING).getClockWise();
    }

    public static Direction fluidSide(
            BlockState state
    ) {
        return state.getValue(FACING).getCounterClockWise();
    }

    public static Direction itemBackSide(BlockState state) {
        return state.getValue(FACING).getOpposite();
    }

    public static boolean isItemAutomationSide(
            BlockState state,
            @Nullable Direction side
    ) {
        return side == Direction.UP || side == itemBackSide(state);
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos
    ) {
        return SUPPORT_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    net.minecraft.world.level.block.Block,
                    BlockState
                    > builder
    ) {
        builder.add(FACING);
    }
}
