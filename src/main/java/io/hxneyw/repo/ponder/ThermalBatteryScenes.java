package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.thermalbattery.ThermalBatteryBlock;
import io.hxneyw.repo.content.blocks.thermalbattery.ThermalBatteryBlockEntity;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class ThermalBatteryScenes {

    private ThermalBatteryScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "thermal_battery.operation",
                "Storing Thermochemical Heat"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.45F);

        BlockPos sourceShaftPos = util.grid().at(2, 1, 1);
        BlockPos interfaceShaftPos = util.grid().at(2, 1, 2);
        BlockPos batteryPos = util.grid().at(2, 1, 3);

        Selection sourceShaft =
                util.select().position(sourceShaftPos);
        Selection interfaceShaft =
                util.select().position(interfaceShaftPos);
        Selection shafts = sourceShaft.add(interfaceShaft);
        Selection battery =
                util.select().position(batteryPos);
        Selection connected = shafts.add(battery);

        BlockState shaftState =
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(
                                RotatedPillarBlock.AXIS,
                                Direction.Axis.Z
                        );

        BlockState heatedBattery =
                AllModBlocks.THERMAL_BATTERY.get()
                        .defaultBlockState()
                        .setValue(
                                ThermalBatteryBlock.FACING,
                                Direction.NORTH
                        )
                        .setValue(
                                ThermalBatteryBlock.INDICATOR,
                                ThermalBatteryBlock.IndicatorState.HEATED
                        );

        BlockState superheatedBattery =
                heatedBattery.setValue(
                        ThermalBatteryBlock.INDICATOR,
                        ThermalBatteryBlock.IndicatorState.SUPERHEATED
                );

        BlockState faultBattery =
                heatedBattery.setValue(
                        ThermalBatteryBlock.INDICATOR,
                        ThermalBatteryBlock.IndicatorState.FAULT
                );

        scene.world().setBlock(
                sourceShaftPos,
                shaftState,
                false
        );
        scene.world().setBlock(
                interfaceShaftPos,
                shaftState,
                false
        );
        scene.world().setBlock(
                batteryPos,
                heatedBattery,
                false
        );

        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(
                battery,
                Direction.DOWN
        );
        scene.idle(20);

        scene.overlay()
                .showText(110)
                .text(
                        "The Thermal Battery stores thermochemical heat for later use instead of generating heat on its own"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(batteryPos))
                .placeNearTarget();

        scene.idle(120);

        scene.world().showSection(
                shafts,
                Direction.SOUTH
        );
        scene.world().setKineticSpeed(
                connected,
                64.0F
        );
        scene.idle(25);

        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "thermal_battery_interface",
                interfaceShaft.add(battery),
                125
        );

        scene.overlay()
                .showText(115)
                .text(
                        "Its single interface connects to a Thermochemical Shaft network and carries rotation and heat through that one face"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(
                        util.vector()
                                .centerOf(interfaceShaftPos)
                                .add(0.0, 0.0, 0.45)
                )
                .placeNearTarget();

        scene.idle(125);

        scene.world().modifyBlockEntityNBT(
                battery,
                ThermalBatteryBlockEntity.class,
                nbt -> {
                    nbt.putFloat("HeatedHeat", 3600.0F);
                    nbt.putFloat("SuperheatedHeat", 2800.0F);
                    nbt.putString("OutputMode", "heated");
                }
        );

        scene.effects().indicateSuccess(batteryPos);

        scene.overlay()
                .showText(130)
                .text(
                        "While live heat is available, the Battery charges automatically and can hold about five fully heated Molten Rotor Furnaces worth of heat"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(batteryPos))
                .placeNearTarget();

        scene.idle(140);

        scene.overlay()
                .showText(120)
                .text(
                        "Heated and Superheated reserves are stored separately so higher-grade heat is preserved instead of being flattened into one pool"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(batteryPos))
                .placeNearTarget();

        scene.idle(130);

        scene.overlay()
                .showControls(
                        util.vector()
                                .topOf(batteryPos)
                                .add(0.0, 0.25, 0.0),
                        Pointing.DOWN,
                        60
                )
                .rightClick();

        scene.overlay()
                .showText(115)
                .text(
                        "Empty-hand right-click opens the control panel, where Heated or Superheated output can be selected"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(batteryPos))
                .placeNearTarget();

        scene.idle(125);

        scene.world().modifyBlockEntityNBT(
                battery,
                ThermalBatteryBlockEntity.class,
                nbt -> nbt.putString(
                        "OutputMode",
                        "superheated"
                )
        );
        scene.world().modifyBlock(
                batteryPos,
                state -> superheatedBattery,
                false
        );
        scene.effects().indicateSuccess(batteryPos);

        scene.overlay()
                .showText(130)
                .text(
                        "Heated lasts longer; Superheated drains its dedicated reserve faster. The Battery never outputs Radiant or Afterburn heat"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(batteryPos))
                .placeNearTarget();

        scene.idle(140);

        scene.world().hideSection(
                shafts,
                Direction.NORTH
        );
        scene.world().setKineticSpeed(
                connected,
                0.0F
        );
        scene.world().modifyBlock(
                batteryPos,
                state -> faultBattery,
                false
        );
        scene.idle(20);

        scene.overlay().showOutline(
                PonderPalette.RED,
                "thermal_battery_fault",
                battery,
                130
        );

        scene.idle(35);
        scene.markAsFinished();
    }
}