package io.hxneyw.repo.ponder;

import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class ThermochemicalLinkDriveScenes {

    private static final float SPEED = 32.0F;

    private ThermochemicalLinkDriveScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "thermochemical_link_drive.operation",
                "Routing Heat Through Chain Drives"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.55F);

        BlockPos firstDrivePos = util.grid().at(1, 1, 2);
        BlockPos middleDrivePos = util.grid().at(2, 1, 2);
        BlockPos lastDrivePos = util.grid().at(3, 1, 2);
        BlockPos inputShaftPos = util.grid().at(1, 1, 1);
        BlockPos outputShaftPos = util.grid().at(3, 1, 3);
        BlockPos conduitPos = util.grid().at(3, 1, 4);

        BlockState shaftZ = AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                .defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        BlockState conduitZ = AllModBlocks.THERMOCHEMICAL_CONDUIT.get()
                .defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        BlockState firstDrive = linkDriveState(ChainDriveBlock.Part.START);
        BlockState middleDrive = linkDriveState(ChainDriveBlock.Part.MIDDLE);
        BlockState lastDrive = linkDriveState(ChainDriveBlock.Part.END);

        scene.world().setBlocks(
                util.select().position(firstDrivePos),
                firstDrive,
                false
        );
        scene.world().setBlocks(
                util.select().position(middleDrivePos),
                middleDrive,
                false
        );
        scene.world().setBlocks(
                util.select().position(lastDrivePos),
                lastDrive,
                false
        );
        scene.world().setBlocks(
                util.select().position(inputShaftPos),
                shaftZ,
                false
        );
        scene.world().setBlocks(
                util.select().position(outputShaftPos),
                shaftZ,
                false
        );
        scene.world().setBlocks(
                util.select().position(conduitPos),
                conduitZ,
                false
        );

        Selection drives = util.select().fromTo(
                firstDrivePos,
                lastDrivePos
        );
        Selection firstDriveSelection =
                util.select().position(firstDrivePos);
        Selection middleDriveSelection =
                util.select().position(middleDrivePos);
        Selection lastDriveSelection =
                util.select().position(lastDrivePos);
        Selection input = util.select().position(inputShaftPos);
        Selection output = util.select().fromTo(
                outputShaftPos,
                conduitPos
        );
        Selection upstream = firstDriveSelection.add(input);
        Selection downstream = lastDriveSelection.add(output);
        Selection fullRoute = drives.add(input).add(output);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(drives, Direction.DOWN);
        scene.world().setKineticSpeed(drives, SPEED);
        scene.idle(25);

        scene.overlay().showText(120)
                .text("Thermochemical Link Drives carry rotation and thermochemical heat through the same physical chain segment")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(middleDrivePos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(120)
                .text("Neighbouring Link Drives connect sideways while their shaft axes stay available for machine and network connections")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(firstDrivePos))
                .placeNearTarget();
        scene.idle(130);

        scene.world().showSection(input, Direction.SOUTH);
        scene.world().setKineticSpeed(input, SPEED);
        scene.idle(20);

        scene.overlay().showText(115)
                .text("Feed one end through a Thermochemical Shaft and the chain carries the kinetic network across every linked drive")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(inputShaftPos))
                .placeNearTarget();
        scene.idle(125);

        scene.world().showSection(output, Direction.NORTH);
        scene.world().setKineticSpeed(output, SPEED);
        scene.idle(20);

        scene.overlay().showText(125)
                .text("Thermochemical heat follows that same connected route instead of jumping wirelessly between Link Drives")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(outputShaftPos))
                .placeNearTarget();
        scene.idle(135);

        scene.overlay().showText(125)
                .text("Ordinary Create chain drives do not join this thermochemical chain; use Thermochemical Link Drives for the heat route")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(middleDrivePos))
                .placeNearTarget();
        scene.idle(135);

        scene.effects().indicateSuccess(conduitPos);
        scene.overlay().showText(135)
                .text("Link Drives relay supplied heat but do not generate it or renew route allowance; a Conduit still performs renewal")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(conduitPos))
                .placeNearTarget();
        scene.idle(145);

        scene.overlay().showText(125)
                .text("Every Link Drive keeps two axial shaft ports available, so one linked segment can accept or hand off rotation and thermochemical heat at several points")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(firstDrivePos))
                .placeNearTarget();
        scene.effects().indicateSuccess(firstDrivePos);
        scene.effects().indicateSuccess(lastDrivePos);
        scene.idle(135);

        scene.overlay().showText(115)
                .text("Goggles report how many of those shaft-side connections are occupied across the entire Link Drive segment")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(middleDrivePos))
                .placeNearTarget();
        scene.idle(125);

        scene.world().destroyBlock(middleDrivePos);
        scene.world().setKineticSpeed(upstream, SPEED);
        scene.world().setKineticSpeed(downstream, 0.0F);
        scene.idle(25);

        scene.overlay().showText(125)
                .text("Breaking any Link Drive in the segment interrupts the physical chain and downstream thermochemical transmission immediately")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(middleDrivePos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showControls(
                        util.vector().topOf(middleDrivePos),
                        Pointing.DOWN,
                        50
                )
                .rightClick()
                .withItem(new ItemStack(
                        AllModBlocks.THERMOCHEMICAL_LINK_DRIVE.get()
                ));
        scene.idle(60);

        scene.world().setBlock(
                middleDrivePos,
                middleDrive,
                false
        );
        scene.world().setKineticSpeed(fullRoute, SPEED);
        scene.effects().indicateSuccess(middleDrivePos);
        scene.idle(40);
        scene.markAsFinished();
    }

    private static BlockState linkDriveState(
            ChainDriveBlock.Part part
    ) {
        return AllModBlocks.THERMOCHEMICAL_LINK_DRIVE.get()
                .defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z)
                .setValue(
                        ChainDriveBlock.CONNECTED_ALONG_FIRST_COORDINATE,
                        true
                )
                .setValue(ChainDriveBlock.PART, part);
    }
}
