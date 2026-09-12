package io.hxneyw.repo.ponder;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.Items;
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

public final class MoltenRotorAfterburnScenes {

    private static final float RADIANT_RPM = -256.0F;

    private MoltenRotorAfterburnScenes() {
    }

    public static void afterburn(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "molten_rotor.afterburn",
                "Pushing Beyond Radiant"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9F);
        scene.setSceneOffsetY(-0.4F);

        BlockPos furnacePos = util.grid().at(2, 1, 2);
        BlockPos westShaftPos = util.grid().at(1, 1, 2);
        BlockPos eastShaftPos = util.grid().at(3, 1, 2);

        Selection furnace =
                util.select().position(furnacePos);
        Selection shafts =
                util.select()
                        .position(westShaftPos)
                        .add(util.select().position(eastShaftPos));
        Selection kinetic =
                furnace.add(shafts);

        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(
                furnace,
                Direction.DOWN
        );
        scene.idle(15);

        scene.world().modifyBlock(
                furnacePos,
                state -> state.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        HeatLevel.SEETHING
                ),
                false
        );

        scene.world().showSection(
                shafts,
                Direction.DOWN
        );
        scene.world().setKineticSpeed(
                kinetic,
                RADIANT_RPM
        );
        scene.idle(20);

        scene.overlay()
                .showText(120)
                .text(
                        "Radiant remains the highest normal heat range, ending at 1599\u00b0C"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(130);

        scene.overlay()
                .showControls(
                        util.vector()
                                .topOf(furnacePos)
                                .add(0.0, 0.25, 0.0),
                        Pointing.DOWN,
                        60
                )
                .rightClick()
                .withItem(
                        new ItemStack(
                                Items.THERMITE_CHARGE.get()
                        )
                );

        scene.overlay()
                .showText(130)
                .text(
                        "Thermite Charge is CSR's built-in Afterburn fuel, capable of pushing the Furnace to 2000\u00b0C"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(furnacePos))
                .placeNearTarget();

        scene.idle(140);

        scene.overlay()
                .showText(125)
                .text(
                        "Any data-driven Molten Rotor fuel must explicitly support temperatures above 1599\u00b0C before it can enter Afterburn"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(135);

        scene.effects().indicateSuccess(furnacePos);

        scene.overlay().showOutline(
                PonderPalette.RED,
                "molten_rotor_afterburn_range",
                furnace,
                130
        );

        scene.overlay()
                .showText(125)
                .text(
                        "The moment temperature rises above 1599\u00b0C, the Furnace enters Afterburn; its operating range extends from 1600\u00b0C to 2000\u00b0C"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(135);

        scene.overlay()
                .showText(135)
                .text(
                        "Afterburn is a Molten Rotor Furnace operating state, not a new thermochemical heat tier"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(145);

        scene.overlay().showOutline(
                PonderPalette.GREEN,
                "molten_rotor_afterburn_stress",
                kinetic,
                135
        );

        scene.overlay()
                .showText(130)
                .text(
                        "While Afterburning, generated stress capacity increases to 1.5x its normal Radiant value"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(eastShaftPos))
                .placeNearTarget();

        scene.idle(140);

        scene.world().setKineticSpeed(
                kinetic,
                RADIANT_RPM
        );

        scene.overlay()
                .showText(135)
                .text(
                        "RPM does not increase, and Afterburn does not make processing recipes run faster"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(eastShaftPos))
                .placeNearTarget();

        scene.idle(145);

        scene.overlay()
                .showText(140)
                .text(
                        "When fuel ends above 1599\u00b0C, the Furnace cools at 80% of its normal rate, holding the Afterburn range longer"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(150);

        scene.overlay()
                .showText(125)
                .text(
                        "At 1599\u00b0C and below, Afterburn ends and normal Radiant cooling behavior resumes"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(135);
        scene.markAsFinished();
    }
}