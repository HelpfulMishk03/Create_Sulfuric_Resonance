package io.hxneyw.repo.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.resonantheatinjector.ResonantHeatInjectorBlock;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ResonantHeatInjectorScenes {

    private ResonantHeatInjectorScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "resonant_heat_injector.operation",
                "Applying Network Heat Directly"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9F);
        scene.setSceneOffsetY(-0.5F);

        BlockPos injectorPos = util.grid().at(2, 1, 2);
        BlockPos basinPos = injectorPos.above();
        BlockPos shaftPos = util.grid().at(3, 1, 2);
        BlockPos conduitPos = util.grid().at(4, 1, 2);

        BlockState injectorState = AllModBlocks.RESONANT_HEAT_INJECTOR.get()
                .defaultBlockState()
                .setValue(ResonantHeatInjectorBlock.FACING, Direction.NORTH);
        BlockState shaftState = AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                .defaultBlockState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.X);
        BlockState conduitState = AllModBlocks.THERMOCHEMICAL_CONDUIT.get()
                .defaultBlockState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.X);

        scene.world().setBlocks(util.select().position(injectorPos), injectorState, false);
        scene.world().setBlocks(util.select().position(shaftPos), shaftState, false);
        scene.world().setBlocks(util.select().position(conduitPos), conduitState, false);
        scene.world().setBlocks(
                util.select().position(basinPos),
                AllBlocks.BASIN.getDefaultState(),
                false
        );

        Selection injector = util.select().position(injectorPos);
        Selection network = util.select().position(shaftPos)
                .add(util.select().position(conduitPos));
        Selection kinetic = util.select().position(shaftPos)
                .add(util.select().position(conduitPos))
                .add(util.select().position(injectorPos));
        Selection basin = util.select().position(basinPos);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(injector, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("Resonant Heat Injectors are endpoints for thermochemical heat networks")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(injectorPos))
                .placeNearTarget();
        scene.idle(110);

        scene.world().showSection(network, Direction.WEST);
        scene.world().setKineticSpeed(kinetic, 64.0F);
        scene.idle(20);

        scene.overlay().showText(105)
                .text("Connect the side shaft to a valid thermochemical route")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(shaftPos))
                .placeNearTarget();
        scene.idle(115);

        scene.overlay().showText(120)
                .text("The Injector never creates, stores, or amplifies heat; it forwards the tier supplied by the network")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(injectorPos))
                .placeNearTarget();
        scene.idle(130);

        scene.world().showSection(basin, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(105)
                .text("Place a Create Basin or Ash Ceramic Crucible directly above the Injector")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(basinPos))
                .placeNearTarget();
        scene.idle(115);

        scene.overlay().showText(90)
                .text("Supplied heat is applied upward only")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().topOf(injectorPos))
                .placeNearTarget();
        scene.effects().indicateSuccess(basinPos);
        scene.idle(100);

        scene.overlay().showText(120)
                .text("When the network supplies Combustion-grade heat, the Injector delivers that tier upward for compatible CSR processing")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(basinPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(105)
                .text("Rotation terminates at the Injector; there is no rotational output on another face")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(injectorPos))
                .placeNearTarget();
        scene.idle(115);
        scene.markAsFinished();
    }
}
