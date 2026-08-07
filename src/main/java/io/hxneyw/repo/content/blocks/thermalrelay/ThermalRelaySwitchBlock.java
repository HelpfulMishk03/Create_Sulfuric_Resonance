package io.hxneyw.repo.content.blocks.thermalrelay;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import io.hxneyw.repo.content.blocks.WrenchInteractionHelper;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.menu.ThermalRelaySwitchMenu;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermalRelaySwitchBlock
        extends HorizontalDirectionalBlock
        implements EntityBlock, IWrenchable {

    public static final MapCodec<ThermalRelaySwitchBlock> CODEC =
            simpleCodec(ThermalRelaySwitchBlock::new);

    public static final IntegerProperty POWER =
            BlockStateProperties.POWER;


    public static final IntegerProperty GLOW =
            IntegerProperty.create("glow", 0, 5);

    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 4.0, 14.0, 1.0, 12.0),
            Block.box(3.0, 1.0, 5.0, 13.0, 2.0, 11.0),

            Block.box(4.0, 2.0, 6.0, 12.0, 4.0, 10.0),

            Block.box(4.0, 2.0, 4.3, 12.0, 7.0, 6.0),
            Block.box(4.0, 2.0, 10.0, 12.0, 7.0, 11.5),
            Block.box(3.0, 2.0, 6.0, 5.0, 7.5, 10.0),
            Block.box(11.0, 2.0, 6.0, 13.0, 7.5, 10.0),
            Block.box(4.0, 6.0, 5.0, 12.0, 7.5, 11.0),

            Block.box(6.0, 0.0, 0.0, 10.0, 1.0, 4.0),
            Block.box(6.0, 0.0, 12.0, 10.0, 1.0, 16.0),
            Block.box(1.0, 0.0, 6.0, 2.0, 1.0, 10.0),
            Block.box(14.0, 0.0, 6.0, 15.0, 1.0, 10.0)
    );

    private static final VoxelShape EAST_SHAPE =
            rotateClockwise(NORTH_SHAPE);

    private static final VoxelShape SOUTH_SHAPE =
            rotateClockwise(EAST_SHAPE);

    private static final VoxelShape WEST_SHAPE =
            rotateClockwise(SOUTH_SHAPE);

    public ThermalRelaySwitchBlock(
            Properties properties
    ) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(POWER, 0)
                        .setValue(GLOW, 0)
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
        builder.add(FACING, POWER, GLOW);
    }


    public static int getRelayLightLevel(
            @NotNull BlockState state
    ) {
        return switch (state.getValue(GLOW)) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 6;
            case 4 -> 8;
            case 5 -> 10;
            default -> 0;
        };
    }

    @Override
    public int getLightEmission(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos
    ) {
        return getRelayLightLevel(state);
    }

    @Override
    protected boolean isSignalSource(
            @NotNull BlockState state
    ) {
        return true;
    }

    @Override
    protected int getSignal(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull Direction direction
    ) {
        return state.getValue(POWER);
    }

    @Override
    protected int getDirectSignal(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull Direction direction
    ) {
        return state.getValue(POWER);
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return shapeFor(state);
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return shapeFor(state);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        return new ThermalRelaySwitchBlockEntity(pos, state);
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

        if (level.isClientSide) {
            return;
        }

        UUID networkId =
                ThermalRelaySwitchItem.getNetworkId(stack);

        ThermalRelaySwitchItem.FurnaceLink link =
                ThermalRelaySwitchItem.getLinkedFurnace(stack);

        if (networkId == null || link == null) {
            return;
        }

        if (level.getBlockEntity(pos)
                instanceof ThermalRelaySwitchBlockEntity relay) {
            relay.setConnection(networkId, link);
        }
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

        if (!(level.getBlockEntity(pos)
                instanceof ThermalRelaySwitchBlockEntity relay)) {
            return ItemInteractionResult
                    .SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                relay.clearConnections();

                player.displayClientMessage(
                        Component.translatable(
                                "message.sulfuricresonance."
                                        + "thermal_relay_switch."
                                        + "network_removed"
                        ),
                        true
                );
            }

            return ItemInteractionResult.SUCCESS;
        }

        if (stack.getItem()
                instanceof ThermalRelaySwitchItem) {
            UUID networkId = relay.getNetworkId();

            ThermalRelaySwitchItem.FurnaceLink link =
                    relay.getFurnaceLink();

            if (!level.isClientSide
                    && networkId != null
                    && link != null) {
                ThermalRelaySwitchItem.setConnection(
                        stack,
                        networkId,
                        link
                );

                player.getInventory().setChanged();
            }

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult
                .PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hit
    ) {
        if (player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos)
                    instanceof ThermalRelaySwitchBlockEntity relay
                    && !level.isClientSide) {
                relay.clearConnections();

                player.displayClientMessage(
                        Component.translatable(
                                "message.sulfuricresonance."
                                        + "thermal_relay_switch."
                                        + "network_removed"
                        ),
                        true
                );
            }

            return InteractionResult.sidedSuccess(
                    level.isClientSide
            );
        }

        if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider =
                    getMenuProvider(state, level, pos);

            if (provider != null) {
                serverPlayer.openMenu(provider);
            }
        }

        return InteractionResult.sidedSuccess(
                level.isClientSide
        );
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof ThermalRelaySwitchBlockEntity relay)) {
            return null;
        }

        return new SimpleMenuProvider(
                (
                        containerId,
                        playerInventory,
                        player
                ) -> new ThermalRelaySwitchMenu(
                        containerId,
                        playerInventory,
                        relay
                ),
                Component.translatable(
                        "menu.sulfuricresonance."
                                + "thermal_relay_switch"
                )
        );
    }

    @Override
    public @Nullable <T extends BlockEntity>
    BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        if (level.isClientSide
                || type != AllBlockEntities
                .THERMAL_RELAY_SWITCH.get()) {
            return null;
        }

        return (
                tickerLevel,
                tickerPos,
                tickerState,
                blockEntity
        ) -> {
            if (blockEntity
                    instanceof ThermalRelaySwitchBlockEntity relay) {
                ThermalRelaySwitchBlockEntity.serverTick(
                        tickerLevel,
                        relay
                );
            }
        };
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

    private static @NotNull VoxelShape shapeFor(
            @NotNull BlockState state
    ) {
        Direction facing = state.getValue(FACING);

        if (facing == Direction.EAST) {
            return EAST_SHAPE;
        }

        if (facing == Direction.SOUTH) {
            return SOUTH_SHAPE;
        }

        if (facing == Direction.WEST) {
            return WEST_SHAPE;
        }

        return NORTH_SHAPE;
    }

    private static VoxelShape rotateClockwise(
            VoxelShape source
    ) {
        VoxelShape[] result = {Shapes.empty()};

        source.forAllBoxes(
                (
                        minX,
                        minY,
                        minZ,
                        maxX,
                        maxY,
                        maxZ
                ) ->
                        result[0] = Shapes.or(
                                result[0],
                                Shapes.box(
                                        1.0 - maxZ,
                                        minY,
                                        minX,
                                        1.0 - minZ,
                                        maxY,
                                        maxX
                                )
                        )
        );

        return result[0];
    }
}