package io.hxneyw.repo.ponder;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.parallelthermochemicalgearbox.ParallelThermochemicalGearboxBlockEntity;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class ParallelThermochemicalGearboxScenes {

    private static final float SPEED = 32.0F;

    private ParallelThermochemicalGearboxScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "parallel_thermochemical_gearbox.operation",
                "Parallel Thermochemical Routing"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.45F);

        BlockPos gearboxPos = util.grid().at(2, 1, 2);
        BlockPos westPos = util.grid().at(1, 1, 2);
        BlockPos eastPos = util.grid().at(3, 1, 2);
        BlockPos northPos = util.grid().at(2, 1, 1);
        BlockPos southPos = util.grid().at(2, 1, 3);

        Selection gearbox = util.select().position(gearboxPos);
        Selection source = util.select().position(westPos);
        Selection straightOutput = util.select().position(eastPos);
        Selection northOutput = util.select().position(northPos);
        Selection southOutput = util.select().position(southPos);
        Selection sideOutputs = northOutput.add(southOutput);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(gearbox.add(source), Direction.DOWN);
        setKineticState(scene, util, westPos, speedForFace(Direction.WEST), null);
        configureGearbox(scene, gearbox, gearboxPos, westPos);
        scene.idle(25);

        scene.overlay().showText(115)
                .text("The Parallel Thermochemical Gearbox routes rotation and thermochemical heat through all six faces like the normal Thermochemical Gearbox")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(gearboxPos))
                .placeNearTarget();
        scene.idle(125);

        scene.world().showSection(straightOutput, Direction.WEST);
        setKineticState(
                scene,
                util,
                eastPos,
                speedForFace(Direction.EAST),
                gearboxPos
        );
        scene.world().showSection(sideOutputs, Direction.UP);
        setKineticState(
                scene,
                util,
                northPos,
                speedForFace(Direction.NORTH),
                gearboxPos
        );
        setKineticState(
                scene,
                util,
                southPos,
                speedForFace(Direction.SOUTH),
                gearboxPos
        );
        scene.idle(20);

        scene.overlay().showText(120)
                .text("Its routed output phases intentionally differ from the standard Thermochemical Gearbox; opposite faces can therefore rotate in different displayed directions")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(gearboxPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(120)
                .text("Use the Parallel variant when a compact branch needs that alternate output phase instead of treating it as a cosmetic gearbox")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(eastPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showOutline(
                PonderPalette.RED,
                "parallel_gearbox_heat_route",
                gearbox.add(source).add(straightOutput).add(sideOutputs),
                125
        );
        scene.overlay().showText(120)
                .text("Thermochemical heat still follows the same physical connections and is unaffected by the rotational phase choice")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(gearboxPos))
                .placeNearTarget();
        scene.idle(130);

        scene.effects().indicateSuccess(gearboxPos);
        scene.overlay().showText(115)
                .text("Like the normal gearbox, it relays supplied heat but does not create heat or renew route allowance")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(gearboxPos))
                .placeNearTarget();
        scene.idle(125);
        scene.markAsFinished();
    }

    private static float speedForFace(Direction face) {
        return switch (face) {
            case NORTH, UP -> SPEED;
            case WEST, EAST, SOUTH, DOWN -> -SPEED;
        };
    }

    private static void setKineticState(
            CreateSceneBuilder scene,
            SceneBuildingUtil util,
            BlockPos position,
            float speed,
            BlockPos sourcePosition
    ) {
        scene.world().setKineticSpeed(
                util.select().position(position),
                speed
        );
        scene.world().modifyBlockEntity(
                position,
                KineticBlockEntity.class,
                blockEntity -> {
                    blockEntity.source = sourcePosition;
                    blockEntity.setSpeed(speed);
                }
        );
    }

    private static void configureGearbox(
            CreateSceneBuilder scene,
            Selection gearboxSelection,
            BlockPos gearboxPos,
            BlockPos sourcePos
    ) {
        float inputSpeed = speedForFace(Direction.WEST);
        scene.world().setKineticSpeed(gearboxSelection, inputSpeed);
        scene.world().modifyBlockEntity(
                gearboxPos,
                ParallelThermochemicalGearboxBlockEntity.class,
                gearbox -> {
                    gearbox.source = sourcePos;
                    gearbox.setSpeed(inputSpeed);
                }
        );
    }
}
