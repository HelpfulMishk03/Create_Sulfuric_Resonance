package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class AshCeramicCrucibleScenes {

    private AshCeramicCrucibleScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "ash_ceramic_crucible.operation",
                "Processing in a Ceramic Crucible"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.55F);

        BlockPos injectorPos = util.grid().at(2, 1, 2);
        BlockPos cruciblePos = util.grid().at(2, 2, 2);
        BlockPos mixerPos = util.grid().at(2, 4, 2);

        Selection injector = util.select().position(injectorPos);
        Selection crucible = util.select().position(cruciblePos);
        Selection mixer = util.select().position(mixerPos);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(crucible, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(115)
                .text("The Ash Ceramic Crucible is a heat-resistant Create Basin-compatible vessel used by CSR processing")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(cruciblePos))
                .placeNearTarget();
        scene.idle(125);

        scene.world().showSection(mixer, Direction.DOWN);
        scene.world().setKineticSpeed(mixer, 32.0F);
        scene.overlay().showText(120)
                .text("A Mechanical Mixer works above it the same way it works above a normal Create Basin")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(mixerPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(125)
                .text("Normal Basin processing can use the Crucible where Create permits it, including mixing and packing-style recipe families")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(cruciblePos))
                .placeNearTarget();
        scene.idle(135);

        scene.world().showSection(injector, Direction.UP);
        scene.overlay().showText(120)
                .text("CSR Combustion Mixing is stricter: it requires the Ceramic Crucible and sufficient Combustion heat beneath it")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(injectorPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(120)
                .text("A Molten Rotor Furnace or Resonant Heat Injector can provide that heat when the connected system reaches the required tier")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(injectorPos))
                .placeNearTarget();
        scene.idle(130);

        scene.effects().indicateSuccess(cruciblePos);
        scene.overlay().showText(110)
                .text("Use the Crucible when a recipe specifically calls for CSR's heat-resistant basin processing path")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(cruciblePos))
                .placeNearTarget();
        scene.idle(120);
        scene.markAsFinished();
    }
}
