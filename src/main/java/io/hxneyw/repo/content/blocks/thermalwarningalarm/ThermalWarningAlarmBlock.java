package io.hxneyw.repo.content.blocks.thermalwarningalarm;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import io.hxneyw.repo.content.blocks.WrenchInteractionHelper;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermalWarningAlarmBlock
        extends FaceAttachedHorizontalDirectionalBlock
        implements EntityBlock, IWrenchable {

    public static final MapCodec<ThermalWarningAlarmBlock> CODEC =
            simpleCodec(ThermalWarningAlarmBlock::new);

    public static final BooleanProperty CONNECTED =
            BooleanProperty.create("connected");
    public static final BooleanProperty ALARMING =
            BooleanProperty.create("alarming");

    private static final VoxelShape BASE_SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 1.0, 14.0, 5.6, 15.0),
            Block.box(0.5, 1.5, -2.0, 6.8, 5.6, 6.0)
    );

    public ThermalWarningAlarmBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(CONNECTED, false)
                        .setValue(ALARMING, false)
        );
    }

    @Override
    protected @NotNull MapCodec<
            ? extends FaceAttachedHorizontalDirectionalBlock
            > codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(
                builder.add(FACE, FACING, CONNECTED, ALARMING)
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        if (state.getValue(FACE) == AttachFace.FLOOR) {
            state = state.setValue(
                    FACING,
                    state.getValue(FACING).getOpposite()
            );
        }

        return state.setValue(CONNECTED, false)
                .setValue(ALARMING, false);
    }

    @Override
    public boolean canSurvive(
            @NotNull BlockState state,
            @NotNull LevelReader level,
            @NotNull BlockPos pos
    ) {
        Direction connectedDirection = getConnectedDirection(state);
        BlockPos supportPos = pos.relative(connectedDirection.getOpposite());
        return !level.getBlockState(supportPos)
                .getCollisionShape(level, supportPos)
                .isEmpty();
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

        UUID networkId = ThermalRelaySwitchItem.getNetworkId(stack);
        ThermalRelaySwitchItem.FurnaceLink link =
                ThermalRelaySwitchItem.getLinkedFurnace(stack);

        if (networkId != null
                && link != null
                && level.getBlockEntity(pos)
                instanceof ThermalWarningAlarmBlockEntity alarm) {
            alarm.setConnection(networkId, link);
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

        if (!(level.getBlockEntity(pos)
                instanceof ThermalWarningAlarmBlockEntity alarm)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.getItem() instanceof ThermalRelaySwitchItem) {
            UUID itemNetworkId = ThermalRelaySwitchItem.getNetworkId(stack);
            ThermalRelaySwitchItem.FurnaceLink itemLink =
                    ThermalRelaySwitchItem.getLinkedFurnace(stack);

            if (itemNetworkId != null && itemLink != null) {
                if (!level.isClientSide) {
                    alarm.setConnection(itemNetworkId, itemLink);
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.sulfuricresonance.thermal_warning_alarm.network_set"
                            ),
                            true
                    );
                }
                return ItemInteractionResult.SUCCESS;
            }

            UUID alarmNetworkId = alarm.getNetworkId();
            ThermalRelaySwitchItem.FurnaceLink alarmLink = alarm.getFurnaceLink();

            if (alarmNetworkId != null && alarmLink != null) {
                if (!level.isClientSide) {
                    ThermalRelaySwitchItem.setConnection(
                            stack,
                            alarmNetworkId,
                            alarmLink
                    );
                    player.getInventory().setChanged();
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.sulfuricresonance.thermal_warning_alarm.network_copied"
                            ),
                            true
                    );
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof ThermalWarningAlarmBlockEntity alarm)) {
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            alarm.clearConnection();
            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance.thermal_warning_alarm.network_removed"
                    ),
                    true
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull Direction direction
    ) {
        return state.getValue(ALARMING) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull Direction direction
    ) {
        return state.getValue(ALARMING) ? 15 : 0;
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return transformShape(
                state.getValue(FACE),
                state.getValue(FACING)
        );
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        return new ThermalWarningAlarmBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        if (level.isClientSide
                || type != AllBlockEntities.THERMAL_WARNING_ALARM.get()) {
            return null;
        }

        return (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof ThermalWarningAlarmBlockEntity alarm) {
                ThermalWarningAlarmBlockEntity.serverTick(tickerLevel, alarm);
            }
        };
    }

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

    @Override
    public @NotNull BlockState rotate(
            @NotNull BlockState state,
            @NotNull Rotation rotation
    ) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(
            @NotNull BlockState state,
            @NotNull Mirror mirror
    ) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    private static VoxelShape transformShape(
            AttachFace face,
            Direction facing
    ) {
        VoxelShape[] result = {Shapes.empty()};

        ThermalWarningAlarmBlock.BASE_SHAPE.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double transformedMinX = Double.POSITIVE_INFINITY;
            double transformedMinY = Double.POSITIVE_INFINITY;
            double transformedMinZ = Double.POSITIVE_INFINITY;
            double transformedMaxX = Double.NEGATIVE_INFINITY;
            double transformedMaxY = Double.NEGATIVE_INFINITY;
            double transformedMaxZ = Double.NEGATIVE_INFINITY;

            for (int xIndex = 0; xIndex < 2; xIndex++) {
                double x = xIndex == 0 ? minX : maxX;
                for (int yIndex = 0; yIndex < 2; yIndex++) {
                    double y = yIndex == 0 ? minY : maxY;
                    for (int zIndex = 0; zIndex < 2; zIndex++) {
                        double z = zIndex == 0 ? minZ : maxZ;
                        double[] point = transformPoint(x, y, z, face, facing);
                        transformedMinX = Math.min(transformedMinX, point[0]);
                        transformedMinY = Math.min(transformedMinY, point[1]);
                        transformedMinZ = Math.min(transformedMinZ, point[2]);
                        transformedMaxX = Math.max(transformedMaxX, point[0]);
                        transformedMaxY = Math.max(transformedMaxY, point[1]);
                        transformedMaxZ = Math.max(transformedMaxZ, point[2]);
                    }
                }
            }

            result[0] = Shapes.or(
                    result[0],
                    Shapes.box(
                            transformedMinX,
                            transformedMinY,
                            transformedMinZ,
                            transformedMaxX,
                            transformedMaxY,
                            transformedMaxZ
                    )
            );
        });

        return result[0];
    }

    private static double[] transformPoint(
            double x,
            double y,
            double z,
            AttachFace face,
            Direction facing
    ) {
        double centeredX = x - 0.5D;
        double centeredY = y - 0.5D;
        double centeredZ = z - 0.5D;

        if (face == AttachFace.WALL) {
            double rotatedY = centeredZ;
            double rotatedZ = -centeredY;
            centeredY = rotatedY;
            centeredZ = rotatedZ;
        } else if (face == AttachFace.CEILING) {
            centeredY = -centeredY;
            centeredZ = -centeredZ;
            facing = facing.getOpposite();
        }

        double rotatedX = centeredX;
        double rotatedZ = centeredZ;

        switch (facing) {
            case EAST -> {
                rotatedX = -centeredZ;
                rotatedZ = centeredX;
            }
            case SOUTH -> {
                rotatedX = -centeredX;
                rotatedZ = -centeredZ;
            }
            case WEST -> {
                rotatedX = centeredZ;
                rotatedZ = -centeredX;
            }
            default -> {
            }
        }

        return new double[]{
                rotatedX + 0.5D,
                centeredY + 0.5D,
                rotatedZ + 0.5D
        };
    }
}
