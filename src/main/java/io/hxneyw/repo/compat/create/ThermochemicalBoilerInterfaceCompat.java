package io.hxneyw.repo.compat.create;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalboilerinterface.ThermochemicalBoilerInterfaceArray;
import io.hxneyw.repo.content.blocks.thermochemicalboilerinterface.ThermochemicalBoilerInterfaceBlockEntity;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ThermochemicalBoilerInterfaceCompat {

    private static final ThreadLocal<Set<FluidTankBlockEntity>> RESOLVING_STEAM_SOURCES =
            ThreadLocal.withInitial(HashSet::new);

    private ThermochemicalBoilerInterfaceCompat() {
    }

    public static void register() {
        BoilerHeater.REGISTRY.register(
                AllModBlocks.THERMOCHEMICAL_BOILER_INTERFACE.get(),
                ThermochemicalBoilerInterfaceCompat::getHeat
        );
    }

    public static float getHeat(
            Level level,
            BlockPos position,
            BlockState state
    ) {
        BlockEntity blockEntity = level.getBlockEntity(position);
        if (!(blockEntity instanceof ThermochemicalBoilerInterfaceBlockEntity interfaceEntity)) {
            return BoilerHeater.NO_HEAT;
        }
        int heat = interfaceEntity.getBoilerHeatContribution();
        return heat > 0 ? heat : BoilerHeater.NO_HEAT;
    }


    public static MoltenRotorBlockEntity.RotorHeatLevel getBoilerHeatTier(
            FluidTankBlockEntity controller
    ) {
        if (controller.getLevel() == null) {
            return MoltenRotorBlockEntity.RotorHeatLevel.NONE;
        }

        Level level = controller.getLevel();
        BlockPos controllerPos = controller.getBlockPos();
        MoltenRotorBlockEntity.RotorHeatLevel highest =
                MoltenRotorBlockEntity.RotorHeatLevel.NONE;

        for (int x = 0; x < FluidTankBlockEntity.getMaxSize(); x++) {
            for (int z = 0; z < FluidTankBlockEntity.getMaxSize(); z++) {
                BlockPos tankPos = controllerPos.offset(x, 0, z);
                BlockEntity tankEntity = level.getBlockEntity(tankPos);
                if (!(tankEntity instanceof FluidTankBlockEntity tank)
                        || tank.getControllerBE() != controller) {
                    continue;
                }

                BlockEntity heaterEntity = level.getBlockEntity(tankPos.below());
                if (!(heaterEntity instanceof ThermochemicalBoilerInterfaceBlockEntity interfaceEntity)
                        || !interfaceEntity.hasDisplayedBoiler()) {
                    continue;
                }

                MoltenRotorBlockEntity.RotorHeatLevel heatTier =
                        interfaceEntity.getDisplayedHeatTier();
                if (heatTier.rank > highest.rank) {
                    highest = heatTier;
                }
            }
        }

        return highest;
    }

    public static int getBoilerTemperature(FluidTankBlockEntity controller) {
        if (controller.getLevel() == null) {
            return 0;
        }

        Level level = controller.getLevel();
        BlockPos controllerPos = controller.getBlockPos();
        int highest = 0;

        for (int x = 0; x < FluidTankBlockEntity.getMaxSize(); x++) {
            for (int z = 0; z < FluidTankBlockEntity.getMaxSize(); z++) {
                BlockPos tankPos = controllerPos.offset(x, 0, z);
                BlockEntity tankEntity = level.getBlockEntity(tankPos);
                if (!(tankEntity instanceof FluidTankBlockEntity tank)
                        || tank.getControllerBE() != controller) {
                    continue;
                }

                BlockEntity heaterEntity = level.getBlockEntity(tankPos.below());
                if (!(heaterEntity instanceof ThermochemicalBoilerInterfaceBlockEntity interfaceEntity)
                        || !interfaceEntity.hasDisplayedBoiler()) {
                    continue;
                }

                highest = Math.max(highest, interfaceEntity.getDisplayedTemperature());
            }
        }

        return highest;
    }

    public static int getBoilerTargetGrossSu(FluidTankBlockEntity controller) {
        if (controller.getLevel() == null) {
            return 0;
        }

        Level level = controller.getLevel();
        BlockPos controllerPos = controller.getBlockPos();
        int highestTarget = 0;
        Set<BlockPos> visitedArrays = new HashSet<>();

        for (int x = 0; x < FluidTankBlockEntity.getMaxSize(); x++) {
            for (int z = 0; z < FluidTankBlockEntity.getMaxSize(); z++) {
                BlockPos tankPos = controllerPos.offset(x, 0, z);
                BlockEntity tankEntity = level.getBlockEntity(tankPos);
                if (!(tankEntity instanceof FluidTankBlockEntity tank)
                        || tank.getControllerBE() != controller) {
                    continue;
                }

                BlockPos heaterPos = tankPos.below();
                BlockEntity heaterEntity = level.getBlockEntity(heaterPos);
                if (!(heaterEntity instanceof ThermochemicalBoilerInterfaceBlockEntity interfaceEntity)) {
                    continue;
                }

                ThermochemicalBoilerInterfaceArray.Snapshot snapshot = interfaceEntity.getSnapshot();
                if (!snapshot.valid()
                        || snapshot.members().isEmpty()
                        || snapshot.heatTier().rank <= 0
                        || !ThermochemicalBoilerInterfaceArray.hasBoilerTarget(level, snapshot)) {
                    continue;
                }

                BlockPos leader = snapshot.members().getFirst();
                if (!visitedArrays.add(leader)) {
                    continue;
                }
                highestTarget = Math.max(highestTarget, snapshot.targetGrossSu());
            }
        }

        return highestTarget;
    }

    public static int getBoilerBonusSu(
            FluidTankBlockEntity controller
    ) {
        return controller.boiler instanceof ThermochemicalBoilerBonusAccess access
                ? access.sulfuricresonance$getThermochemicalBonusSu()
                : 0;
    }

    public static SteamSource resolveSteamSource(
            SteamEngineBlockEntity engine
    ) {
        PoweredShaftBlockEntity shaft = engine.getShaft();
        if (shaft == null) {
            return SteamSource.NONE;
        }
        return resolveSteamSource(engine, shaft);
    }

    public static SteamSource resolveSteamSource(
            PoweredShaftBlockEntity shaft
    ) {
        Level level = shaft.getLevel();
        if (level == null || shaft.enginePos == null) {
            return SteamSource.NONE;
        }

        BlockPos enginePosition = shaft.getBlockPos().subtract(
                shaft.enginePos
        );
        BlockEntity blockEntity = level.getBlockEntity(enginePosition);
        if (!(blockEntity instanceof SteamEngineBlockEntity engine)
                || engine.getShaft() != shaft) {
            return SteamSource.NONE;
        }

        return resolveSteamSource(engine, shaft);
    }

    private static SteamSource resolveSteamSource(
            SteamEngineBlockEntity engine,
            PoweredShaftBlockEntity shaft
    ) {
        FluidTankBlockEntity controller = engine.getTank();
        if (controller == null) {
            return SteamSource.NONE;
        }

        Set<FluidTankBlockEntity> resolving = RESOLVING_STEAM_SOURCES.get();
        if (!resolving.add(controller)) {
            return SteamSource.NONE;
        }

        try {
            MoltenRotorBlockEntity.RotorHeatLevel heatTier = getBoilerHeatTier(controller);
            int temperature = getBoilerTemperature(controller);
            if (heatTier == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                    || temperature <= 0) {
                return SteamSource.NONE;
            }

            int rpm = Math.round(Math.abs(shaft.getSpeed()));
            int su = Math.round(Math.abs(
                    shaft.calculateAddedStressCapacity() * shaft.getGeneratedSpeed()
            ));
            return new SteamSource(
                    heatTier,
                    temperature,
                    rpm,
                    su
            );
        } finally {
            resolving.remove(controller);
            if (resolving.isEmpty()) {
                RESOLVING_STEAM_SOURCES.remove();
            }
        }
    }

    public record SteamSource(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier,
            int temperature,
            int rpm,
            int su
    ) {
        public static final SteamSource NONE = new SteamSource(
                MoltenRotorBlockEntity.RotorHeatLevel.NONE,
                0,
                0,
                0
        );

        public boolean active() {
            return heatTier != MoltenRotorBlockEntity.RotorHeatLevel.NONE
                    && temperature > 0;
        }
    }

}
