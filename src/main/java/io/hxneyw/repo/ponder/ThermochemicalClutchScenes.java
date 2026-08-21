package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.thermochemicalclutch.ThermochemicalClutchBlock;
import io.hxneyw.repo.content.blocks.thermochemicalclutch.ThermochemicalClutchBlockEntity;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ThermochemicalClutchScenes {

    private ThermochemicalClutchScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "thermochemical_clutch.operation",
                "Interrupting Heat and Rotation"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9F);
        scene.setSceneOffsetY(-0.5F);

        BlockPos disconnectedPos = util.grid().at(1, 1, 2);
        BlockPos clutchPos = util.grid().at(2, 1, 2);
        BlockPos drivenPos = util.grid().at(3, 1, 2);
        BlockPos conduitPos = util.grid().at(4, 1, 2);
        BlockPos redstonePos = util.grid().at(2, 2, 2);

        Selection disconnected = util.select().position(disconnectedPos);
        Selection clutch = util.select().position(clutchPos);
        Selection driven = util.select().position(drivenPos);
        Selection conduit = util.select().position(conduitPos);
        Selection line = disconnected.add(clutch).add(driven).add(conduit);
        Selection redstone = util.select().position(redstonePos);
        Vec3 clutchCenter = util.vector().centerOf(clutchPos);
        Vec3 disconnectedCenter = util.vector().centerOf(disconnectedPos);

        BlockState shaftX = AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                .defaultBlockState()
                .setValue(
                        RotatedPillarBlock.AXIS,
                        Direction.Axis.X
                );
        BlockState conduitX = AllModBlocks.THERMOCHEMICAL_CONDUIT.get()
                .defaultBlockState()
                .setValue(
                        RotatedPillarBlock.AXIS,
                        Direction.Axis.X
                );
        BlockState clutchOpen = AllModBlocks.THERMOCHEMICAL_CLUTCH.get()
                .defaultBlockState()
                .setValue(
                        ThermochemicalClutchBlock.AXIS,
                        Direction.Axis.X
                )
                .setValue(
                        ThermochemicalClutchBlock.POWERED,
                        false
                );

        scene.world().setBlock(disconnectedPos, shaftX, false);
        scene.world().setBlock(clutchPos, clutchOpen, false);
        scene.world().setBlock(drivenPos, shaftX, false);
        scene.world().setBlock(conduitPos, conduitX, false);

        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(line, Direction.DOWN);
        scene.world().setKineticSpeed(line, 32.0F);
        scene.world().modifyBlockEntity(
                clutchPos,
                ThermochemicalClutchBlockEntity.class,
                blockEntity -> blockEntity.setSource(drivenPos)
        );
        scene.idle(20);

        scene.overlay()
                .showText(110)
                .text(
                        "The Thermochemical Clutch is an inline control point for both kinetic rotation and thermochemical heat"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(clutchCenter)
                .placeNearTarget();
        scene.idle(120);

        scene.overlay()
                .showText(110)
                .text(
                        "Without a redstone signal, both systems pass straight through along the Clutch's shaft axis"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(disconnectedCenter)
                .placeNearTarget();
        scene.idle(120);

        scene.world().setBlock(
                redstonePos,
                Blocks.REDSTONE_BLOCK.defaultBlockState(),
                false
        );
        scene.world().showSection(redstone, Direction.DOWN);
        scene.world().modifyBlock(
                clutchPos,
                state -> state.setValue(
                        ThermochemicalClutchBlock.POWERED,
                        true
                ),
                false
        );
        scene.world().setKineticSpeed(disconnected, 0.0F);
        scene.effects().indicateRedstone(clutchPos);
        scene.idle(20);

        scene.overlay()
                .showText(115)
                .text(
                        "Powering the Clutch disconnects the far side and stops transmitted rotation immediately"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(clutchCenter)
                .placeNearTarget();
        scene.idle(125);

        scene.overlay()
                .showText(120)
                .text(
                        "The same powered state blocks thermochemical heat, making the Clutch a hard cutoff for the entire route"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(disconnectedCenter)
                .placeNearTarget();
        scene.idle(130);

        scene.world().hideSection(redstone, Direction.UP);
        scene.world().modifyBlock(
                clutchPos,
                state -> state.setValue(
                        ThermochemicalClutchBlock.POWERED,
                        false
                ),
                false
        );
        scene.world().showSection(line, Direction.DOWN);
        scene.world().setKineticSpeed(line, 32.0F);
        scene.idle(20);

        scene.overlay()
                .showText(110)
                .text(
                        "Remove the redstone signal and both rotation and thermochemical heat reconnect through the line"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(disconnectedCenter)
                .placeNearTarget();
        scene.idle(120);

        scene.overlay()
                .showText(125)
                .text(
                        "There is no GUI, mode, threshold, storage, or amplification: redstone alone decides whether the route passes or stops"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(clutchCenter)
                .placeNearTarget();
        scene.idle(135);

        scene.markAsFinished();
    }
}
