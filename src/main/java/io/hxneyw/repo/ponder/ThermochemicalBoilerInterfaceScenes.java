package io.hxneyw.repo.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalboilerinterface.ThermochemicalBoilerInterfaceBlock;
import io.hxneyw.repo.content.blocks.thermochemicalshaft.ThermochemicalShaftBlockEntity;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.lang.ref.WeakReference;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
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
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(0.76F);
        scene.setSceneOffsetY(-0.45F);
        scene.showBasePlate();
        scene.idle(30);

        BlockPos small = util.grid().at(1, 1, 1);
        scene.world().setBlock(
                small,
                AllModBlocks.THERMOCHEMICAL_BOILER_INTERFACE.get().defaultBlockState(),
                false
        );
        scene.world().showSection(util.select().position(small), Direction.DOWN);
        scene.idle(30);

        scene.overlay().showText(75)
                .attachKeyFrame()
                .text("Interfaces form heater arrays from one to nine blocks")
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().blockSurface(small, Direction.EAST))
                .placeNearTarget();
        scene.overlay().showControls(
                        util.vector().blockSurface(small, Direction.EAST),
                        Pointing.DOWN,
                        65
                )
                .rightClick()
                .withItem(new ItemStack(AllItems.WRENCH.get()));
        scene.idle(70);
        scene.world().modifyBlock(
                small,
                state -> state
                        .setValue(ThermochemicalBoilerInterfaceBlock.PORT_EAST, true)
                        .setValue(ThermochemicalBoilerInterfaceBlock.FACING, Direction.EAST),
                false
        );
        scene.effects().indicateSuccess(small);
        scene.idle(20);

        scene.overlay().showText(90)
                .attachKeyFrame()
                .text("Wrench another exposed face to move the array's single shaft port")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(small, Direction.SOUTH))
                .placeNearTarget();
        scene.overlay().showControls(
                        util.vector().blockSurface(small, Direction.SOUTH),
                        Pointing.DOWN,
                        75
                )
                .rightClick()
                .withItem(new ItemStack(AllItems.WRENCH.get()));
        scene.idle(80);
        scene.world().modifyBlock(
                small,
                state -> state
                        .setValue(ThermochemicalBoilerInterfaceBlock.PORT_EAST, false)
                        .setValue(ThermochemicalBoilerInterfaceBlock.PORT_SOUTH, true)
                        .setValue(ThermochemicalBoilerInterfaceBlock.FACING, Direction.SOUTH),
                false
        );
        scene.effects().indicateSuccess(small);
        scene.idle(20);
        scene.world().hideSection(util.select().position(small), Direction.UP);
        scene.idle(15);

        Selection array = util.select().fromTo(2, 1, 2, 4, 1, 4);
        Selection tanks = util.select().fromTo(2, 2, 2, 4, 2, 4);
        BlockPos westCenter = util.grid().at(2, 1, 3);
        BlockPos tankController = util.grid().at(2, 2, 2);
        BlockPos engine = util.grid().at(5, 2, 3);
        BlockState engineState = AllBlocks.STEAM_ENGINE.getDefaultState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST);

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
                    state = state
                            .setValue(ThermochemicalBoilerInterfaceBlock.PORT_WEST, true)
                            .setValue(ThermochemicalBoilerInterfaceBlock.FACING, Direction.WEST);
                }

                scene.world().setBlock(pos, state, false);
                scene.world().setBlock(
                        util.grid().at(x, 2, z),
                        AllBlocks.FLUID_TANK.getDefaultState(),
                        false
                );
            }
        }
        scene.world().modifyBlockEntity(
                tankController,
                FluidTankBlockEntity.class,
                tank -> ConnectivityHandler.formMulti(tank)
        );
        scene.world().setBlock(engine, engineState, false);
        scene.world().modifyBlockEntity(
                tankController,
                FluidTankBlockEntity.class,
                FluidTankBlockEntity::updateBoilerState
        );

        scene.world().showSection(array, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .text("Touching Interfaces merge and hide their internal faces")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(util.grid().at(3, 1, 3)))
                .placeNearTarget();
        scene.idle(85);

        scene.overlay().showText(80)
                .attachKeyFrame()
                .text("A 3x3 keeps one port at the middle of an outer edge")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(westCenter))
                .placeNearTarget();
        scene.idle(85);

        scene.world().showSection(tanks, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(85)
                .attachKeyFrame()
                .text("Each Interface sits directly beneath its matching boiler tank")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(util.grid().at(3, 2, 3)))
                .placeNearTarget();
        scene.idle(90);

        BlockPos rotor = util.grid().at(0, 1, 3);
        BlockPos inputShaft = util.grid().at(1, 1, 3);
        scene.world().setBlock(
                rotor,
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                        .defaultBlockState()
                        .setValue(MoltenRotorBlock.FACING, Direction.NORTH)
                        .setValue(MoltenRotorBlock.HEAT_LEVEL, HeatLevel.SEETHING),
                false
        );
        scene.world().modifyBlockEntity(
                rotor,
                MoltenRotorBlockEntity.class,
                furnace -> {
                    furnace.setCreativeMode(true);
                    furnace.cycleCreativeTier();
                }
        );
        scene.world().setBlock(
                inputShaft,
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(BlockStateProperties.AXIS, Direction.Axis.X),
                false
        );
        scene.world().modifyBlockEntity(
                inputShaft,
                ThermochemicalShaftBlockEntity.class,
                shaft -> shaft.source = rotor
        );
        scene.world().modifyBlock(
                westCenter,
                state -> state
                        .setValue(ThermochemicalBoilerInterfaceBlock.INPUT_ACTIVE, true)
                        .setValue(ThermochemicalBoilerInterfaceBlock.FACING, Direction.WEST),
                false
        );
        scene.world().showSection(
                util.select().position(rotor).add(util.select().position(inputShaft)),
                Direction.EAST
        );
        scene.world().setKineticSpeed(
                util.select().position(rotor).add(util.select().position(inputShaft)),
                -128.0F
        );
        scene.effects().indicateSuccess(westCenter);
        scene.idle(20);
        scene.overlay().showText(95)
                .attachKeyFrame()
                .text("The port reads thermochemical heat without consuming or transmitting SU")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(inputShaft))
                .placeNearTarget();
        scene.idle(100);

        BlockPos poweredShaft = util.grid().at(7, 2, 3);

        scene.world().showSection(util.select().position(engine), Direction.WEST);
        scene.idle(20);
        scene.overlay().showText(75)
                .attachKeyFrame()
                .text("A Steam Engine mounts normally to the heated boiler")
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(engine))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(
                        util.vector().centerOf(poweredShaft),
                        Pointing.DOWN,
                        65
                )
                .rightClick()
                .withItem(AllBlocks.SHAFT.asStack());
        scene.idle(40);
        scene.world().setBlock(
                poweredShaft,
                AllBlocks.POWERED_SHAFT.getDefaultState()
                        .setValue(ShaftBlock.AXIS, Direction.Axis.Z),
                false
        );
        scene.world().modifyBlockEntity(
                poweredShaft,
                PoweredShaftBlockEntity.class,
                shaft -> {
                    shaft.initialTicks = 0;
                    shaft.update(engine, -1, 1.0F);
                }
        );
        scene.world().modifyBlockEntity(
                engine,
                SteamEngineBlockEntity.class,
                steamEngine -> {
                    FluidTankBlockEntity controller = steamEngine.getTank();
                    if (controller != null) {
                        steamEngine.source = new WeakReference<>(controller);
                    }
                    PoweredShaftBlockEntity shaft = steamEngine.getShaft();
                    if (shaft != null) {
                        steamEngine.target = new WeakReference<>(shaft);
                    }
                }
        );
        scene.world().showSection(util.select().position(poweredShaft), Direction.WEST);
        scene.world().setKineticSpeed(util.select().position(poweredShaft), -64.0F);
        scene.effects().indicateSuccess(poweredShaft);
        scene.idle(25);
        scene.overlay().showText(85)
                .attachKeyFrame()
                .text("Adding a Shaft completes Create's moving Steam Engine assembly")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(poweredShaft))
                .placeNearTarget();
        scene.idle(90);

        BlockPos downstream = util.grid().at(7, 2, 4);
        scene.overlay().showControls(
                        util.vector().centerOf(downstream),
                        Pointing.DOWN,
                        65
                )
                .rightClick()
                .withItem(new ItemStack(
                        AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                ));
        scene.idle(40);
        scene.world().setBlock(
                downstream,
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(ShaftBlock.AXIS, Direction.Axis.Z),
                false
        );
        scene.world().modifyBlockEntity(
                downstream,
                ThermochemicalShaftBlockEntity.class,
                shaft -> shaft.source = poweredShaft
        );
        scene.world().showSection(util.select().position(downstream), Direction.NORTH);
        scene.world().setKineticSpeed(
                util.select().position(poweredShaft).add(util.select().position(downstream)),
                -64.0F
        );
        scene.idle(25);
        scene.overlay().showText(90)
                .attachKeyFrame()
                .text("A Thermochemical Shaft carries this boiler's heat; RPM and SU come from the boiler")
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(downstream))
                .placeNearTarget();
        scene.idle(95);

        scene.overlay().showControls(
                        util.vector().centerOf(engine),
                        Pointing.DOWN,
                        70
                )
                .withItem(AllItems.GOGGLES.asStack());
        scene.overlay().showText(85)
                .attachKeyFrame()
                .text("Input and output stay separate; Goggles show carried heat and engine output")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(downstream))
                .placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }
}
