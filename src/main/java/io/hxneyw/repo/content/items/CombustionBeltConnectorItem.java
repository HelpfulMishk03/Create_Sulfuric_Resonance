package io.hxneyw.repo.content.items;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.kinetics.belt.BeltSlicer;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import io.hxneyw.repo.Config;
import io.hxneyw.repo.mixin.CombustionBeltLengthOverride;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import io.hxneyw.repo.content.blocks.thermochemicalshaft.ThermochemicalShaftBlock;
import java.util.LinkedList;
import java.util.List;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class CombustionBeltConnectorItem extends Item {

    public CombustionBeltConnectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(
            UseOnContext context
    ) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPosition = context.getClickedPos();
        BlockState clickedState =
                level.getBlockState(clickedPosition);

        if (AllBlocks.BELT.has(clickedState)) {
            if (player == null
                    || !isCombustionBelt(
                    level,
                    clickedPosition
            )) {
                return InteractionResult.FAIL;
            }

            BlockHitResult hitResult =
                    new BlockHitResult(
                            context.getClickLocation(),
                            context.getClickedFace(),
                            clickedPosition,
                            context.isInside()
                    );

            ItemInteractionResult extensionResult;
            try (CombustionBeltLengthOverride.Scope ignored =
                         CombustionBeltLengthOverride.push()) {
                extensionResult = BeltSlicer.useConnector(
                        clickedState,
                        level,
                        clickedPosition,
                        player,
                        context.getHand(),
                        hitResult,
                        new BeltSlicer.Feedback()
                );
            }

            if (!level.isClientSide()
                    && extensionResult.consumesAction()) {
                scheduleCombustionBeltChainMarking(
                        level,
                        clickedPosition
                );
            }

            return extensionResult.result();
        }

        if (player != null && player.isShiftKeyDown()) {
            stack.remove(AllDataComponents.BELT_FIRST_SHAFT);
            return InteractionResult.SUCCESS;
        }

        boolean clickedPositionIsValid =
                isValidBeltShaft(
                        level,
                        clickedPosition
                );

        if (level.isClientSide()) {
            return clickedPositionIsValid
                    ? InteractionResult.SUCCESS
                    : InteractionResult.FAIL;
        }

        if (player == null || !clickedPositionIsValid) {
            return InteractionResult.FAIL;
        }

        BlockPos firstShaft =
                stack.get(AllDataComponents.BELT_FIRST_SHAFT);

        if (firstShaft != null
                && (!isValidBeltShaft(
                level,
                firstShaft
        )
                || isNotWithinConfiguredLength(firstShaft, clickedPosition))) {
            stack.remove(AllDataComponents.BELT_FIRST_SHAFT);
            firstShaft = null;
        }

        if (firstShaft == null) {
            stack.set(
                    AllDataComponents.BELT_FIRST_SHAFT,
                    clickedPosition
            );
            player.getCooldowns().addCooldown(this, 5);
            return InteractionResult.SUCCESS;
        }

        if (!canConnect(
                level,
                firstShaft,
                clickedPosition
        )) {
            return InteractionResult.FAIL;
        }

        if (!firstShaft.equals(clickedPosition)) {
            createBelts(
                    level,
                    firstShaft,
                    clickedPosition
            );

            markCombustionBeltChain(level, firstShaft);
            scheduleCombustionBeltChainMarking(
                    level,
                    firstShaft
            );

            AllAdvancements.BELT.awardTo(player);

            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        if (!stack.isEmpty()) {
            stack.remove(AllDataComponents.BELT_FIRST_SHAFT);
            player.getCooldowns().addCooldown(this, 5);
        }

        return InteractionResult.SUCCESS;
    }

    private static boolean isValidBeltShaft(
            Level level,
            BlockPos position
    ) {
        if (!level.isLoaded(position)) {
            return false;
        }

        return isSupportedShaftState(
                level.getBlockState(position)
        );
    }

    public static boolean isSupportedShaftState(
            BlockState state
    ) {
        return state.getBlock()
                instanceof ThermochemicalShaftBlock;
    }

    public static boolean canConnect(
            Level level,
            BlockPos first,
            BlockPos second
    ) {
        if (!level.isLoaded(first)
                || !level.isLoaded(second)
                || isNotWithinConfiguredLength(first, second)) {
            return false;
        }

        BlockState firstState = level.getBlockState(first);
        BlockState secondState = level.getBlockState(second);

        if (!isSupportedShaftState(firstState)
                || !isSupportedShaftState(secondState)
                || !firstState.hasProperty(
                BlockStateProperties.AXIS
        )
                || !secondState.hasProperty(
                BlockStateProperties.AXIS
        )) {
            return false;
        }

        BlockPos difference = second.subtract(first);
        Axis shaftAxis = firstState.getValue(
                BlockStateProperties.AXIS
        );

        int x = difference.getX();
        int y = difference.getY();
        int z = difference.getZ();

        int equalComponents =
                (Math.abs(x) == Math.abs(y) ? 1 : 0)
                        + (Math.abs(y) == Math.abs(z) ? 1 : 0)
                        + (Math.abs(z) == Math.abs(x) ? 1 : 0);

        if (shaftAxis.choose(x, y, z) != 0
                || equalComponents != 1
                || shaftAxis != secondState.getValue(
                BlockStateProperties.AXIS
        )
                || shaftAxis == Axis.Y
                && x != 0
                && z != 0) {
            return false;
        }

        BlockEntity firstEntity =
                level.getBlockEntity(first);
        BlockEntity secondEntity =
                level.getBlockEntity(second);

        if (!(firstEntity
                instanceof KineticBlockEntity firstKinetic)
                || !(secondEntity
                instanceof KineticBlockEntity secondKinetic)) {
            return false;
        }

        float firstSpeed =
                firstKinetic.getTheoreticalSpeed();
        float secondSpeed =
                secondKinetic.getTheoreticalSpeed();

        if (Math.signum(firstSpeed)
                != Math.signum(secondSpeed)
                && firstSpeed != 0
                && secondSpeed != 0) {
            return false;
        }

        BlockPos step = BlockPos.containing(
                Math.signum(x),
                Math.signum(y),
                Math.signum(z)
        );

        for (BlockPos current = first.offset(step);
             !current.equals(second);
             current = current.offset(step)) {
            BlockState state =
                    level.getBlockState(current);

            if (isSupportedShaftState(state)
                    && state.hasProperty(
                    BlockStateProperties.AXIS
            )
                    && state.getValue(
                    BlockStateProperties.AXIS
            ) == shaftAxis) {
                continue;
            }

            if (!state.canBeReplaced()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isNotWithinConfiguredLength(
            BlockPos first,
            BlockPos second
    ) {
        int segments = Math.max(
                Math.abs(second.getX() - first.getX()),
                Math.max(
                        Math.abs(second.getY() - first.getY()),
                        Math.abs(second.getZ() - first.getZ())
                )
        );
        return !Config.unlimitedCombustionBeltLength()
                && segments >= Config.combustionBeltLengthLimit();
    }

    private static void createBelts(
            Level level,
            BlockPos start,
            BlockPos end
    ) {
        level.playSound(
                null,
                BlockPos.containing(
                        VecHelper.getCenterOf(
                                start.offset(end)
                        ).scale(0.5F)
                ),
                SoundEvents.WOOL_PLACE,
                SoundSource.BLOCKS,
                0.5F,
                1.0F
        );

        BeltSlope slope = getSlopeBetween(start, end);
        Direction facing = getFacingFromTo(start, end);
        BlockPos difference = end.subtract(start);

        if (difference.getX() == difference.getZ()) {
            facing = Direction.get(
                    facing.getAxisDirection(),
                    level.getBlockState(start)
                            .getValue(
                                    BlockStateProperties.AXIS
                            )
                            == Axis.X
                            ? Axis.Z
                            : Axis.X
            );
        }

        List<BlockPos> positions =
                getBeltChainBetween(
                        start,
                        end,
                        slope,
                        facing
                );

        BlockState beltState =
                AllBlocks.BELT.getDefaultState();

        boolean failed = false;

        for (BlockPos position : positions) {
            BlockState existingState =
                    level.getBlockState(position);

            if (existingState.getDestroySpeed(
                    level,
                    position
            ) == -1) {
                failed = true;
                break;
            }

            BeltPart part =
                    position.equals(start)
                            ? BeltPart.START
                            : position.equals(end)
                            ? BeltPart.END
                            : BeltPart.MIDDLE;

            boolean pulley =
                    isSupportedShaftState(existingState);

            if (part == BeltPart.MIDDLE && pulley) {
                part = BeltPart.PULLEY;
            }

            if (pulley
                    && existingState.hasProperty(
                    BlockStateProperties.AXIS
            )
                    && existingState.getValue(
                    BlockStateProperties.AXIS
            ) == Axis.Y) {
                slope = BeltSlope.SIDEWAYS;
            }

            if (!existingState.canBeReplaced()) {
                level.destroyBlock(position, false);
            }

            KineticBlockEntity.switchToBlockState(
                    level,
                    position,
                    ProperWaterloggedBlock.withWater(
                            level,
                            beltState
                                    .setValue(
                                            BeltBlock.SLOPE,
                                            slope
                                    )
                                    .setValue(
                                            BeltBlock.PART,
                                            part
                                    )
                                    .setValue(
                                            BeltBlock.HORIZONTAL_FACING,
                                            facing
                                    ),
                            position
                    )
            );
        }

        if (!failed) {
            return;
        }

        for (BlockPos position : positions) {
            if (AllBlocks.BELT.has(
                    level.getBlockState(position)
            )) {
                level.destroyBlock(position, false);
            }
        }
    }

    private static Direction getFacingFromTo(
            BlockPos start,
            BlockPos end
    ) {
        Axis beltAxis =
                start.getX() == end.getX()
                        ? Axis.Z
                        : Axis.X;

        BlockPos difference = end.subtract(start);
        AxisDirection axisDirection =
                AxisDirection.POSITIVE;

        if (difference.getX() == 0
                && difference.getZ() == 0) {
            axisDirection =
                    difference.getY() > 0
                            ? AxisDirection.POSITIVE
                            : AxisDirection.NEGATIVE;
        } else if (beltAxis.choose(
                difference.getX(),
                0,
                difference.getZ()
        ) <= 0) {
            axisDirection = AxisDirection.NEGATIVE;
        }

        return Direction.get(
                axisDirection,
                beltAxis
        );
    }

    private static BeltSlope getSlopeBetween(
            BlockPos start,
            BlockPos end
    ) {
        BlockPos difference = end.subtract(start);

        if (difference.getY() != 0) {
            if (difference.getZ() != 0
                    || difference.getX() != 0) {
                return difference.getY() > 0
                        ? BeltSlope.UPWARD
                        : BeltSlope.DOWNWARD;
            }

            return BeltSlope.VERTICAL;
        }

        return BeltSlope.HORIZONTAL;
    }

    private static List<BlockPos> getBeltChainBetween(
            BlockPos start,
            BlockPos end,
            BeltSlope slope,
            Direction direction
    ) {
        List<BlockPos> positions =
                new LinkedList<>();

        BlockPos current = start;

        do {
            positions.add(current);

            if (slope == BeltSlope.VERTICAL) {
                current = current.above(
                        direction.getAxisDirection()
                                == AxisDirection.POSITIVE
                                ? 1
                                : -1
                );
                continue;
            }

            current = current.relative(direction);

            if (slope != BeltSlope.HORIZONTAL) {
                current = current.above(
                        slope == BeltSlope.UPWARD
                                ? 1
                                : -1
                );
            }
        } while (!current.equals(end));

        positions.add(end);
        return positions;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isCombustionBelt(
            Level level,
            BlockPos position
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(position);

        return blockEntity
                instanceof CombustionBeltAccessor accessor
                && accessor
                .sulfuricresonance$isCombustionBelt();
    }

    private static void scheduleCombustionBeltChainMarking(
            Level level,
            BlockPos beltPosition
    ) {
        MinecraftServer server = level.getServer();

        if (server == null) {
            return;
        }

        BlockPos savedPosition =
                beltPosition.immutable();

        int currentTick = server.getTickCount();

        for (int delay : new int[]{1, 2, 5}) {
            server.tell(
                    new TickTask(
                            currentTick + delay,
                            () -> markCombustionBeltChain(
                                    level,
                                    savedPosition
                            )
                    )
            );
        }
    }

    private static void markCombustionBeltChain(
            Level level,
            BlockPos beltPosition
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(beltPosition);

        if (!(blockEntity
                instanceof BeltBlockEntity belt)) {
            return;
        }

        BlockPos controllerPosition =
                belt.getController();

        for (BlockPos segmentPosition :
                BeltBlock.getBeltChain(
                        level,
                        controllerPosition
                )) {
            BlockEntity segmentEntity =
                    level.getBlockEntity(segmentPosition);

            if (!(segmentEntity
                    instanceof BeltBlockEntity segment)
                    || !(segment
                    instanceof CombustionBeltAccessor accessor)) {
                continue;
            }

            accessor.sulfuricresonance$setCombustionBelt(
                    true
            );
            accessor.sulfuricresonance$setThermochemicalPulley(
                    segment.hasPulley()
            );
            segment.setChanged();
            segment.sendData();
        }
    }
}
