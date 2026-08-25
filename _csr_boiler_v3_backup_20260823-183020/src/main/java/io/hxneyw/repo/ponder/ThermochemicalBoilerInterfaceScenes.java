package io.hxneyw.repo.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.thermochemicalboilerinterface.ThermochemicalBoilerInterfaceBlock;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ThermochemicalBoilerInterfaceScenes {

    private ThermochemicalBoilerInterfaceScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "thermochemical_boiler_interface.operation",
                "Thermochemical Boiler Interface"
        );
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.82F);
        scene.setSceneOffsetY(-0.4F);
        scene.showBasePlate();

        BlockPos small = util.grid().at(1, 1, 1);
        scene.world().setBlock(
                small,
                AllModBlocks.THERMOCHEMICAL_BOILER_INTERFACE.get().defaultBlockState(),
                false
        );
        scene.world().showSection(util.select().position(small), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(70)
                .text("For 1x1 and 2x2 arrays, choose one exposed shaft face with a Wrench")
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(small))
                .placeNearTarget();
        scene.overlay().showControls(
                        util.vector().centerOf(small).add(-0.7, 0, 0),
                        Pointing.DOWN,
                        45
                )
                .rightClick()
                .withItem(new ItemStack(AllItems.WRENCH.get()));
        scene.idle(80);

        scene.world().modifyBlock(
                small,
                state -> state
                        .setValue(ThermochemicalBoilerInterfaceBlock.PORT_WEST, true)
                        .setValue(ThermochemicalBoilerInterfaceBlock.FACING, Direction.WEST),
                false
        );
        scene.overlay().showText(65)
                .text("Selecting another face closes the previous one")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(small))
                .placeNearTarget();
        scene.idle(75);
        scene.world().hideSection(util.select().position(small), Direction.UP);
        scene.idle(10);

        Selection array = util.select().fromTo(2, 1, 2, 4, 1, 4);
        Selection tanks = util.select().fromTo(2, 2, 2, 4, 2, 4);
        BlockPos westCenter = util.grid().at(2, 1, 3);
        BlockPos northCenter = util.grid().at(3, 1, 2);
        BlockPos eastCenter = util.grid().at(4, 1, 3);
        BlockPos southCenter = util.grid().at(3, 1, 4);

        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                BlockPos pos = util.grid().at(x, 1, z);
                BlockState state = AllModBlocks.THERMOCHEMICAL_BOILER_INTERFACE.get()
                        .defaultBlockState()
                        .setValue(ThermochemicalBoilerInterfaceBlock.NORTH, z > 2)
                        .setValue(ThermochemicalBoilerInterfaceBlock.EAST, x < 4)
                        .setValue(ThermochemicalBoilerInterfaceBlock.SOUTH, z < 4)
                        .setValue(ThermochemicalBoilerInterfaceBlock.WEST, x > 2);
                if (pos.equals(westCenter)) {
                    state = state.setValue(ThermochemicalBoilerInterfaceBlock.PORT_WEST, true)
                            .setValue(ThermochemicalBoilerInterfaceBlock.FACING, Direction.WEST);
                } else if (pos.equals(northCenter)) {
                    state = state.setValue(ThermochemicalBoilerInterfaceBlock.PORT_NORTH, true)
                            .setValue(ThermochemicalBoilerInterfaceBlock.FACING, Direction.NORTH);
                } else if (pos.equals(eastCenter)) {
                    state = state.setValue(ThermochemicalBoilerInterfaceBlock.PORT_EAST, true)
                            .setValue(ThermochemicalBoilerInterfaceBlock.FACING, Direction.EAST);
                } else if (pos.equals(southCenter)) {
                    state = state.setValue(ThermochemicalBoilerInterfaceBlock.PORT_SOUTH, true)
                            .setValue(ThermochemicalBoilerInterfaceBlock.FACING, Direction.SOUTH);
                }
                scene.world().setBlock(pos, state, false);
                scene.world().setBlock(
                        util.grid().at(x, 2, z),
                        AllBlocks.FLUID_TANK.getDefaultState(),
                        false
                );
            }
        }

        scene.world().showSection(array, Direction.DOWN);
        scene.world().showSection(tanks, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(75)
                .text("A full 3x3 exposes only the four edge-center shaft faces")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(westCenter))
                .placeNearTarget();
        scene.idle(85);

        BlockPos rotor = util.grid().at(0, 1, 3);
        BlockPos inputShaft = util.grid().at(1, 1, 3);
        scene.world().setBlock(
                rotor,
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get().defaultBlockState(),
                false
        );
        scene.world().setBlock(
                inputShaft,
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(BlockStateProperties.AXIS, Direction.Axis.X),
                false
        );
        scene.world().modifyBlock(
                westCenter,
                state -> state.setValue(ThermochemicalBoilerInterfaceBlock.INPUT_ACTIVE, true),
                false
        );
        scene.world().showSection(
                util.select().position(rotor).add(util.select().position(inputShaft)),
                Direction.EAST
        );
        scene.world().setKineticSpeed(util.select().position(inputShaft), 64.0F);
        scene.effects().indicateSuccess(westCenter);
        scene.overlay().showText(80)
                .text("Only one port supplies the array. Its heat tier and temperature heat the boiler")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(inputShaft))
                .placeNearTarget();
        scene.idle(90);

        BlockPos engine = util.grid().at(5, 2, 3);
        BlockPos poweredShaft = util.grid().at(6, 2, 3);
        scene.world().setBlock(engine, AllBlocks.STEAM_ENGINE.getDefaultState(), false);
        scene.world().setBlock(
                poweredShaft,
                AllBlocks.POWERED_SHAFT.getDefaultState()
                        .setValue(BlockStateProperties.AXIS, Direction.Axis.X),
                false
        );
        scene.world().showSection(
                util.select().position(engine).add(util.select().position(poweredShaft)),
                Direction.WEST
        );
        scene.world().setKineticSpeed(util.select().position(poweredShaft), 64.0F);
        scene.overlay().showText(80)
                .text("A Steam Engine on an Interface-heated boiler becomes a new thermochemical source")
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(poweredShaft))
                .placeNearTarget();
        scene.idle(90);

        BlockPos downstream = util.grid().at(6, 2, 4);
        scene.world().setBlock(
                downstream,
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(BlockStateProperties.AXIS, Direction.Axis.Z),
                false
        );
        scene.world().showSection(util.select().position(downstream), Direction.NORTH);
        scene.world().setKineticSpeed(util.select().position(downstream), 64.0F);
        scene.overlay().showText(85)
                .text("Thermochemical Shafts connected to its Powered Shaft carry that heat on a separate downstream network")
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(downstream))
                .placeNearTarget();
        scene.idle(95);

        scene.overlay().showText(75)
                .text("Engineer's Goggles show the transferred heat, RPM, and generated SU")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(engine))
                .placeNearTarget();
        scene.idle(85);
        scene.markAsFinished();
    }
}
