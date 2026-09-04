package io.hxneyw.repo.content.blocks.thermochemicalconduit;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltHeatResolver;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.blocks.thermochemicalcogwheel.ThermochemicalCogwheelBlock;
import io.hxneyw.repo.content.blocks.thermochemicallinkdrive.ThermochemicalLinkDriveBlock;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.compat.create.ThermochemicalBoilerInterfaceCompat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public final class ThermochemicalHeatResolver {
    public static final int SOURCE_ALLOWANCE = 3;
    public static final int CONDUIT_ALLOWANCE = 10;
    private static final int MAX_INHERITED_STEPS = 4096;

    private ThermochemicalHeatResolver() {
    }

    public static Result resolve(@Nullable BlockEntity target) {
        if (target == null) {
            return Result.NONE;
        }
        return resolve(
                target.getLevel(),
                target.getBlockPos(),
                true
        );
    }

    public static Result resolveNetworkOnly(
            @Nullable Level level,
            @Nullable BlockPos startPosition
    ) {
        return resolve(
                level,
                startPosition,
                false
        );
    }

    private static Result resolve(
            @Nullable Level level,
            @Nullable BlockPos startPosition,
            boolean allowDirectBeltSource
    ) {
        if (level == null
                || level.isClientSide
                || startPosition == null
                || !level.isLoaded(startPosition)) {
            return Result.NONE;
        }

        BlockState startState = level.getBlockState(startPosition);
        if (!isAllowedNode(startState)) {
            return Result.NONE;
        }

        BlockEntity startEntity = level.getBlockEntity(startPosition);
        if (!(startEntity instanceof KineticBlockEntity)) {
            return Result.NONE;
        }

        List<BlockPos> targetToSource = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        BlockPos currentPosition = startPosition.immutable();
        int totalDistance = 0;

        for (int step = 0; step < MAX_INHERITED_STEPS; step++) {
            if (!level.isLoaded(currentPosition)
                    || !visited.add(currentPosition)) {
                return Result.NONE;
            }

            BlockState currentState = level.getBlockState(currentPosition);
            if (!isAllowedNode(currentState)) {
                return Result.NONE;
            }

            BlockEntity currentEntity = level.getBlockEntity(currentPosition);
            if (!(currentEntity instanceof KineticBlockEntity currentKinetic)) {
                return Result.NONE;
            }

            targetToSource.add(currentPosition.immutable());

            DirectSteamSource directSteamSource =
                    findDirectLinkDriveSteamSource(
                            level,
                            currentPosition,
                            currentState
                    );
            if (directSteamSource != null) {
                Result directResult = buildResult(
                        level,
                        targetToSource,
                        directSteamSource.heatTier(),
                        directSteamSource.sourcePosition(),
                        directSteamSource.temperature(),
                        totalDistance + 1
                );
                if (directResult.heatTier()
                        != MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
                    return directResult;
                }
            }

            if (isRotationSpeedController(currentState)) {
                if (doesNotHaveThermochemicalControllerCog(
                        level,
                        currentPosition
                )) {
                    return Result.NONE;
                }

                return resolveThroughSpeedController(
                        level,
                        currentPosition,
                        targetToSource,
                        visited,
                        totalDistance
                );
            }

            BlockPos sourcePosition = currentKinetic.source;
            if (sourcePosition == null
                    || !level.isLoaded(sourcePosition)) {
                return Result.NONE;
            }

            BlockEntity sourceEntity = level.getBlockEntity(sourcePosition);
            totalDistance++;

            if (sourceEntity instanceof PoweredShaftBlockEntity poweredShaft) {
                if (lacksInheritedConnection(
                        level,
                        sourcePosition,
                        currentPosition
                )) {
                    return Result.NONE;
                }

                ThermochemicalBoilerInterfaceCompat.SteamSource steamSource =
                        ThermochemicalBoilerInterfaceCompat.resolveSteamSource(
                                poweredShaft
                        );
                if (!steamSource.active()) {
                    return Result.NONE;
                }

                return buildResult(
                        level,
                        targetToSource,
                        steamSource.heatTier(),
                        sourcePosition,
                        steamSource.temperature(),
                        totalDistance
                );
            }

            if (sourceEntity instanceof MoltenRotorBlockEntity furnace) {
                if (lacksInheritedConnection(
                        level,
                        sourcePosition,
                        currentPosition
                )) {
                    return Result.NONE;
                }
                return buildResult(
                        level,
                        targetToSource,
                        furnace.getCurrentHeatTier(),
                        sourcePosition,
                        furnace.getDisplayTemperature(),
                        totalDistance
                );
            }

            if (sourceEntity instanceof BeltBlockEntity belt) {
                if (!allowDirectBeltSource
                        || !isMarkedPulley(belt)
                        || lacksInheritedConnection(
                        level,
                        sourcePosition,
                        currentPosition
                )) {
                    return Result.NONE;
                }

                BeltBlockEntity controller = BeltHelper.getControllerBE(
                        level,
                        sourcePosition
                );
                if (controller == null
                        || !controller.isController()
                        || controller.beltLength <= 0) {
                    return Result.NONE;
                }

                CombustionBeltHeatResolver.Result beltResult =
                        CombustionBeltHeatResolver.resolveRelaySource(
                                level,
                                controller,
                                currentPosition
                        );
                if (beltResult.heatTier()
                        == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                        || beltResult.sourcePosition() == null) {
                    return Result.NONE;
                }

                int temperature = 0;
                BlockEntity furnaceEntity = level.getBlockEntity(
                        beltResult.sourcePosition()
                );
                if (furnaceEntity instanceof MoltenRotorBlockEntity furnace) {
                    temperature = furnace.getDisplayTemperature();
                }

                long combinedDistance = totalDistance
                        + beltResult.distanceSquared();
                int resolvedDistance = combinedDistance >= Integer.MAX_VALUE
                        ? Integer.MAX_VALUE
                        : (int) combinedDistance;

                return buildResult(
                        level,
                        targetToSource,
                        beltResult.heatTier(),
                        beltResult.sourcePosition(),
                        temperature,
                        resolvedDistance
                );
            }

            BlockState sourceState =
                    level.getBlockState(sourcePosition);

            if (requiresImmediateConduit(sourceState)
                    && !(currentState.getBlock()
                    instanceof ThermochemicalConduitBlock)) {
                return Result.NONE;
            }

            if (!(sourceEntity instanceof KineticBlockEntity)
                    || !isAllowedNode(sourceState)
                    || lacksInheritedConnection(
                    level,
                    sourcePosition,
                    currentPosition
            )) {
                return Result.NONE;
            }

            currentPosition = sourcePosition.immutable();
        }

        return Result.NONE;
    }

    private static @Nullable DirectSteamSource
    findDirectLinkDriveSteamSource(
            Level level,
            BlockPos linkDrivePosition,
            BlockState linkDriveState
    ) {
        if (!(linkDriveState.getBlock()
                instanceof ThermochemicalLinkDriveBlock)) {
            return null;
        }

        Direction.Axis axis = linkDriveState.getValue(
                BlockStateProperties.AXIS
        );
        DirectSteamSource selected = null;

        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != axis) {
                continue;
            }

            BlockPos sourcePosition = linkDrivePosition.relative(direction);
            if (!level.isLoaded(sourcePosition)
                    || !hasLinkDriveShaftConnection(
                    level,
                    linkDrivePosition,
                    linkDriveState,
                    direction
            )) {
                continue;
            }

            BlockEntity sourceEntity = level.getBlockEntity(sourcePosition);
            if (!(sourceEntity instanceof PoweredShaftBlockEntity poweredShaft)) {
                continue;
            }

            ThermochemicalBoilerInterfaceCompat.SteamSource steamSource =
                    ThermochemicalBoilerInterfaceCompat.resolveSteamSource(
                            poweredShaft
                    );
            if (!steamSource.active()) {
                continue;
            }

            DirectSteamSource candidate = new DirectSteamSource(
                    sourcePosition.immutable(),
                    steamSource.heatTier(),
                    steamSource.temperature()
            );
            if (selected == null
                    || isBetterDirectSteamSource(candidate, selected)) {
                selected = candidate;
            }
        }

        return selected;
    }

    private static boolean isBetterDirectSteamSource(
            DirectSteamSource candidate,
            DirectSteamSource selected
    ) {
        int tier = Integer.compare(
                candidate.heatTier().rank,
                selected.heatTier().rank
        );
        if (tier != 0) {
            return tier > 0;
        }

        int temperature = Integer.compare(
                candidate.temperature(),
                selected.temperature()
        );
        if (temperature != 0) {
            return temperature > 0;
        }

        return Long.compare(
                candidate.sourcePosition().asLong(),
                selected.sourcePosition().asLong()
        ) < 0;
    }

    public static @Nullable InheritedBeltSource
    findInheritedBeltSource(
            Level level,
            BlockPos startPosition
    ) {
        if (level.isClientSide
                || !level.isLoaded(startPosition)) {
            return null;
        }

        BlockState startState =
                level.getBlockState(startPosition);

        if (!isBeltHeatInterface(startState)) {
            return null;
        }

        BlockEntity startEntity =
                level.getBlockEntity(startPosition);

        if (!(startEntity instanceof KineticBlockEntity)) {
            return null;
        }

        Set<BlockPos> visited = new HashSet<>();
        BlockPos currentPosition =
                startPosition.immutable();
        int distance = 0;

        for (int step = 0;
             step < MAX_INHERITED_STEPS;
             step++) {
            if (!level.isLoaded(currentPosition)
                    || !visited.add(currentPosition)) {
                return null;
            }

            BlockState currentState =
                    level.getBlockState(currentPosition);
            BlockEntity currentEntity =
                    level.getBlockEntity(currentPosition);

            if (!(currentEntity
                    instanceof KineticBlockEntity currentKinetic)
                    || !isAllowedNode(currentState)) {
                return null;
            }

            BlockPos sourcePosition =
                    currentKinetic.source;

            if (sourcePosition == null
                    || !level.isLoaded(sourcePosition)) {
                return null;
            }

            BlockEntity sourceEntity =
                    level.getBlockEntity(sourcePosition);
            distance++;

            if (sourceEntity instanceof BeltBlockEntity belt) {
                if (!isMarkedPulley(belt)
                        || lacksInheritedConnection(
                        level,
                        sourcePosition,
                        currentPosition
                )) {
                    return null;
                }

                BeltBlockEntity controller =
                        BeltHelper.getControllerBE(
                                level,
                                sourcePosition
                        );

                if (controller == null
                        || !controller.isController()
                        || controller.beltLength <= 0) {
                    return null;
                }

                return new InheritedBeltSource(
                        controller.getBlockPos().immutable(),
                        distance
                );
            }

            if (sourceEntity
                    instanceof MoltenRotorBlockEntity) {
                return null;
            }

            BlockState sourceState =
                    level.getBlockState(sourcePosition);

            if (requiresImmediateConduit(sourceState)
                    && !(currentState.getBlock()
                    instanceof ThermochemicalConduitBlock)) {
                return null;
            }

            if (!(sourceEntity instanceof KineticBlockEntity)
                    || !isAllowedNode(sourceState)
                    || lacksInheritedConnection(
                    level,
                    sourcePosition,
                    currentPosition
            )) {
                return null;
            }

            currentPosition =
                    sourcePosition.immutable();
        }

        return null;
    }

    private static Result resolveThroughSpeedController(
            Level level,
            BlockPos controllerPosition,
            List<BlockPos> targetToController,
            Set<BlockPos> alreadyVisited,
            int baseDistance
    ) {
        Queue<PhysicalStep> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>(alreadyVisited);

        visited.remove(controllerPosition);

        pending.add(new PhysicalStep(
                controllerPosition.immutable(),
                List.of(),
                0
        ));

        for (int stepCount = 0;
             stepCount < MAX_INHERITED_STEPS && !pending.isEmpty();
             stepCount++) {

            PhysicalStep step = pending.remove();
            BlockPos position = step.position();

            if (!level.isLoaded(position)
                    || !visited.add(position)) {
                continue;
            }

            BlockState state = level.getBlockState(position);
            if (isNotSpeedControllerBridgeNode(level, position, state)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos furnacePosition = position.relative(direction);

                if (!level.isLoaded(furnacePosition)) {
                    continue;
                }

                BlockEntity furnaceEntity =
                        level.getBlockEntity(furnacePosition);

                if (!(furnaceEntity instanceof MoltenRotorBlockEntity furnace)
                        || lacksInheritedConnection(
                        level,
                        furnacePosition,
                        position
                )) {
                    continue;
                }

                List<BlockPos> fullPath =
                        new ArrayList<>(targetToController);
                fullPath.addAll(step.pathFromController());

                Result result = buildResult(
                        level,
                        fullPath,
                        furnace.getCurrentHeatTier(),
                        furnacePosition,
                        furnace.getDisplayTemperature(),
                        baseDistance + step.distanceFromController() + 1
                );

                if (result.heatTier()
                        != MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
                    return result;
                }
            }

            for (BlockPos neighbour :
                    connectedSpeedControllerBridgeNeighbours(
                            level,
                            position
                    )) {
                if (visited.contains(neighbour)) {
                    continue;
                }

                List<BlockPos> nextPath =
                        new ArrayList<>(step.pathFromController());

                if (!neighbour.equals(controllerPosition)) {
                    nextPath.add(neighbour.immutable());
                }

                pending.add(new PhysicalStep(
                        neighbour.immutable(),
                        List.copyOf(nextPath),
                        step.distanceFromController() + 1
                ));
            }
        }

        return Result.NONE;
    }

    private static boolean isNotSpeedControllerBridgeNode(
            Level level,
            BlockPos position,
            BlockState state
    ) {
        if (isRotationSpeedController(state)) {
            return doesNotHaveThermochemicalControllerCog(
                    level,
                    position
            );
        }

        return !(state.getBlock() instanceof ThermochemicalConnection);
    }

    private static boolean doesNotHaveThermochemicalControllerCog(
            Level level,
            BlockPos controllerPosition
    ) {
        BlockPos cogPosition = controllerPosition.above();

        if (!level.isLoaded(cogPosition)) {
            return true;
        }

        BlockState cogState = level.getBlockState(cogPosition);
        if (!cogState.is(
                AllModBlocks.LARGE_THERMOCHEMICAL_COGWHEEL.get()
        )) {
            return true;
        }

        BlockEntity controllerEntity =
                level.getBlockEntity(controllerPosition);
        BlockEntity cogEntity =
                level.getBlockEntity(cogPosition);

        if (!(controllerEntity instanceof KineticBlockEntity controllerKinetic)
                || !(cogEntity instanceof KineticBlockEntity cogKinetic)) {
            return true;
        }

        return !RotationPropagator.isConnected(
                controllerKinetic,
                cogKinetic
        ) && !RotationPropagator.isConnected(
                cogKinetic,
                controllerKinetic
        );
    }

    private static List<BlockPos>
    connectedSpeedControllerBridgeNeighbours(
            Level level,
            BlockPos position
    ) {
        BlockState state = level.getBlockState(position);
        BlockEntity blockEntity = level.getBlockEntity(position);

        if (!(blockEntity instanceof KineticBlockEntity kinetic)
                || !(state.getBlock() instanceof IRotate rotate)) {
            return List.of();
        }

        Set<BlockPos> potentialPositions =
                new LinkedHashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos neighbour = position.relative(direction);
            if (level.isLoaded(neighbour)) {
                potentialPositions.add(neighbour.immutable());
            }
        }

        potentialPositions.addAll(
                kinetic.addPropagationLocations(
                        rotate,
                        state,
                        new ArrayList<>(potentialPositions)
                )
        );

        List<BlockPos> neighbours = new ArrayList<>();

        for (BlockPos neighbour : potentialPositions) {
            if (neighbour.equals(position)
                    || !level.isLoaded(neighbour)) {
                continue;
            }

            BlockState neighbourState =
                    level.getBlockState(neighbour);

            if (isNotSpeedControllerBridgeNode(level, neighbour, neighbourState)) {
                continue;
            }

            if (hasPhysicalConnection(
                    level,
                    position,
                    neighbour
            )) {
                neighbours.add(neighbour.immutable());
            }
        }

        return neighbours;
    }

    private static Result buildResult(
            Level level,
            List<BlockPos> targetToSource,
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            BlockPos sourcePosition,
            int temperature,
            int totalDistance
    ) {
        if (heatTier == MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
            return Result.NONE;
        }

        int spanLimit = SOURCE_ALLOWANCE;
        int spanUsed = 0;

        for (int index = targetToSource.size() - 1;
             index >= 0;
             index--) {
            BlockState state = level.getBlockState(
                    targetToSource.get(index)
            );

            if (state.getBlock() instanceof ThermochemicalConduitBlock) {
                spanLimit = CONDUIT_ALLOWANCE;
                spanUsed = 0;
                continue;
            }

            int cost = transmissionCost(state);
            if (cost == Integer.MAX_VALUE) {
                return Result.NONE;
            }

            spanUsed += cost;
            if (spanUsed > spanLimit) {
                return Result.NONE;
            }
        }

        return new Result(
                heatTier,
                sourcePosition.immutable(),
                spanUsed,
                spanLimit,
                spanLimit - spanUsed,
                temperature,
                totalDistance
        );
    }

    public static boolean isAllowedNode(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof ThermochemicalConnection) {
            return true;
        }

        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (!id.getNamespace().equals("create")) {
            return false;
        }

        if (id.getPath().equals("cogwheel")
                || id.getPath().equals("large_cogwheel")) {
            return false;
        }

        return switch (id.getPath()) {
            case "rotation_speed_controller",
                 "clutch",
                 "gearshift",
                 "adjustable_chain_gearshift" -> true;
            default -> false;
        };
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isBeltHeatInterface(
            BlockState state
    ) {
        return state.getBlock()
                instanceof ThermochemicalConnection;
    }

    private static boolean isRotationSpeedController(
            BlockState state
    ) {
        ResourceLocation id =
                BuiltInRegistries.BLOCK.getKey(state.getBlock());

        return id.getNamespace().equals("create")
                && id.getPath().equals("rotation_speed_controller");
    }

    private static boolean requiresImmediateConduit(
            BlockState state
    ) {
        Block block = state.getBlock();
        ResourceLocation id =
                BuiltInRegistries.BLOCK.getKey(block);

        if (!id.getNamespace().equals("create")) {
            return false;
        }

        return switch (id.getPath()) {
            case "clutch",
                 "gearshift",
                 "adjustable_chain_gearshift" -> true;
            default -> false;
        };
    }

    public static int transmissionCost(BlockState state) {
        if (state.getBlock() instanceof ThermochemicalConduitBlock) {
            return 0;
        }
        return isAllowedNode(state) ? 1 : Integer.MAX_VALUE;
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isBridgeConnector(
            BlockState state
    ) {
        return state.getBlock()
                instanceof ThermochemicalConnection;
    }

    public static boolean hasPhysicalConnection(
            Level level,
            BlockPos firstPosition,
            BlockPos secondPosition
    ) {
        BlockEntity firstEntity = level.getBlockEntity(firstPosition);
        BlockEntity secondEntity = level.getBlockEntity(secondPosition);
        if (!(firstEntity instanceof KineticBlockEntity firstKinetic)
                || !(secondEntity instanceof KineticBlockEntity secondKinetic)) {
            return false;
        }

        if (!RotationPropagator.isConnected(firstKinetic, secondKinetic)
                && !RotationPropagator.isConnected(
                secondKinetic,
                firstKinetic
        )) {
            return false;
        }

        BlockState firstState = level.getBlockState(firstPosition);
        BlockState secondState = level.getBlockState(secondPosition);

        if (firstState.getBlock() instanceof ThermochemicalCogwheelBlock
                && secondState.getBlock() instanceof ThermochemicalCogwheelBlock) {
            return true;
        }

        Direction direction = directionBetween(
                firstPosition,
                secondPosition
        );
        if (direction == null) {
            return false;
        }

        if (areLinkDrivesSegmentConnected(
                firstState,
                secondState
        )) {
            return true;
        }

        if (isRotationSpeedController(firstState)
                || isRotationSpeedController(secondState)) {
            BlockPos controllerPosition =
                    isRotationSpeedController(firstState)
                            ? firstPosition
                            : secondPosition;

            if (doesNotHaveThermochemicalControllerCog(
                    level,
                    controllerPosition
            )) {
                return false;
            }

            if (isAllowedNode(firstState)
                    && isAllowedNode(secondState)) {
                return true;
            }
        }

        boolean thermochemicalEdge = firstState.getBlock()
                instanceof ThermochemicalConnection
                || secondState.getBlock()
                instanceof ThermochemicalConnection;

        if (!thermochemicalEdge) {
            return true;
        }

        if (firstState.getBlock()
                instanceof ThermochemicalConnection firstThermal
                && firstThermal.doesNotHaveThermochemicalConnection(
                firstState,
                direction
        )) {
            return false;
        }

        if (secondState.getBlock()
                instanceof ThermochemicalConnection secondThermal
                && secondThermal.doesNotHaveThermochemicalConnection(
                secondState,
                direction.getOpposite()
        )) {
            return false;
        }

        if (!(firstState.getBlock() instanceof IRotate firstRotate)
                || !(secondState.getBlock() instanceof IRotate secondRotate)) {
            return false;
        }

        return firstRotate.hasShaftTowards(
                level,
                firstPosition,
                firstState,
                direction
        ) && secondRotate.hasShaftTowards(
                level,
                secondPosition,
                secondState,
                direction.getOpposite()
        );
    }

    private static boolean lacksInheritedConnection(
            Level level,
            BlockPos sourcePosition,
            BlockPos targetPosition
    ) {
        return !hasPhysicalConnection(
                level,
                sourcePosition,
                targetPosition
        );
    }

    public static int countDirectThermochemicalConnections(
            Level level,
            BlockPos position
    ) {
        int connections = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = position.relative(direction);
            if (!level.isLoaded(neighbour)) {
                continue;
            }

            BlockState neighbourState = level.getBlockState(neighbour);
            if (!isAllowedNode(neighbourState)
                    && !isCombustionBeltPulley(level, neighbour)) {
                continue;
            }

            if (hasPhysicalConnection(level, position, neighbour)) {
                connections++;
            }
        }
        return connections;
    }

    public static boolean areLinkDrivesSegmentConnected(
            BlockState firstState,
            BlockState secondState
    ) {
        return firstState.getBlock() instanceof ThermochemicalLinkDriveBlock
                && secondState.getBlock() instanceof ThermochemicalLinkDriveBlock;
    }

    public static LinkDriveConnectionStats getLinkDriveConnectionStats(
            Level level,
            BlockPos start
    ) {
        if (!level.isLoaded(start)
                || !(level.getBlockState(start).getBlock()
                instanceof ThermochemicalLinkDriveBlock)) {
            return LinkDriveConnectionStats.NONE;
        }

        Set<BlockPos> segment = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.immutable());

        while (!queue.isEmpty()) {
            BlockPos current = queue.remove();

            if (!segment.add(current)) {
                continue;
            }

            BlockState currentState =
                    level.getBlockState(current);

            for (Direction direction : Direction.values()) {
                BlockPos neighbour =
                        current.relative(direction);

                if (!level.isLoaded(neighbour)
                        || segment.contains(neighbour)) {
                    continue;
                }

                BlockState neighbourState =
                        level.getBlockState(neighbour);

                if (areLinkDrivesKineticallyConnected(
                        currentState,
                        neighbourState,
                        direction
                )) {
                    queue.add(neighbour.immutable());
                }
            }
        }

        int connections = 0;

        for (BlockPos drivePosition : segment) {
            BlockState driveState =
                    level.getBlockState(drivePosition);

            Direction.Axis axis =
                    driveState.getValue(
                            BlockStateProperties.AXIS
                    );

            Direction negative =
                    Direction.get(
                            Direction.AxisDirection.NEGATIVE,
                            axis
                    );

            Direction positive =
                    Direction.get(
                            Direction.AxisDirection.POSITIVE,
                            axis
                    );

            if (hasLinkDriveShaftConnection(
                    level,
                    drivePosition,
                    driveState,
                    negative
            )) {
                connections++;
            }

            if (hasLinkDriveShaftConnection(
                    level,
                    drivePosition,
                    driveState,
                    positive
            )) {
                connections++;
            }
        }

        return new LinkDriveConnectionStats(
                connections,
                segment.size() * 2
        );
    }

    private static boolean areLinkDrivesKineticallyConnected(
            BlockState firstState,
            BlockState secondState,
            Direction direction
    ) {
        if (!(firstState.getBlock()
                instanceof ThermochemicalLinkDriveBlock)
                || !(secondState.getBlock()
                instanceof ThermochemicalLinkDriveBlock)) {
            return false;
        }

        Direction.Axis firstAxis =
                firstState.getValue(
                        BlockStateProperties.AXIS
                );

        Direction.Axis secondAxis =
                secondState.getValue(
                        BlockStateProperties.AXIS
                );

        if (direction.getAxis() == firstAxis
                && direction.getAxis() == secondAxis) {
            return true;
        }

        return ChainDriveBlock.areBlocksConnected(
                firstState,
                secondState,
                direction
        ) || ChainDriveBlock.areBlocksConnected(
                secondState,
                firstState,
                direction.getOpposite()
        );
    }

    private static boolean hasLinkDriveShaftConnection(
            Level level,
            BlockPos drivePosition,
            BlockState driveState,
            Direction direction
    ) {
        BlockPos neighbourPosition = drivePosition.relative(direction);
        if (!level.isLoaded(neighbourPosition)) {
            return false;
        }

        BlockState neighbourState = level.getBlockState(neighbourPosition);
        if (neighbourState.getBlock() instanceof ThermochemicalLinkDriveBlock) {
            return false;
        }
        if (neighbourState.getBlock() instanceof ChainDriveBlock) {
            return false;
        }

        if (!(driveState.getBlock() instanceof IRotate driveRotate)
                || !(neighbourState.getBlock() instanceof IRotate neighbourRotate)) {
            return false;
        }

        return driveRotate.hasShaftTowards(
                level,
                drivePosition,
                driveState,
                direction
        ) && neighbourRotate.hasShaftTowards(
                level,
                neighbourPosition,
                neighbourState,
                direction.getOpposite()
        );
    }

    private static boolean isCombustionBeltPulley(
            Level level,
            BlockPos position
    ) {
        BlockEntity blockEntity = level.getBlockEntity(position);
        return blockEntity instanceof BeltBlockEntity belt
                && isMarkedPulley(belt);
    }

    private static boolean isMarkedPulley(BeltBlockEntity belt) {
        return belt.hasPulley()
                && belt instanceof CombustionBeltAccessor accessor
                && accessor.sulfuricresonance$isCombustionBelt()
                && accessor.sulfuricresonance$isThermochemicalPulley();
    }

    private static @Nullable Direction directionBetween(
            BlockPos first,
            BlockPos second
    ) {
        int x = second.getX() - first.getX();
        int y = second.getY() - first.getY();
        int z = second.getZ() - first.getZ();
        if (Math.abs(x) + Math.abs(y) + Math.abs(z) != 1) {
            return null;
        }
        return Direction.fromDelta(x, y, z);
    }

    public record LinkDriveConnectionStats(
            int connections,
            int capacity
    ) {
        public static final LinkDriveConnectionStats NONE =
                new LinkDriveConnectionStats(0, 0);
    }

    private record PhysicalStep(
            BlockPos position,
            List<BlockPos> pathFromController,
            int distanceFromController
    ) {
    }

    private record DirectSteamSource(
            BlockPos sourcePosition,
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            int temperature
    ) {
    }

    public record InheritedBeltSource(
            BlockPos controllerPosition,
            int distance
    ) {
    }

    public record Result(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            @Nullable BlockPos sourcePos,
            int pathLength,
            int spanLimit,
            int remainingAllowance,
            int temperature,
            int totalDistance
    ) {
        public static final Result NONE = new Result(
                MoltenRotorBlockEntity.RotorHeatLevel.NONE,
                null,
                0,
                0,
                0,
                0,
                Integer.MAX_VALUE
        );
    }
}
