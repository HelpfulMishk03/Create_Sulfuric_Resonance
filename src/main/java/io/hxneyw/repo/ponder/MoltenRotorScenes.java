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
                .showText(90)
                .text(
                        "The Molten Rotor Furnace converts stored heat into rotational force"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(100);

        scene.world().showSection(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(rightShaftPos)),
                Direction.DOWN
        );

        scene.idle(20);

        scene.overlay()
                .showText(85)
                .text(
                        "Shafts can connect to either side of the furnace"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(rightShaftPos))
                .placeNearTarget();

        scene.idle(95);

        scene.overlay()
                .showText(95)
                .text(
                        "At ambient temperature, the furnace remains idle until it reaches 300°C"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(105);

        /*
         * Creative heat mode is used only inside the demonstration so the
         * scene can move through its heat states without waiting for fuel.
         */
        scene.world().modifyBlockEntity(
                furnacePos,
                MoltenRotorBlockEntity.class,
                furnace -> furnace.setCreativeMode(true)
        );

        scene.world().modifyBlock(
                furnacePos,
                state -> state.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        HeatLevel.KINDLED
                ),
                false
        );

        /*
         * Negative speeds intentionally reverse the original Ponder shaft
         * direction so it matches the requested in-game presentation.
         */
        scene.world().setKineticSpeed(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(furnacePos))
                        .add(util.select().position(rightShaftPos)),
                -32.0F
        );

        scene.idle(25);

        scene.overlay()
                .showText(90)
                .text(
                        "Smouldering begins at 300°C and produces 32 RPM"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(100);

        scene.world().setKineticSpeed(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(furnacePos))
                        .add(util.select().position(rightShaftPos)),
                -64.0F
        );

        scene.overlay()
                .showText(90)
                .text(
                        "Kindled begins at 500°C, raising output to 64 RPM"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(rightShaftPos))
                .placeNearTarget();

        scene.idle(100);

        scene.world().modifyBlockEntity(
                furnacePos,
                MoltenRotorBlockEntity.class,
                MoltenRotorBlockEntity::cycleCreativeTier
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
                -128.0F
        );

        scene.idle(25);

        scene.overlay()
                .showText(100)
                .text(
                        "Seething begins at 800°C, producing 128 RPM and meeting the Superheated requirement"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(110);

        scene.world().modifyBlockEntity(
                furnacePos,
                MoltenRotorBlockEntity.class,
                MoltenRotorBlockEntity::cycleCreativeTier
        );

        scene.world().setKineticSpeed(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(furnacePos))
                        .add(util.select().position(rightShaftPos)),
                -256.0F
        );

        scene.idle(25);

        scene.overlay()
                .showText(105)
                .text(
                        "Radiant begins at 1300°C, producing 256 RPM and enabling Combustion recipes"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(115);

        scene.overlay()
                .showText(90)
                .text(
                        "The front gauge tracks chamber temperature as the furnace crosses each heat tier"
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

        scene.overlay()
                .showText(105)
                .text(
                        "Engineer's Goggles show temperature, heat state, stress capacity, generated speed, fuel and cooldown time"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(115);

        scene.overlay()
                .showText(145)
                .text(
                        "When fuel runs out, stored heat drains away and output slows through lower tiers before stopping"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(rightShaftPos))
                .placeNearTarget();

        scene.idle(35);

        scene.world().setKineticSpeed(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(furnacePos))
                        .add(util.select().position(rightShaftPos)),
                -128.0F
        );
        scene.idle(25);

        scene.world().setKineticSpeed(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(furnacePos))
                        .add(util.select().position(rightShaftPos)),
                -64.0F
        );
        scene.idle(25);

        scene.world().setKineticSpeed(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(furnacePos))
                        .add(util.select().position(rightShaftPos)),
                -32.0F
        );
        scene.idle(25);

        scene.world().setKineticSpeed(
                util.select()
                        .position(leftShaftPos)
                        .add(util.select().position(furnacePos))
                        .add(util.select().position(rightShaftPos)),
                0.0F
        );

        scene.world().modifyBlockEntity(
                furnacePos,
                MoltenRotorBlockEntity.class,
                furnace -> furnace.setCreativeMode(false)
        );

        scene.world().modifyBlock(
                furnacePos,
                state -> state.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        HeatLevel.NONE
                ),
                false
        );

        scene.idle(55);
        scene.markAsFinished();
    }
}
