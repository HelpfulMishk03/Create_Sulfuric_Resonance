package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.catalystbed.CatalystBedBlock;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class CatalystBedScenes {

    private CatalystBedScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "catalyst_bed.operation",
                "Accelerating Chamber Reactions"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.45F);

        BlockPos bedPos = util.grid().at(2, 1, 2);
        BlockPos chamberPos = bedPos.above();

        BlockState disconnectedBed = AllModBlocks.CATALYST_BED.get()
                .defaultBlockState()
                .setValue(CatalystBedBlock.CONNECTED, false);
        BlockState connectedBed = disconnectedBed.setValue(
                CatalystBedBlock.CONNECTED,
                true
        );
        BlockState chamberState =
                AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()
                        .defaultBlockState();

        scene.world().setBlock(bedPos, disconnectedBed, false);

        Selection bed = util.select().position(bedPos);
        Selection chamber = util.select().position(chamberPos);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(bed, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("Place a Catalyst Bed directly beneath a Sulfuric Resonance Chamber")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(bedPos))
                .placeNearTarget();
        scene.idle(110);

        scene.world().setBlock(chamberPos, chamberState, false);
        scene.world().setBlock(bedPos, connectedBed, false);
        scene.world().showSection(chamber, Direction.DOWN);
        scene.effects().indicateSuccess(bedPos);
        scene.idle(25);

        scene.overlay().showOutline(
                PonderPalette.GREEN,
                "catalyst_bed_connection",
                bed.add(chamber),
                110
        );
        scene.overlay().showText(105)
                .text("The raised connector appears only while a valid Chamber is directly above")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(bedPos))
                .placeNearTarget();
        scene.idle(115);

        scene.overlay().showText(120)
                .text("An active Bed advances live Chamber processing at 1.5× normal speed")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(chamberPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(120)
                .text("Recipe durations and ingredients stay unchanged, including any catalyst ingredient")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(chamberPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(130)
                .text("Only the Bed immediately below applies; removing it safely returns the current process to normal speed")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(bedPos))
                .placeNearTarget();
        scene.idle(140);
        scene.markAsFinished();
    }
}
