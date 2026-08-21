package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.Items;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public final class SulfuricResonanceChamberScenes {

    private static final float SPEED = 32.0F;

    private SulfuricResonanceChamberScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "sulfuric_resonance_chamber.operation",
                "Resonating Advanced Materials"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.45F);

        BlockPos chamberPos = util.grid().at(2, 1, 2);
        BlockPos acidPipePos = util.grid().at(1, 1, 2);
        BlockPos thermochemicalShaftPos = util.grid().at(3, 1, 2);

        Selection chamber = util.select().position(chamberPos);
        Selection acidPipe = util.select().position(acidPipePos);
        Selection shaft = util.select().position(thermochemicalShaftPos);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(chamber, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("The Sulfuric Resonance Chamber combines thermochemical heat, rotation, Sulfuric Acid, and item reagents in one advanced reaction")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(chamberPos))
                .placeNearTarget();
        scene.idle(130);

        scene.world().showSection(shaft, Direction.WEST);
        scene.world().setKineticSpeed(shaft.add(chamber), SPEED);
        scene.overlay().showOutline(
                PonderPalette.RED,
                "chamber_thermochemical_input",
                shaft,
                110
        );
        scene.overlay().showText(105)
                .text("The shaft side accepts both rotational power and thermochemical heat from a valid thermochemical network")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(thermochemicalShaftPos))
                .placeNearTarget();
        scene.idle(115);

        scene.world().showSection(acidPipe, Direction.EAST);
        scene.overlay().showControls(
                        util.vector().centerOf(acidPipePos),
                        Pointing.DOWN,
                        55
                )
                .withItem(new ItemStack(Items.SULFURIC_ACID_BUCKET.get()));
        scene.overlay().showText(105)
                .text("Sulfuric Acid enters through the dedicated fluid side; the other faces are not general-purpose fluid ports")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(acidPipePos))
                .placeNearTarget();
        scene.idle(115);

        scene.overlay().showControls(
                        util.vector().topOf(chamberPos),
                        Pointing.DOWN,
                        55
                )
                .withItem(new ItemStack(net.minecraft.world.item.Items.COPPER_INGOT));
        scene.overlay().showControls(
                        util.vector().topOf(chamberPos).add(0.25D, 0.0D, 0.0D),
                        Pointing.DOWN,
                        55
                )
                .withItem(new ItemStack(Items.ACTIVATED_SULFUR_CATALYST.get()));
        scene.overlay().showControls(
                        util.vector().topOf(chamberPos).add(-0.25D, 0.0D, 0.0D),
                        Pointing.DOWN,
                        55
                )
                .withItem(new ItemStack(Items.UNFINISHED_THERMAL_MATRIX.get()));
        scene.overlay().showText(120)
                .text("Recipes always use a substrate and may additionally require a catalyst and auxiliary reagent")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(chamberPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(120)
                .text("Item automation is intentionally limited to the top and back interfaces; the front, shaft side, acid side, and bottom are not item ports")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(chamberPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(130)
                .text("The Chamber becomes READY only when its Sulfuric Acid amount, minimum heat tier, minimum RPM, ingredients, and output space are all satisfied")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(chamberPos))
                .placeNearTarget();
        scene.idle(140);

        scene.overlay().showControls(
                        util.vector().topOf(chamberPos),
                        Pointing.DOWN,
                        55
                )
                .rightClick();
        scene.overlay().showText(130)
                .text("The GUI has Automatic and Manual operating modes: Automatic starts a READY recipe immediately, while Manual waits for the player to press Start")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(chamberPos))
                .placeNearTarget();
        scene.idle(140);

        scene.overlay().showText(120)
                .text("While processing, the input batch is locked against new insertion, but manually removing an input safely aborts and resets the reaction")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(chamberPos))
                .placeNearTarget();
        scene.idle(130);

        scene.effects().indicateSuccess(chamberPos);
        scene.overlay().showText(120)
                .text("The rings build visual intensity during processing and now cool down smoothly after completion instead of snapping instantly dark")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(chamberPos))
                .placeNearTarget();
        scene.idle(130);
        scene.markAsFinished();
    }
}
