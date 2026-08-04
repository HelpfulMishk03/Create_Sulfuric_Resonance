package io.hxneyw.repo.content.blocks.combustionbelt;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalConduitBlockEntity;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves live heat for one complete marked Combustion Belt chain.
 *
 * Heat is accepted only through a real Create kinetic path attached to one of
 * the belt's pulley segments.
 *
 * Shafts, gearboxes, cogwheels, chain drives, clutches, gearshifts, speed
 * controllers, and compatible custom kinetic connections may exist between the
 * pulley and the Thermochemical Conduit.
 *
 * Belt block entities are never traversed. A belt may receive heat at a pulley,
 * but it cannot relay heat from one pulley to the other.
 */
public final class CombustionBeltHeatResolver {

    private CombustionBeltHeatResolver() {
    }

    public static Result resolveChain(
            Level level,
            BeltBlockEntity controller
    ) {
        if (level.isClientSide()
                || controller == null
                || !controller.isController()
                || controller.beltLength <= 0) {
            return Result.NONE;
        }

        Result bestResult = Result.NONE;

        for (int segment = 0;
             segment < controller.beltLength;
             segment++) {

            BlockPos pulleyPosition =
                    BeltHelper.getPositionForOffset(
                            controller,
                            segment
                    );

            if (!level.isLoaded(pulleyPosition)) {
                continue;
            }

            BlockEntity blockEntity =
                    level.getBlockEntity(pulleyPosition);

            if (!(blockEntity
                    instanceof BeltBlockEntity pulley)
                    || !pulley.hasPulley()
                    || !(pulley
                    instanceof CombustionBeltAccessor accessor)
                    || !accessor
                    .sulfuricresonance$isCombustionBelt()) {
                continue;
            }

            bestResult = chooseBetterResult(
                    bestResult,
                    resolveFromPulley(
                            level,
                            pulley
                    )
            );
        }

        return bestResult;
    }

    /**
     * Starts at one real belt pulley, exits through its actual shaft connection,
     * and searches only the physically attached non-belt kinetic branch.
     */
    private static Result resolveFromPulley(
            Level level,
            BeltBlockEntity pulley
    ) {
        if (!pulley.hasNetwork()
                || pulley.network == null) {
            return Result.NONE;
        }

        Long targetNetworkId = pulley.network;

        ArrayDeque<KineticBlockEntity> pending =
                new ArrayDeque<>();

        Set<BlockPos> visited =
                new HashSet<>();

        visited.add(pulley.getBlockPos());

        /*
         * A belt is allowed only as the starting receiver. Every neighbouring
         * BeltBlockEntity is rejected, preventing traversal across the belt.
         */
        for (KineticBlockEntity neighbour
                : getConnectedNeighbours(
                        level,
                        pulley,
                        targetNetworkId
                )) {

            if (neighbour instanceof BeltBlockEntity) {
                continue;
            }

            if (visited.add(neighbour.getBlockPos())) {
                pending.addLast(neighbour);
            }
        }

        Result bestResult = Result.NONE;

        while (!pending.isEmpty()) {
            KineticBlockEntity current =
                    pending.removeFirst();

            if (current.isRemoved()
                    || current.getLevel() != level
                    || !targetNetworkId.equals(
                    current.network
            )
                    || current instanceof BeltBlockEntity) {
                continue;
            }

            if (current
                    instanceof ThermochemicalConduitBlockEntity
                    conduit) {

                MoltenRotorBlockEntity.RotorHeatLevel
                        liveHeatTier =
                        conduit.getLiveValidatedHeatTier();

                if (liveHeatTier
                        != MoltenRotorBlockEntity
                        .RotorHeatLevel.NONE) {

                    bestResult = chooseBetterResult(
                            bestResult,
                            new Result(
                                    liveHeatTier,
                                    conduit.getBlockPos()
                                            .immutable(),
                                    true,
                                    distanceSquared(
                                            pulley.getBlockPos(),
                                            conduit.getBlockPos()
                                    )
                            )
                    );
                }
            }

            for (KineticBlockEntity neighbour
                    : getConnectedNeighbours(
                            level,
                            current,
                            targetNetworkId
                    )) {

                if (neighbour instanceof BeltBlockEntity) {
                    continue;
                }

                if (visited.add(neighbour.getBlockPos())) {
                    pending.addLast(neighbour);
                }
            }
        }

        return bestResult;
    }

    /**
     * Reproduces Create's kinetic neighbour discovery using its public
     * connection test. This supports standard adjacent shaft connections plus
     * diagonal and custom propagation locations supplied by kinetic blocks.
     */
    private static List<KineticBlockEntity>
    getConnectedNeighbours(
            Level level,
            KineticBlockEntity current,
            Long targetNetworkId
    ) {
        List<BlockPos> potentialLocations =
                new ArrayList<>();

        BlockPos currentPosition =
                current.getBlockPos();

        for (Direction direction : Direction.values()) {
            BlockPos neighbourPosition =
                    currentPosition.relative(direction);

            if (level.isLoaded(neighbourPosition)) {
                potentialLocations.add(
                        neighbourPosition
                );
            }
        }

        BlockState currentState =
                current.getBlockState();

        if (currentState.getBlock()
                instanceof IRotate rotatingBlock) {

            potentialLocations =
                    current.addPropagationLocations(
                            rotatingBlock,
                            currentState,
                            potentialLocations
                    );
        }

        Set<BlockPos> uniqueLocations =
                new LinkedHashSet<>(
                        potentialLocations
                );

        List<KineticBlockEntity> connected =
                new ArrayList<>();

        for (BlockPos neighbourPosition
                : uniqueLocations) {

            if (!level.isLoaded(neighbourPosition)) {
                continue;
            }

            BlockEntity blockEntity =
                    level.getBlockEntity(
                            neighbourPosition
                    );

            if (!(blockEntity
                    instanceof KineticBlockEntity neighbour)
                    || neighbour.isRemoved()
                    || neighbour.getLevel() != level
                    || !targetNetworkId.equals(
                    neighbour.network
            )) {
                continue;
            }

            boolean connectedEitherWay =
                    RotationPropagator.isConnected(
                            current,
                            neighbour
                    )
                            || RotationPropagator.isConnected(
                                    neighbour,
                                    current
                            );

            if (connectedEitherWay) {
                connected.add(neighbour);
            }
        }

        return connected;
    }

    private static Result chooseBetterResult(
            Result current,
            Result candidate
    ) {
        if (candidate == null
                || candidate.heatTier()
                == MoltenRotorBlockEntity
                .RotorHeatLevel.NONE
                || candidate.sourcePosition() == null) {
            return current;
        }

        if (current.heatTier()
                == MoltenRotorBlockEntity
                .RotorHeatLevel.NONE
                || current.sourcePosition() == null) {
            return candidate;
        }

        if (candidate.heatTier().rank
                > current.heatTier().rank) {
            return candidate;
        }

        if (candidate.heatTier().rank
                < current.heatTier().rank) {
            return current;
        }

        if (candidate.heatTier().displayTemp
                > current.heatTier().displayTemp) {
            return candidate;
        }

        if (candidate.heatTier().displayTemp
                < current.heatTier().displayTemp) {
            return current;
        }

        return candidate.distanceSquared()
                < current.distanceSquared()
                ? candidate
                : current;
    }

    private static long distanceSquared(
            BlockPos first,
            BlockPos second
    ) {
        long deltaX =
                (long) first.getX() - second.getX();

        long deltaY =
                (long) first.getY() - second.getY();

        long deltaZ =
                (long) first.getZ() - second.getZ();

        return deltaX * deltaX
                + deltaY * deltaY
                + deltaZ * deltaZ;
    }

    public record Result(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            @Nullable BlockPos sourcePosition,
            boolean fromConduit,
            long distanceSquared
    ) {
        public static final Result NONE =
                new Result(
                        MoltenRotorBlockEntity
                                .RotorHeatLevel.NONE,
                        null,
                        false,
                        Long.MAX_VALUE
                );
    }
}
