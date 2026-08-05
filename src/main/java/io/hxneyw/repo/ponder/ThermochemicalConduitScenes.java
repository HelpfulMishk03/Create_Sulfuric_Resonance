package io.hxneyw.repo.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ThermochemicalConduitScenes {

    private ThermochemicalConduitScenes() {
    }

    @SuppressWarnings("DuplicatedCode")
    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "thermochemical_conduit.operation",
                "Renewing a Thermochemical Route"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9F);
        scene.setSceneOffsetY(-0.5F);


        BlockPos sourcePos = util.grid().at(0, 1, 2);
        BlockPos inputShaftPos = util.grid().at(1, 1, 2);
        BlockPos conduitPos = util.grid().at(2, 1, 2);
        BlockPos outputShaft1Pos = util.grid().at(3, 1, 2);
        BlockPos outputShaft2Pos = util.grid().at(4, 1, 2);

        Selection sourceSelection =
                util.select().position(sourcePos);
        Selection inputShaftSelection =
                util.select().position(inputShaftPos);
        Selection conduitSelection =
                util.select().position(conduitPos);
        Selection downstreamSelection =
                util.select().fromTo(
                        outputShaft1Pos,
                        outputShaft2Pos
                );
        Selection fullRouteSelection =
                util.select().fromTo(
                        sourcePos,
                        outputShaft2Pos
                );

        Vec3 conduitCenter =
                util.vector().centerOf(conduitPos);
        Vec3 conduitTop =
                util.vector().topOf(conduitPos);
        Vec3 sourceCenter =
                util.vector().centerOf(sourcePos);
        Vec3 downstreamCenter =
                util.vector().centerOf(outputShaft1Pos);

        BlockState shaftAlongX =
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(
                                RotatedPillarBlock.AXIS,
                                Direction.Axis.X
                        );

        BlockState heatedRotor =
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                        .defaultBlockState()
                        .setValue(
                                MoltenRotorBlock.FACING,
                                Direction.NORTH
                        )
                        .setValue(
                                MoltenRotorBlock.HEAT_LEVEL,
                                HeatLevel.SEETHING
                        );

        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(
                conduitSelection,
                Direction.DOWN
        );
        scene.world().setKineticSpeed(
                conduitSelection,
                32.0F
        );
        scene.idle(20);

        scene.overlay()
                .showText(105)
                .text(
                        "Thermochemical Conduits transmit ordinary kinetic rotation through their internal shaft"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(115);

        scene.overlay()
                .showText(115)
                .text(
                        "They also renew the distance available to a physically connected thermochemical heat route"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(125);

        scene.overlay()
                .showText(115)
                .text(
                        "A Conduit does not generate heat and cannot collect it wirelessly from nearby machinery"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(125);

        scene.world().setBlock(
                sourcePos,
                heatedRotor,
                false
        );
        scene.world().setBlock(
                inputShaftPos,
                shaftAlongX,
                false
        );

        scene.world().showSection(
                sourceSelection,
                Direction.DOWN
        );
        scene.world().showSection(
                inputShaftSelection,
                Direction.WEST
        );
        scene.world().setKineticSpeed(
                util.select().fromTo(
                        sourcePos,
                        conduitPos
                ),
                32.0F
        );
        scene.idle(25);

        scene.overlay()
                .showText(130)
                .text(
                        "Here, a heated Molten Rotor reaches the Conduit through a real Thermochemical Shaft connection"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(sourceCenter)
                .placeNearTarget();
        scene.idle(140);

        scene.world().setBlock(
                outputShaft1Pos,
                shaftAlongX,
                false
        );
        scene.world().setBlock(
                outputShaft2Pos,
                shaftAlongX,
                false
        );
        scene.world().showSection(
                downstreamSelection,
                Direction.WEST
        );
        scene.world().setKineticSpeed(
                fullRouteSelection,
                32.0F
        );
        scene.idle(25);

        scene.overlay()
                .showText(135)
                .text(
                        "Once the Conduit is reached, it starts a fresh downstream allowance of ten transmission segments"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(145);

        scene.overlay()
                .showText(120)
                .text(
                        "More Thermochemical Shafts can now continue carrying both rotation and heat beyond it"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(downstreamCenter)
                .placeNearTarget();
        scene.idle(130);

        scene.overlay()
                .showControls(
                        conduitTop.add(0.0, 0.35, 0.0),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .withItem(new ItemStack(AllItems.WRENCH.get()));

        scene.overlay()
                .showText(110)
                .text(
                        "A Wrench can rotate the Conduit between the X, Y, and Z axes even while it is connected"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(120);

        scene.world().modifyBlock(
                conduitPos,
                state -> state.setValue(
                        RotatedPillarBlock.AXIS,
                        Direction.Axis.Z
                ),
                false
        );
        scene.world().setKineticSpeed(
                conduitSelection,
                0.0F
        );
        scene.world().setKineticSpeed(
                downstreamSelection,
                0.0F
        );
        scene.idle(20);

        scene.overlay()
                .showText(135)
                .text(
                        "Rotating it away from the line breaks that physical route, so the downstream Shafts stop receiving rotation and heat"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(145);

        scene.world().modifyBlock(
                conduitPos,
                state -> state.setValue(
                        RotatedPillarBlock.AXIS,
                        Direction.Axis.X
                ),
                false
        );
        scene.world().setKineticSpeed(
                conduitSelection,
                32.0F
        );
        scene.world().setKineticSpeed(
                downstreamSelection,
                32.0F
        );
        scene.idle(20);

        scene.overlay()
                .showText(115)
                .text(
                        "Rotate it back into alignment to restore the complete network immediately"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(125);

        scene.world().setBlock(
                outputShaft1Pos,
                Blocks.AIR.defaultBlockState(),
                false
        );
        scene.world().setKineticSpeed(
                util.select().position(outputShaft2Pos),
                0.0F
        );
        scene.idle(20);

        scene.overlay()
                .showText(115)
                .text(
                        "Breaking any required Shaft also interrupts everything beyond the gap"
                )
                .colored(PonderPalette.OUTPUT)
                .pointAt(downstreamCenter)
                .placeNearTarget();
        scene.idle(125);

        scene.overlay()
                .showControls(
                        util.vector().topOf(outputShaft1Pos)
                                .add(0.0, 0.25, 0.0),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .withItem(
                        new ItemStack(
                                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        )
                );

        scene.overlay()
                .showText(125)
                .text(
                        "Replace the missing Thermochemical Shaft to reconnect the downstream route"
                )
                .colored(PonderPalette.INPUT)
                .pointAt(downstreamCenter)
                .placeNearTarget();
        scene.idle(100);

        scene.world().setBlock(
                outputShaft1Pos,
                shaftAlongX,
                false
        );
        scene.world().setKineticSpeed(
                downstreamSelection,
                32.0F
        );
        scene.idle(25);

        scene.overlay()
                .showText(130)
                .text(
                        "Use Conduits as renewal points, but keep every Shaft aligned and physically connected"
                )
                .colored(PonderPalette.MEDIUM)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(140);

        scene.markAsFinished();
    }
}
