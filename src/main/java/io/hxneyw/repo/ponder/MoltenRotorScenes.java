package io.hxneyw.repo.ponder;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class MoltenRotorScenes {

    private MoltenRotorScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "molten_rotor.operation",
                "Powering the Molten Rotor Furnace"
        );

        BlockPos furnacePos = util.grid().at(2, 1, 2);
        BlockPos leftShaftPos = util.grid().at(1, 1, 2);
        BlockPos rightShaftPos = util.grid().at(3, 1, 2);

        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(
                util.select().position(furnacePos),
                Direction.DOWN
        );

        scene.idle(20);

        scene.overlay()
                .showText(80)
                .text(
                        "The Molten Rotor Furnace converts heat into rotational force"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(90);

        scene.world().showSection(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(rightShaftPos)),
                Direction.DOWN
        );

        scene.idle(20);

        scene.overlay()
                .showText(80)
                .text(
                        "Shafts can connect to either side of the furnace"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(rightShaftPos))
                .placeNearTarget();

        scene.idle(90);

        scene.world().modifyBlockEntity(
                furnacePos,
                MoltenRotorBlockEntity.class,
                furnace -> {
                    furnace.setCreativeMode(true);
                    furnace.cycleCreativeTier();
                }
        );

        scene.world().modifyBlock(
                furnacePos,
                state -> state.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        HeatLevel.SEETHING
                ),
                false
        );

        scene.world().setKineticSpeed(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(furnacePos))
                        .add(util.select().position(rightShaftPos)),
                64.0F
        );

        scene.idle(30);

        scene.overlay()
                .showText(90)
                .text(
                        "Fuel heats the chamber and causes the internal impeller to accelerate"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(100);

        scene.overlay()
                .showText(90)
                .text(
                        "As temperature rises, the furnace produces greater rotational output"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(rightShaftPos))
                .placeNearTarget();

        scene.idle(100);

        scene.overlay()
                .showText(90)
                .text(
                        "The heat gauge shows the furnace's current operating temperature"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(
                        util.vector()
                                .centerOf(furnacePos)
                                .add(0.20, 0.20, -0.45)
                )
                .placeNearTarget();

        scene.idle(100);
        scene.markAsFinished();
    }
}
