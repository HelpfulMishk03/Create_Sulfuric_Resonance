package io.hxneyw.repo.content.blocks.thermochemicalboilerinterface;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ThermochemicalBoilerInterfaceArray {

    private static final int MAX_MEMBERS = 9;
    private static final int MAX_SPAN = 3;
    private static final int MAX_CREATE_HEAT = 18;
    private static final int MAX_TEMPERATURE = 1599;
    private static final int[] OUTPUT_NUMERATOR = {
            0, 32, 48, 60, 72, 80, 88, 94, 100, 104
    };
    private static final int[] STRESS_SU = {
            0, 256, 384, 512, 768, 1024, 1280, 1536, 1792, 2048
    };

    private ThermochemicalBoilerInterfaceArray() {
    }

    public static Snapshot resolve(Level level, BlockPos start) {
        List<BlockPos> members = collectMembers(level, start);
        if (members.isEmpty()) {
            return Snapshot.NONE;
        }

        Bounds bounds = bounds(members);
        boolean valid = members.size() <= MAX_MEMBERS
                && bounds.width() <= MAX_SPAN
                && bounds.depth() <= MAX_SPAN;
        if (!valid) {
            return new Snapshot(
                    members,
                    false,
                    bounds,
                    MoltenRotorBlockEntity.RotorHeatLevel.NONE,
                    null,
                    null,
                    0,
                    0,
                    0,
                    0
            );
        }

        InputCandidate selected = findSelectedInput(level, members, bounds);
        MoltenRotorBlockEntity.RotorHeatLevel heatTier = selected == null
                ? MoltenRotorBlockEntity.RotorHeatLevel.NONE
                : selected.heatTier();
        int temperature = selected == null ? 0 : selected.temperature();
        int size = members.size();
        int grossSu = heatTier == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                ? 0
                : Math.round(heatTier.baseStressCapacity * OUTPUT_NUMERATOR[size] / 32.0F);
        int stressSu = heatTier == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                ? 0
                : STRESS_SU[size];
        int heatScore = heatTier == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                ? 0
                : heatLevelForTemperature(temperature);

        return new Snapshot(
                members,
                true,
                bounds,
                heatTier,
                selected == null ? null : selected.position(),
                selected == null ? null : selected.side(),
                temperature,
                grossSu,
                stressSu,
                heatScore
        );
    }

    public static boolean isPortEligible(
            LevelReader level,
            BlockPos position,
            Direction side
    ) {
        List<BlockPos> members = collectMembers(level, position);
        if (members.isEmpty()) {
            return false;
        }
        Bounds bounds = bounds(members);
        if (members.size() > MAX_MEMBERS
                || bounds.width() > MAX_SPAN
                || bounds.depth() > MAX_SPAN) {
            return false;
        }
        return isPortEligible(level, members, bounds, position, side);
    }

    private static boolean isPortEligible(
            LevelReader level,
            List<BlockPos> members,
            Bounds bounds,
            BlockPos position,
            Direction side
    ) {
        if (side.getAxis() == Direction.Axis.Y
                || ThermochemicalBoilerInterfaceBlock.isInterface(
                level,
                position.relative(side)
        )) {
            return false;
        }

        if (isFullThreeByThree(members, bounds)) {
            int centerX = bounds.minX() + 1;
            int centerZ = bounds.minZ() + 1;
            return switch (side) {
                case NORTH -> position.getZ() == bounds.minZ()
                        && position.getX() == centerX;
                case SOUTH -> position.getZ() == bounds.maxZ()
                        && position.getX() == centerX;
                case WEST -> position.getX() == bounds.minX()
                        && position.getZ() == centerZ;
                case EAST -> position.getX() == bounds.maxX()
                        && position.getZ() == centerZ;
                default -> false;
            };
        }

        return true;
    }

    public static boolean usesAutomaticPorts(
            LevelReader level,
            BlockPos position
    ) {
        List<BlockPos> members = collectMembers(level, position);
        return !members.isEmpty()
                && isFullThreeByThree(members, bounds(members));
    }

    private static @Nullable Direction automaticPortSide(
            LevelReader level,
            List<BlockPos> members,
            Bounds bounds,
            BlockPos position
    ) {
        if (!isFullThreeByThree(members, bounds)) {
            return null;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (isPortEligible(level, members, bounds, position, side)) {
                return side;
            }
        }
        return null;
    }
    public static void synchronizeState(Level level, Snapshot snapshot) {
        if (level.isClientSide || snapshot.members().isEmpty()) {
            return;
        }

        boolean fullThreeByThree = isFullThreeByThree(
                snapshot.members(),
                snapshot.bounds()
        );

        for (BlockPos member : snapshot.members()) {
            BlockState state = level.getBlockState(member);
            if (!(state.getBlock() instanceof ThermochemicalBoilerInterfaceBlock)) {
                continue;
            }

            BlockState updated = state;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                boolean eligible = isPortEligible(
                        level,
                        snapshot.members(),
                        snapshot.bounds(),
                        member,
                        side
                );
                if (fullThreeByThree) {
                    updated = ThermochemicalBoilerInterfaceBlock.setPort(
                            updated,
                            side,
                            eligible
                    );
                } else if (ThermochemicalBoilerInterfaceBlock.hasPort(updated, side)
                        && !eligible) {
                    updated = ThermochemicalBoilerInterfaceBlock.setPort(
                            updated,
                            side,
                            false
                    );
                }
            }

            if (fullThreeByThree) {
                Direction automaticFacing = automaticPortSide(
                        level,
                        snapshot.members(),
                        snapshot.bounds(),
                        member
                );
                if (automaticFacing != null) {
                    updated = updated.setValue(
                            ThermochemicalBoilerInterfaceBlock.FACING,
                            automaticFacing
                    );
                }
            } else {
                updated = normalizeManualPorts(updated, snapshot, member);
            }

            boolean shouldBeInput = snapshot.inputPosition() != null
                    && snapshot.inputPosition().equals(member)
                    && snapshot.inputSide() != null;
            updated = updated.setValue(
                    ThermochemicalBoilerInterfaceBlock.INPUT_ACTIVE,
                    shouldBeInput
            );
            if (shouldBeInput) {
                updated = updated.setValue(
                        ThermochemicalBoilerInterfaceBlock.FACING,
                        snapshot.inputSide()
                );
            }

            if (updated != state) {
                level.setBlock(member, updated, Block.UPDATE_ALL);
            }
        }
    }

    public static void requestRefresh(Level level, BlockPos position) {
        if (level.isClientSide || !level.isLoaded(position)) {
            return;
        }
        List<BlockPos> members = collectMembers(level, position);
        for (BlockPos member : members) {
            BlockEntity blockEntity = level.getBlockEntity(member);
            if (blockEntity instanceof ThermochemicalBoilerInterfaceBlockEntity interfaceEntity) {
                interfaceEntity.requestImmediateRefresh();
            }
        }
    }

    public static int getMemberHeatContribution(
            Level level,
            BlockPos position,
            Snapshot snapshot
    ) {
        if (!snapshot.valid()
                || snapshot.heatScore() <= 0
                || snapshot.heatTier() == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                || findBoilerController(level, snapshot) == null
                || !snapshot.members().contains(position)) {
            return 0;
        }

        int index = snapshot.members().indexOf(position);
        if (index < 0) {
            return 0;
        }

        int base = snapshot.heatScore() / snapshot.size();
        int remainder = snapshot.heatScore() % snapshot.size();
        return base + (index < remainder ? 1 : 0);
    }

    public static boolean hasBoilerTarget(Level level, Snapshot snapshot) {
        return findBoilerController(level, snapshot) != null;
    }

    public static @Nullable FluidTankBlockEntity findBoilerController(
            Level level,
            Snapshot snapshot
    ) {
        if (!snapshot.valid() || snapshot.members().isEmpty()) {
            return null;
        }

        FluidTankBlockEntity commonController = null;
        for (BlockPos member : snapshot.members()) {
            BlockEntity above = level.getBlockEntity(member.above());
            if (!(above instanceof FluidTankBlockEntity tank)) {
                return null;
            }
            FluidTankBlockEntity controller = tank.getControllerBE();
            if (controller == null) {
                return null;
            }
            if (commonController == null) {
                commonController = controller;
            } else if (!commonController.getBlockPos().equals(controller.getBlockPos())) {
                return null;
            }
        }
        return commonController;
    }

    private static @Nullable InputCandidate findSelectedInput(
            Level level,
            List<BlockPos> members,
            Bounds bounds
    ) {
        List<InputCandidate> candidates = new ArrayList<>();
        boolean fullThreeByThree = isFullThreeByThree(members, bounds);

        for (BlockPos member : members) {
            BlockState state = level.getBlockState(member);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                boolean eligible = isPortEligible(
                        level,
                        members,
                        bounds,
                        member,
                        side
                );
                boolean portOpen = fullThreeByThree
                        ? eligible
                        : ThermochemicalBoilerInterfaceBlock.hasPort(state, side);
                if (!portOpen
                        || !eligible
                        || !ThermochemicalBoilerInterfaceBlock.hasValidInputNeighbour(
                        level,
                        member,
                        side
                )) {
                    continue;
                }

                BlockPos neighbour = member.relative(side);
                ThermochemicalHeatResolver.Result result =
                        ThermochemicalHeatResolver.resolveNetworkOnly(level, neighbour);
                if (result.heatTier() == MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
                    continue;
                }

                InputCandidate candidate = new InputCandidate(
                        member,
                        side,
                        result.heatTier(),
                        result.temperature()
                );
                candidates.add(candidate);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }
        candidates.sort(InputCandidateComparator.INSTANCE);
        return candidates.getFirst();
    }

    private static BlockState normalizeManualPorts(
            BlockState state,
            Snapshot snapshot,
            BlockPos member
    ) {
        Direction keep = null;
        if (snapshot.inputPosition() != null
                && snapshot.inputPosition().equals(member)
                && snapshot.inputSide() != null
                && ThermochemicalBoilerInterfaceBlock.hasPort(state, snapshot.inputSide())) {
            keep = snapshot.inputSide();
        }
        if (keep == null) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (ThermochemicalBoilerInterfaceBlock.hasPort(state, side)) {
                    keep = side;
                    break;
                }
            }
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side != keep && ThermochemicalBoilerInterfaceBlock.hasPort(state, side)) {
                state = ThermochemicalBoilerInterfaceBlock.setPort(state, side, false);
            }
        }
        return state;
    }

    private static boolean isFullThreeByThree(
            List<BlockPos> members,
            Bounds bounds
    ) {
        return members.size() == 9
                && bounds.width() == 3
                && bounds.depth() == 3;
    }

    private static int heatLevelForTemperature(int temperature) {
        if (temperature <= 0) {
            return 0;
        }
        int heat = Math.round(
                temperature * MAX_CREATE_HEAT / (float) MAX_TEMPERATURE
        );
        return Math.max(1, Math.min(MAX_CREATE_HEAT, heat));
    }

    private static List<BlockPos> collectMembers(LevelReader level, BlockPos start) {
        if (!ThermochemicalBoilerInterfaceBlock.isInterface(level, start)) {
            return List.of();
        }

        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.immutable());

        while (!queue.isEmpty() && visited.size() <= MAX_MEMBERS + 8) {
            BlockPos current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (!visited.contains(next)
                        && ThermochemicalBoilerInterfaceBlock.isInterface(level, next)) {
                    queue.addLast(next.immutable());
                }
            }
        }

        List<BlockPos> members = new ArrayList<>(visited);
        members.sort(BlockPosComparator.INSTANCE);
        return List.copyOf(members);
    }

    private static Bounds bounds(List<BlockPos> members) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos member : members) {
            minX = Math.min(minX, member.getX());
            maxX = Math.max(maxX, member.getX());
            minZ = Math.min(minZ, member.getZ());
            maxZ = Math.max(maxZ, member.getZ());
        }
        return new Bounds(minX, maxX, minZ, maxZ);
    }

    private record InputCandidate(
            BlockPos position,
            Direction side,
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            int temperature
    ) {
    }

    private enum InputCandidateComparator implements Comparator<InputCandidate> {
        INSTANCE;

        @Override
        public int compare(InputCandidate first, InputCandidate second) {
            int tier = Integer.compare(second.heatTier().rank, first.heatTier().rank);
            if (tier != 0) {
                return tier;
            }
            int temperature = Integer.compare(second.temperature(), first.temperature());
            if (temperature != 0) {
                return temperature;
            }
            int position = BlockPosComparator.INSTANCE.compare(
                    first.position(),
                    second.position()
            );
            if (position != 0) {
                return position;
            }
            return Integer.compare(first.side().get2DDataValue(), second.side().get2DDataValue());
        }
    }

    private enum BlockPosComparator implements Comparator<BlockPos> {
        INSTANCE;

        @Override
        public int compare(BlockPos first, BlockPos second) {
            int y = Integer.compare(first.getY(), second.getY());
            if (y != 0) {
                return y;
            }
            int x = Integer.compare(first.getX(), second.getX());
            if (x != 0) {
                return x;
            }
            return Integer.compare(first.getZ(), second.getZ());
        }
    }

    public record Bounds(
            int minX,
            int maxX,
            int minZ,
            int maxZ
    ) {
        public int width() {
            return maxX - minX + 1;
        }

        public int depth() {
            return maxZ - minZ + 1;
        }
    }

    public record Snapshot(
            List<BlockPos> members,
            boolean valid,
            Bounds bounds,
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            @Nullable BlockPos inputPosition,
            @Nullable Direction inputSide,
            int temperature,
            int targetGrossSu,
            int stressSu,
            int heatScore
    ) {
        public static final Snapshot NONE = new Snapshot(
                List.of(),
                false,
                new Bounds(0, 0, 0, 0),
                MoltenRotorBlockEntity.RotorHeatLevel.NONE,
                null,
                null,
                0,
                0,
                0,
                0
        );

        public int size() {
            return members.size();
        }

        public boolean isInput(BlockPos position) {
            return inputPosition != null && inputPosition.equals(position);
        }
    }
}
