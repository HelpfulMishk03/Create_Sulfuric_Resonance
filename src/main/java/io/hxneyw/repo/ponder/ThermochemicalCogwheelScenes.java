package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class ThermochemicalCogwheelScenes {

    private ThermochemicalCogwheelScenes() {
    }

    public static void smallCogwheel(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "thermochemical_cogwheel.operation",
                "Thermochemical Cogwheels"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.88F);
        scene.setSceneOffsetY(-0.45F);

        BlockPos shaftPos = util.grid().at(2, 1, 2);
        BlockPos thermochemicalCogPos = util.grid().at(2, 2, 2);
        BlockPos createCogPos = util.grid().at(3, 2, 2);

        Selection shaft = util.select().position(shaftPos);
        Selection thermoCog = util.select().position(thermochemicalCogPos);
        Selection createCog = util.select().position(createCogPos);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(shaft.add(thermoCog), Direction.DOWN);
        scene.world().setKineticSpeed(shaft.add(thermoCog), 32.0F);
        scene.idle(25);

        scene.overlay().showText(115)
                .text("Thermochemical Cogwheels behave as normal Create cogwheels for kinetic rotation while also participating in thermochemical heat routes")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(thermochemicalCogPos))
                .placeNearTarget();
        scene.idle(125);

        scene.world().showSection(createCog, Direction.WEST);
        scene.world().setKineticSpeed(createCog, -32.0F);
        scene.overlay().showText(120)
                .text("They can mesh mechanically with ordinary Create cogwheels, so the kinetic network can continue normally")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(createCogPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showOutline(
                PonderPalette.RED,
                "ordinary_cog_heat_boundary",
                createCog,
                120
        );
        scene.overlay().showText(115)
                .text("Thermochemical heat does not cross into an ordinary Create cogwheel; use Thermochemical Cogwheels anywhere the heat route must continue")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(createCogPos))
                .placeNearTarget();
        scene.idle(125);

        scene.effects().indicateSuccess(thermochemicalCogPos);
        scene.overlay().showText(110)
                .text("Cogwheels relay supplied heat but do not generate heat or renew thermochemical route allowance")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(thermochemicalCogPos))
                .placeNearTarget();
        scene.idle(120);
        scene.markAsFinished();
    }

    public static void largeCogwheel(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "large_thermochemical_cogwheel.operation",
                "Large Thermochemical Cogwheels"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.88F);
        scene.setSceneOffsetY(-0.45F);

        BlockPos shaftPos = util.grid().at(2, 1, 2);
        BlockPos thermochemicalCogPos = util.grid().at(2, 2, 2);
        BlockPos createCogPos = util.grid().at(3, 2, 2);

        Selection shaft = util.select().position(shaftPos);
        Selection thermoCog = util.select().position(thermochemicalCogPos);
        Selection createCog = util.select().position(createCogPos);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(shaft.add(thermoCog), Direction.DOWN);
        scene.world().setKineticSpeed(shaft.add(thermoCog), 16.0F);
        scene.idle(25);

        scene.overlay().showText(115)
                .text("Large Thermochemical Cogwheels provide the large-cog form of the same combined kinetic and thermochemical connection")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(thermochemicalCogPos))
                .placeNearTarget();
        scene.idle(125);

        scene.world().showSection(createCog, Direction.WEST);
        scene.world().setKineticSpeed(createCog, -16.0F);
        scene.overlay().showText(120)
                .text("Their kinetic meshing follows Create's normal large-cog behavior and remains compatible with ordinary cogwheels")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(createCogPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showOutline(
                PonderPalette.RED,
                "ordinary_large_cog_heat_boundary",
                createCog,
                120
        );
        scene.overlay().showText(115)
                .text("That mechanical compatibility does not make ordinary cogwheels heat conductors; only the Thermochemical Cogwheel continues the heat route")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(createCogPos))
                .placeNearTarget();
        scene.idle(125);

        scene.effects().indicateSuccess(thermochemicalCogPos);
        scene.overlay().showText(110)
                .text("Use the large variant wherever the kinetic layout needs a large cog without sacrificing thermochemical continuity")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(thermochemicalCogPos))
                .placeNearTarget();
        scene.idle(120);
        scene.markAsFinished();
    }
}
