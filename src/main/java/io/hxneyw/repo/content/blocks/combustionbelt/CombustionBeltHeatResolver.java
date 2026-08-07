package io.hxneyw.repo.content.blocks.combustionbelt;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class CombustionBeltHeatResolver {
    private static final int MAX_BELTS_PER_FURNACE = 10;
    private static final int MAX_STRAIGHT_CONNECTORS_BETWEEN_BELTS = 3;
    private static final int MAX_COMPONENT_BELTS = 256;
    private static final long REGISTRY_STALE_TICKS = 40L;
    private static final Map<Level, ControllerRegistry> CONTROLLER_REGISTRIES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CombustionBeltHeatResolver() {
    }

    public static Result resolveChain(
            Level level,
            BeltBlockEntity controller
    ) {
        return resolveAllocatedSource(
                level,
                controller,
                null
        );
    }

    public static Result resolveRelaySource(
            Level level,
            BeltBlockEntity controller,
            BlockPos excludedThermalNode
    ) {
        return resolveAllocatedSource(
                level,
                controller,
                excludedThermalNode
        );
    }

    private static Result resolveAllocatedSource(
            Level level,
            BeltBlockEntity targetController,
            @Nullable BlockPos excludedThermalNode
    ) {
        if (!isValidController(level, targetController)) {
            return Result.NONE;
        }

        BlockPos targetPosition =
                targetController.getBlockPos().immutable();
        ControllerRegistry registry = registerController(
                level,
                targetController
        );
        Component component = discoverRegisteredComponents(
                level,
                registry
        );

        if (!component.controllers().containsKey(targetPosition)) {
            return Result.NONE;
        }

        Map<BlockPos, SourceCandidate> assignments;
        long gameTime = level.getGameTime();

        if (excludedThermalNode == null) {
            if (registry.allocationTick != gameTime) {
                registry.assignments = allocateSources(
                        level,
                        component,
                        null,
                        null
                );
                registry.allocationTick = gameTime;
            }
            assignments = registry.assignments;
        } else {
            assignments = allocateSources(
                    level,
                    component,
                    targetPosition,
                    excludedThermalNode
            );
        }

        SourceCandidate assigned = assignments.get(targetPosition);

        if (assigned == null) {
            return Result.NONE;
        }

        return new Result(
                assigned.heatTier(),
                assigned.furnacePosition(),
                assigned.fromConduit(),
                assigned.distance()
        );
    }

    private static ControllerRegistry registerController(
            Level level,
            BeltBlockEntity controller
    ) {
        ControllerRegistry registry;

        synchronized (CONTROLLER_REGISTRIES) {
            registry = CONTROLLER_REGISTRIES.computeIfAbsent(
                    level,
                    ignored -> new ControllerRegistry()
            );
        }

        long gameTime = level.getGameTime();
        BlockPos position = controller.getBlockPos().immutable();
        RegisteredController previous = registry.controllers.put(
                position,
                new RegisteredController(controller, gameTime)
        );

        if (previous == null
                || previous.controller() != controller) {
            registry.allocationTick = Long.MIN_VALUE;
        }

        boolean removed = registry.controllers.entrySet().removeIf(
                entry -> isStaleController(
                        level,
                        entry.getKey(),
                        entry.getValue(),
                        gameTime
                )
        );

        if (removed) {
            registry.allocationTick = Long.MIN_VALUE;
        }

        return registry;
    }

    private static boolean isStaleController(
            Level level,
            BlockPos position,
            RegisteredController registered,
            long gameTime
    ) {
        if (gameTime - registered.lastSeenTick()
                > REGISTRY_STALE_TICKS) {
            return true;
        }

        if (!level.isLoaded(position)) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(position);
        return blockEntity != registered.controller()
                || !isValidController(
                level,
                registered.controller()
        );
    }

    private static Component discoverRegisteredComponents(
            Level level,
            ControllerRegistry registry
    ) {
        Map<BlockPos, BeltBlockEntity> controllers =
                new LinkedHashMap<>();
        Map<BlockPos, Map<BlockPos, Long>> edges =
                new LinkedHashMap<>();

        List<RegisteredController> registeredControllers =
                List.copyOf(registry.controllers.values());

        for (RegisteredController registered
                : registeredControllers) {
            BeltBlockEntity controller = registered.controller();

            if (!isValidController(level, controller)) {
                continue;
            }

            Component component = discoverComponent(
                    level,
                    controller
            );

            for (Map.Entry<BlockPos, BeltBlockEntity> entry
                    : component.controllers().entrySet()) {
                controllers.putIfAbsent(
                        entry.getKey(),
                        entry.getValue()
                );
                RegisteredController previous =
                        registry.controllers.putIfAbsent(
                                entry.getKey(),
                                new RegisteredController(
                                        entry.getValue(),
                                        level.getGameTime()
                                )
                        );

                if (previous == null) {
                    registry.allocationTick = Long.MIN_VALUE;
                }
            }

            for (Map.Entry<BlockPos, Map<BlockPos, Long>> edgeEntry
                    : component.edges().entrySet()) {
                Map<BlockPos, Long> merged = edges.computeIfAbsent(
                        edgeEntry.getKey(),
                        ignored -> new LinkedHashMap<>()
                );

                for (Map.Entry<BlockPos, Long> neighbour
                        : edgeEntry.getValue().entrySet()) {
                    merged.merge(
                            neighbour.getKey(),
                            neighbour.getValue(),
                            Math::min
                    );
                }
            }
        }

        return new Component(controllers, edges);
    }

    private static Component discoverComponent(
            Level level,
            BeltBlockEntity startController
    ) {
        Map<BlockPos, BeltBlockEntity> controllers =
                new LinkedHashMap<>();
        Map<BlockPos, Map<BlockPos, Long>> edges =
                new LinkedHashMap<>();
        Queue<BlockPos> pending = new ArrayDeque<>();

        BlockPos startPosition =
                startController.getBlockPos().immutable();

        controllers.put(startPosition, startController);
        edges.put(startPosition, new LinkedHashMap<>());
        pending.add(startPosition);

        while (!pending.isEmpty()) {
            if (controllers.size() > MAX_COMPONENT_BELTS) {
                return Component.EMPTY;
            }

            BlockPos currentPosition = pending.remove();
            BeltBlockEntity currentController =
                    controllers.get(currentPosition);

            Map<BlockPos, Long> neighbours =
                    discoverNeighbours(
                            level,
                            currentController
                    );

            for (Map.Entry<BlockPos, Long> entry
                    : neighbours.entrySet()) {
                BlockPos neighbourPosition = entry.getKey();
                long distance = entry.getValue();
                BeltBlockEntity neighbourController =
                        controllerAt(
                                level,
                                neighbourPosition
                        );

                if (neighbourController == null) {
                    continue;
                }

                edges.computeIfAbsent(
                        currentPosition,
                        ignored -> new LinkedHashMap<>()
                ).merge(
                        neighbourPosition,
                        distance,
                        Math::min
                );

                edges.computeIfAbsent(
                        neighbourPosition,
                        ignored -> new LinkedHashMap<>()
                ).merge(
                        currentPosition,
                        distance,
                        Math::min
                );

                if (!controllers.containsKey(neighbourPosition)) {
                    controllers.put(
                            neighbourPosition,
                            neighbourController
                    );
                    pending.add(neighbourPosition);
                }
            }
        }

        return new Component(controllers, edges);
    }

    private static Map<BlockPos, Long> discoverNeighbours(
            Level level,
            BeltBlockEntity controller
    ) {
        Map<BlockPos, Long> neighbours =
                new LinkedHashMap<>();

        for (BlockPos targetPulley : pulleyPositions(
                level,
                controller
        )) {
            for (Direction direction : Direction.values()) {
                addDirectNeighbour(
                        level,
                        controller,
                        targetPulley,
                        direction,
                        neighbours
                );

                addStraightNeighbours(
                        level,
                        controller,
                        targetPulley,
                        direction,
                        neighbours
                );

                addTurnNeighbours(
                        level,
                        controller,
                        targetPulley,
                        direction,
                        neighbours
                );

                addInheritedBeltNeighbour(
                        level,
                        controller,
                        targetPulley,
                        direction,
                        neighbours
                );
            }
        }

        return neighbours;
    }

    private static void addInheritedBeltNeighbour(
            Level level,
            BeltBlockEntity targetController,
            BlockPos targetPulley,
            Direction direction,
            Map<BlockPos, Long> neighbours
    ) {
        BlockPos interfacePosition =
                targetPulley.relative(direction);

        if (!level.isLoaded(interfacePosition)
                || lacksReciprocalShaftConnection(
                level,
                targetPulley,
                interfacePosition,
                direction
        )) {
            return;
        }

        BlockState interfaceState =
                level.getBlockState(interfacePosition);

        if (!ThermochemicalHeatResolver
                .isBeltHeatInterface(interfaceState)) {
            return;
        }

        ThermochemicalHeatResolver.InheritedBeltSource
                inheritedSource =
                ThermochemicalHeatResolver
                        .findInheritedBeltSource(
                                level,
                                interfacePosition
                        );

        if (inheritedSource == null) {
            return;
        }

        BeltBlockEntity sourceController =
                controllerAt(
                        level,
                        inheritedSource.controllerPosition()
                );

        if (sourceController == null
                || sourceController.getBlockPos().equals(
                targetController.getBlockPos()
        )) {
            return;
        }

        neighbours.merge(
                sourceController.getBlockPos().immutable(),
                saturatedAdd(
                        inheritedSource.distance(),
                        1L
                ),
                Math::min
        );
    }

    private static void addDirectNeighbour(
            Level level,
            BeltBlockEntity targetController,
            BlockPos targetPulley,
            Direction direction,
            Map<BlockPos, Long> neighbours
    ) {
        BlockPos sourcePulley =
                targetPulley.relative(direction);

        if (!isMarkedPulley(level, sourcePulley)
                || lacksReciprocalShaftConnection(
                level,
                targetPulley,
                sourcePulley,
                direction
        )) {
            return;
        }

        addNeighbourController(
                level,
                targetController,
                sourcePulley,
                1L,
                neighbours
        );
    }

    private static void addStraightNeighbours(
            Level level,
            BeltBlockEntity targetController,
            BlockPos targetPulley,
            Direction direction,
            Map<BlockPos, Long> neighbours
    ) {
        BlockPos previousPosition = targetPulley;

        for (int connectorCount = 1;
             connectorCount
                     <= MAX_STRAIGHT_CONNECTORS_BETWEEN_BELTS;
             connectorCount++) {
            BlockPos connectorPosition =
                    connectedBridgeConnector(
                            level,
                            previousPosition,
                            direction
                    );

            if (connectorPosition == null) {
                return;
            }

            BlockPos sourcePulley =
                    connectorPosition.relative(direction);

            if (isMarkedPulley(level, sourcePulley)
                    && !lacksReciprocalShaftConnection(
                    level,
                    connectorPosition,
                    sourcePulley,
                    direction
            )) {
                addNeighbourController(
                        level,
                        targetController,
                        sourcePulley,
                        connectorCount + 1L,
                        neighbours
                );
            }

            previousPosition = connectorPosition;
        }
    }

    private static void addTurnNeighbours(
            Level level,
            BeltBlockEntity targetController,
            BlockPos targetPulley,
            Direction directionToConnector,
            Map<BlockPos, Long> neighbours
    ) {
        BlockPos connectorPosition =
                connectedBridgeConnector(
                        level,
                        targetPulley,
                        directionToConnector
                );

        if (connectorPosition == null) {
            return;
        }

        for (Direction directionToSourceBelt
                : Direction.values()) {
            if (directionToSourceBelt
                    == directionToConnector
                    || directionToSourceBelt
                    == directionToConnector.getOpposite()) {
                continue;
            }

            BlockPos sourcePulley =
                    connectorPosition.relative(
                            directionToSourceBelt
                    );

            if (!isMarkedPulley(level, sourcePulley)
                    || lacksReciprocalShaftConnection(
                    level,
                    connectorPosition,
                    sourcePulley,
                    directionToSourceBelt
            )) {
                continue;
            }

            addNeighbourController(
                    level,
                    targetController,
                    sourcePulley,
                    2L,
                    neighbours
            );
        }
    }

    private static @Nullable BlockPos connectedBridgeConnector(
            Level level,
            BlockPos origin,
            Direction direction
    ) {
        BlockPos connectorPosition =
                origin.relative(direction);

        if (!level.isLoaded(connectorPosition)) {
            return null;
        }

        BlockState connectorState =
                level.getBlockState(connectorPosition);

        if (!ThermochemicalHeatResolver.isBridgeConnector(
                connectorState
        )) {
            return null;
        }

        return lacksReciprocalShaftConnection(
                level,
                origin,
                connectorPosition,
                direction
        )
                ? null
                : connectorPosition;
    }

    private static void addNeighbourController(
            Level level,
            BeltBlockEntity targetController,
            BlockPos sourcePulley,
            long distance,
            Map<BlockPos, Long> neighbours
    ) {
        BeltBlockEntity sourceController =
                findControllerForPulley(
                        level,
                        sourcePulley
                );

        if (sourceController == null) {
            return;
        }

        BlockPos sourcePosition =
                sourceController.getBlockPos().immutable();

        if (sourcePosition.equals(
                targetController.getBlockPos()
        )) {
            return;
        }

        neighbours.merge(
                sourcePosition,
                distance,
                Math::min
        );
    }

    private static Map<BlockPos, SourceCandidate> allocateSources(
            Level level,
            Component component,
            @Nullable BlockPos excludedControllerPosition,
            @Nullable BlockPos excludedThermalNode
    ) {
        Map<BlockPos, Map<BlockPos, SourceCandidate>>
                directSources = new LinkedHashMap<>();

        for (Map.Entry<BlockPos, BeltBlockEntity> entry
                : component.controllers().entrySet()) {
            directSources.put(
                    entry.getKey(),
                    resolveDirectSources(
                            level,
                            entry.getValue(),
                            excludedControllerPosition != null
                                    && excludedControllerPosition.equals(
                                    entry.getKey()
                            )
                                    ? excludedThermalNode
                                    : null
                    )
            );
        }

        Map<BlockPos, Map<BlockPos, SourceCandidate>>
                candidatesByBelt = new LinkedHashMap<>();

        for (Map.Entry<BlockPos, Map<BlockPos, SourceCandidate>>
                sourceEntry : directSources.entrySet()) {
            if (sourceEntry.getValue().isEmpty()) {
                continue;
            }

            Map<BlockPos, Long> graphDistances =
                    shortestDistances(
                            sourceEntry.getKey(),
                            component.edges()
                    );

            for (Map.Entry<BlockPos, Long> distanceEntry
                    : graphDistances.entrySet()) {
                BlockPos beltPosition = distanceEntry.getKey();
                long beltDistance = distanceEntry.getValue();

                for (SourceCandidate directSource
                        : sourceEntry.getValue().values()) {
                    SourceCandidate candidate =
                            new SourceCandidate(
                                    directSource.furnacePosition(),
                                    directSource.heatTier(),
                                    directSource.fromConduit()
                                            || beltDistance > 0L,
                                    saturatedAdd(
                                            directSource.distance(),
                                            beltDistance
                                    )
                            );

                    candidatesByBelt
                            .computeIfAbsent(
                                    beltPosition,
                                    ignored -> new LinkedHashMap<>()
                            )
                            .merge(
                                    candidate.furnacePosition(),
                                    candidate,
                                    CombustionBeltHeatResolver
                                            ::chooseBetterSourceCandidate
                            );
                }
            }
        }

        List<AllocationCandidate> allocationCandidates =
                createAllocationCandidates(candidatesByBelt);

        allocationCandidates.sort(
                Comparator
                        .comparingLong(
                                (AllocationCandidate candidate) ->
                                        candidate.source().distance()
                        )
                        .thenComparing(
                                Comparator.comparingInt(
                                        (AllocationCandidate candidate) ->
                                                candidate.source()
                                                        .heatTier().rank
                                ).reversed()
                        )
                        .thenComparingLong(
                                candidate -> candidate.source()
                                        .furnacePosition().asLong()
                        )
                        .thenComparingLong(
                                candidate -> candidate.beltPosition()
                                        .asLong()
                        )
        );

        return assignSources(allocationCandidates);
    }

    private static List<AllocationCandidate>
    createAllocationCandidates(
            Map<BlockPos, Map<BlockPos, SourceCandidate>>
                    candidatesByBelt
    ) {
        List<AllocationCandidate> allocationCandidates =
                new ArrayList<>();

        for (Map.Entry<BlockPos, Map<BlockPos, SourceCandidate>>
                beltEntry : candidatesByBelt.entrySet()) {
            for (SourceCandidate candidate
                    : beltEntry.getValue().values()) {
                allocationCandidates.add(
                        new AllocationCandidate(
                                beltEntry.getKey(),
                                candidate
                        )
                );
            }
        }

        return allocationCandidates;
    }

    private static Map<BlockPos, SourceCandidate> assignSources(
            List<AllocationCandidate> allocationCandidates
    ) {
        Map<BlockPos, Integer> furnaceLoads = new HashMap<>();
        Map<BlockPos, SourceCandidate> assignments =
                new LinkedHashMap<>();

        for (AllocationCandidate allocation
                : allocationCandidates) {
            if (assignments.containsKey(
                    allocation.beltPosition()
            )) {
                continue;
            }

            BlockPos furnacePosition =
                    allocation.source().furnacePosition();
            int currentLoad = furnaceLoads.getOrDefault(
                    furnacePosition,
                    0
            );

            if (currentLoad >= MAX_BELTS_PER_FURNACE) {
                continue;
            }

            assignments.put(
                    allocation.beltPosition(),
                    allocation.source()
            );
            furnaceLoads.put(
                    furnacePosition,
                    currentLoad + 1
            );
        }

        return assignments;
    }

    private static Map<BlockPos, SourceCandidate>
    resolveDirectSources(
            Level level,
            BeltBlockEntity controller,
            @Nullable BlockPos excludedThermalNode
    ) {
        Map<BlockPos, SourceCandidate> sources =
                new LinkedHashMap<>();

        for (BlockPos pulley : pulleyPositions(
                level,
                controller
        )) {
            for (Direction direction : Direction.values()) {
                BlockPos sourcePosition =
                        pulley.relative(direction);

                if (excludedThermalNode != null
                        && excludedThermalNode.equals(
                        sourcePosition
                )) {
                    continue;
                }

                if (!level.isLoaded(sourcePosition)
                        || lacksReciprocalShaftConnection(
                        level,
                        pulley,
                        sourcePosition,
                        direction
                )) {
                    continue;
                }

                BlockEntity blockEntity =
                        level.getBlockEntity(sourcePosition);

                if (blockEntity
                        instanceof MoltenRotorBlockEntity furnace) {
                    if (furnace.getCurrentHeatTier()
                            == MoltenRotorBlockEntity
                            .RotorHeatLevel.NONE) {
                        continue;
                    }

                    SourceCandidate candidate =
                            new SourceCandidate(
                                    sourcePosition.immutable(),
                                    furnace.getCurrentHeatTier(),
                                    false,
                                    1L
                            );

                    sources.merge(
                            candidate.furnacePosition(),
                            candidate,
                            CombustionBeltHeatResolver
                                    ::chooseBetterSourceCandidate
                    );
                    continue;
                }

                BlockState sourceState =
                        level.getBlockState(sourcePosition);

                if (!ThermochemicalHeatResolver
                        .isBeltHeatInterface(sourceState)) {
                    continue;
                }

                ThermochemicalHeatResolver.Result thermalResult =
                        ThermochemicalHeatResolver.resolveNetworkOnly(
                                level,
                                sourcePosition
                        );

                if (thermalResult.heatTier()
                        == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                        || thermalResult.sourcePos() == null) {
                    continue;
                }

                SourceCandidate candidate =
                        new SourceCandidate(
                                thermalResult.sourcePos().immutable(),
                                thermalResult.heatTier(),
                                true,
                                saturatedAdd(
                                        thermalResult.totalDistance(),
                                        1L
                                )
                        );

                sources.merge(
                        candidate.furnacePosition(),
                        candidate,
                        CombustionBeltHeatResolver
                                ::chooseBetterSourceCandidate
                );
            }
        }

        return sources;
    }

    private static Map<BlockPos, Long> shortestDistances(
            BlockPos start,
            Map<BlockPos, Map<BlockPos, Long>> edges
    ) {
        Map<BlockPos, Long> distances = new HashMap<>();
        PriorityQueue<PathStep> pending =
                new PriorityQueue<>(
                        Comparator.comparingLong(PathStep::distance)
                );

        distances.put(start, 0L);
        pending.add(new PathStep(start, 0L));

        while (!pending.isEmpty()) {
            PathStep step = pending.remove();
            long knownDistance = distances.getOrDefault(
                    step.position(),
                    Long.MAX_VALUE
            );

            if (step.distance() != knownDistance) {
                continue;
            }

            for (Map.Entry<BlockPos, Long> neighbour
                    : edges.getOrDefault(
                    step.position(),
                    Map.of()
            ).entrySet()) {
                long nextDistance = saturatedAdd(
                        step.distance(),
                        neighbour.getValue()
                );
                long oldDistance = distances.getOrDefault(
                        neighbour.getKey(),
                        Long.MAX_VALUE
                );

                if (nextDistance >= oldDistance) {
                    continue;
                }

                distances.put(
                        neighbour.getKey(),
                        nextDistance
                );
                pending.add(
                        new PathStep(
                                neighbour.getKey(),
                                nextDistance
                        )
                );
            }
        }

        return distances;
    }

    private static SourceCandidate chooseBetterSourceCandidate(
            SourceCandidate current,
            SourceCandidate candidate
    ) {
        if (candidate.distance() < current.distance()) {
            return candidate;
        }

        if (candidate.distance() > current.distance()) {
            return current;
        }

        if (candidate.heatTier().rank
                > current.heatTier().rank) {
            return candidate;
        }

        if (candidate.heatTier().rank
                < current.heatTier().rank) {
            return current;
        }

        return candidate.furnacePosition().asLong()
                < current.furnacePosition().asLong()
                ? candidate
                : current;
    }

    private static long saturatedAdd(
            long first,
            long second
    ) {
        if (first == Long.MAX_VALUE
                || second > Long.MAX_VALUE - first) {
            return Long.MAX_VALUE;
        }

        return first + second;
    }

    private static @Nullable BeltBlockEntity
    findControllerForPulley(
            Level level,
            BlockPos pulley
    ) {
        if (!isMarkedPulley(level, pulley)) {
            return null;
        }

        BeltBlockEntity controller =
                BeltHelper.getControllerBE(
                        level,
                        pulley
                );

        return isValidController(level, controller)
                ? controller
                : null;
    }

    private static @Nullable BeltBlockEntity controllerAt(
            Level level,
            BlockPos controllerPosition
    ) {
        if (!level.isLoaded(controllerPosition)) {
            return null;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(controllerPosition);

        return blockEntity instanceof BeltBlockEntity controller
                && isValidController(level, controller)
                ? controller
                : null;
    }

    private static Set<BlockPos> pulleyPositions(
            Level level,
            BeltBlockEntity controller
    ) {
        Set<BlockPos> pulleys = new LinkedHashSet<>();

        for (int offset = 0;
             offset < controller.beltLength;
             offset++) {
            BlockPos position =
                    BeltHelper.getPositionForOffset(
                            controller,
                            offset
                    );

            if (isMarkedPulley(level, position)) {
                pulleys.add(position.immutable());
            }
        }

        return pulleys;
    }

    private static boolean isMarkedPulley(
            Level level,
            BlockPos position
    ) {
        if (!level.isLoaded(position)) {
            return false;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(position);

        return blockEntity instanceof BeltBlockEntity belt
                && belt.hasPulley()
                && blockEntity
                instanceof CombustionBeltAccessor accessor
                && accessor.sulfuricresonance$isCombustionBelt()
                && accessor.sulfuricresonance$isThermochemicalPulley();
    }

    private static boolean lacksReciprocalShaftConnection(
            Level level,
            BlockPos firstPosition,
            BlockPos secondPosition,
            Direction direction
    ) {
        BlockState firstState =
                level.getBlockState(firstPosition);
        BlockState secondState =
                level.getBlockState(secondPosition);

        if (firstState.getBlock()
                instanceof IRotate firstRotate
                && secondState.getBlock()
                instanceof IRotate secondRotate
                && firstRotate.hasShaftTowards(
                level,
                firstPosition,
                firstState,
                direction
        )
                && secondRotate.hasShaftTowards(
                level,
                secondPosition,
                secondState,
                direction.getOpposite()
        )) {
            return false;
        }

        BlockEntity firstEntity =
                level.getBlockEntity(firstPosition);
        BlockEntity secondEntity =
                level.getBlockEntity(secondPosition);

        return !(firstEntity
                instanceof KineticBlockEntity firstKinetic
                && secondEntity
                instanceof KineticBlockEntity secondKinetic
                && (RotationPropagator.isConnected(
                firstKinetic,
                secondKinetic
        ) || RotationPropagator.isConnected(
                secondKinetic,
                firstKinetic
        )));
    }

    private static boolean isValidController(
            Level level,
            @Nullable BeltBlockEntity controller
    ) {
        return !level.isClientSide()
                && controller != null
                && controller.isController()
                && controller.beltLength > 0;
    }


    private static final class ControllerRegistry {
        private final Map<BlockPos, RegisteredController> controllers =
                new LinkedHashMap<>();
        private long allocationTick = Long.MIN_VALUE;
        private Map<BlockPos, SourceCandidate> assignments = Map.of();
    }

    private record RegisteredController(
            BeltBlockEntity controller,
            long lastSeenTick
    ) {
    }

    private record Component(
            Map<BlockPos, BeltBlockEntity> controllers,
            Map<BlockPos, Map<BlockPos, Long>> edges
    ) {
        private static final Component EMPTY = new Component(
                Map.of(),
                Map.of()
        );
    }

    private record SourceCandidate(
            BlockPos furnacePosition,
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            boolean fromConduit,
            long distance
    ) {
    }

    private record AllocationCandidate(
            BlockPos beltPosition,
            SourceCandidate source
    ) {
    }

    private record PathStep(
            BlockPos position,
            long distance
    ) {
    }

    public record Result(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            @Nullable BlockPos sourcePosition,
            boolean fromConduit,
            long distanceSquared
    ) {
        public static final Result NONE = new Result(
                MoltenRotorBlockEntity.RotorHeatLevel.NONE,
                null,
                false,
                Long.MAX_VALUE
        );
    }
}