package io.hxneyw.repo.content.blocks.thermochemicalconduit;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
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
 * Resolves live Molten Rotor heat through Create's actual kinetic connections.
 *
 * Thermal rules:
 *
 * 1. Shafts, gearboxes, cogwheels, chain drives, clutches, gearshifts, speed
 *    controllers, and custom Create kinetic connections may carry heat while
 *    they are genuinely connected in the same active kinetic network.
 *
 * 2. There is no arbitrary shaft-distance limit.
 *
 * 3. Any Create Belt block entity is a thermal endpoint. Belts may receive heat
 *    from a charged Thermochemical Conduit, but heat traversal never enters or
 *    crosses a belt. This prevents a belt from transmitting heat to machinery
 *    attached to its opposite pulley.
 *
 * 4. The hottest reachable live Molten Rotor Furnace wins. Equal heat tiers
 *    prefer the source nearest to the target conduit.
 */
public final class ThermochemicalHeatResolver {

    /**
     * Charged conduit output range to belts and later thermal consumers.
     */
    public static final int CONDUIT_OUTPUT_RANGE = 10;

    private ThermochemicalHeatResolver() {
    }

    public static Result resolve(
            ThermochemicalConduitBlockEntity targetConduit
    ) {
        Level level = targetConduit.getLevel();

        if (level == null
                || level.isClientSide
                || !targetConduit.hasNetwork()
                || targetConduit.network == null) {
            return Result.NONE;
        }

        Long targetNetworkId = targetConduit.network;

        ArrayDeque<KineticBlockEntity> pending =
                new ArrayDeque<>();

        Set<BlockPos> visited =
                new HashSet<>();

        pending.addLast(targetConduit);
        visited.add(targetConduit.getBlockPos());

        Result bestResult = Result.NONE;

        while (!pending.isEmpty()) {
            KineticBlockEntity current =
                    pending.removeFirst();

            if (current.isRemoved()
                    || current.getLevel() != level
                    || !targetNetworkId.equals(
                            current.network
                    )) {
                continue;
            }

            /*
             * Belts are receiver-only thermal endpoints. They are never added
             * to the traversal queue, but retain this guard for safety.
             */
            if (current instanceof BeltBlockEntity) {
                continue;
            }

            if (current instanceof MoltenRotorBlockEntity furnace) {
                bestResult = chooseBetterSource(
                        bestResult,
                        furnace,
                        targetConduit.getBlockPos()
                );
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
     * Reproduces Create's propagation-neighbour discovery using its public
     * connection test. The six adjacent positions are always examined, then
     * the current block entity may add diagonal/custom propagation locations.
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

            /*
             * RotationPropagator.isConnected covers integrated shafts,
             * gearboxes, cogs, chain drives, speed controllers, and custom
             * propagation relationships.
             */
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

    private static Result chooseBetterSource(
            Result current,
            MoltenRotorBlockEntity furnace,
            BlockPos targetPosition
    ) {
        MoltenRotorBlockEntity.RotorHeatLevel candidateTier =
                furnace.getCurrentHeatTier();

        if (candidateTier
                == MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
            return current;
        }

        BlockPos candidatePosition =
                furnace.getBlockPos();

        if (current.heatTier()
                == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                || current.sourcePos() == null) {

            return resultFor(
                    candidateTier,
                    candidatePosition
            );
        }

        int heatComparison = compareHeat(
                candidateTier,
                current.heatTier()
        );

        if (heatComparison > 0) {
            return resultFor(
                    candidateTier,
                    candidatePosition
            );
        }

        if (heatComparison < 0) {
            return current;
        }

        long candidateDistance =
                distanceSquared(
                        candidatePosition,
                        targetPosition
                );

        long currentDistance =
                distanceSquared(
                        current.sourcePos(),
                        targetPosition
                );

        if (candidateDistance < currentDistance) {
            return resultFor(
                    candidateTier,
                    candidatePosition
            );
        }

        return current;
    }

    private static Result resultFor(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            BlockPos sourcePosition
    ) {
        return new Result(
                heatTier,
                sourcePosition.immutable()
        );
    }

    public static boolean isWithinRange(
            BlockPos source,
            BlockPos target,
            int range
    ) {
        return distanceSquared(source, target)
                <= (long) range * range;
    }

    private static long distanceSquared(
            BlockPos source,
            BlockPos target
    ) {
        long deltaX =
                (long) source.getX() - target.getX();

        long deltaY =
                (long) source.getY() - target.getY();

        long deltaZ =
                (long) source.getZ() - target.getZ();

        return deltaX * deltaX
                + deltaY * deltaY
                + deltaZ * deltaZ;
    }

    private static int compareHeat(
            MoltenRotorBlockEntity.RotorHeatLevel first,
            MoltenRotorBlockEntity.RotorHeatLevel second
    ) {
        int rankComparison =
                Integer.compare(
                        first.rank,
                        second.rank
                );

        if (rankComparison != 0) {
            return rankComparison;
        }

        return Integer.compare(
                first.displayTemp,
                second.displayTemp
        );
    }

    public record Result(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            @Nullable BlockPos sourcePos
    ) {
        public static final Result NONE =
                new Result(
                        MoltenRotorBlockEntity
                                .RotorHeatLevel.NONE,
                        null
                );
    }
}
