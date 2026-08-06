package io.hxneyw.repo.ponder;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
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
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class CombustionBeltScenes {

    private static final int BELT_LENGTH = 5;
    private static final float BELT_SPEED = -24.0F;

    private CombustionBeltScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "combustion_belt.operation",
                "Heating Items on a Combustion Belt"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.85F);
        scene.setSceneOffsetY(-0.5F);

        BlockPos beltStart = util.grid().at(0, 1, 2);
        BlockPos beltCenter = util.grid().at(2, 1, 2);
        BlockPos beltEnd = util.grid().at(4, 1, 2);
        BlockPos conduitPos = util.grid().at(0, 1, 3);
        BlockPos rotorPos = util.grid().at(0, 1, 4);

        Selection beltSelection =
                util.select().fromTo(beltStart, beltEnd);
        Selection sourceSelection =
                util.select().fromTo(conduitPos, rotorPos);

        Vec3 beltStartTop = util.vector().topOf(beltStart);
        Vec3 beltCenterTop = util.vector().topOf(beltCenter);
        Vec3 beltEndTop = util.vector().topOf(beltEnd);
        Vec3 conduitCenter = util.vector().centerOf(conduitPos);
        Vec3 rotorCenter = util.vector().centerOf(rotorPos);

        ItemStack connector =
                new ItemStack(
                        Items.COMBUSTION_BELT_CONNECTOR.get()
                );
        ItemStack sand =
                new ItemStack(
                        net.minecraft.world.item.Items.SAND
                );
        ItemStack glass =
                new ItemStack(
                        net.minecraft.world.item.Items.GLASS
                );

        BlockState superheatedRotor =
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                        .defaultBlockState()
                        .setValue(
                                MoltenRotorBlock.FACING,
                                Direction.EAST
                        )
                        .setValue(
                                MoltenRotorBlock.HEAT_LEVEL,
                                HeatLevel.SEETHING
                        );

        BlockState heatedRotor =
                superheatedRotor.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        HeatLevel.KINDLED
                );

        BlockState conduitAlongZ =
                AllModBlocks.THERMOCHEMICAL_CONDUIT.get()
                        .defaultBlockState()
                        .setValue(
                                RotatedPillarBlock.AXIS,
                                Direction.Axis.Z
                        );

        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(
                beltSelection,
                Direction.DOWN
        );
        scene.world().setKineticSpeed(
                beltSelection,
                BELT_SPEED
        );
        scene.idle(25);

        scene.overlay()
                .showControls(
                        beltStartTop.add(0.0, 0.35, 0.0),
                        Pointing.DOWN,
                        45
                )
                .rightClick()
                .withItem(connector);

        scene.idle(20);

        scene.overlay()
                .showControls(
                        beltEndTop.add(0.0, 0.35, 0.0),
                        Pointing.DOWN,
                        45
                )
                .rightClick()
                .withItem(connector);

        scene.overlay()
                .showText(125)
                .text(
                        "Create a Combustion Belt between aligned Thermochemical Shafts using the Combustion Belt Connector"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(beltStart))
                .placeNearTarget();
        scene.idle(135);

        scene.overlay()
                .showControls(
                        beltStartTop.add(0.0, 0.35, 0.0),
                        Pointing.DOWN,
                        45
                )
                .rightClick()
                .withItem(sand);

        scene.world().createItemOnBelt(
                beltStart,
                Direction.DOWN,
                sand.copy()
        );

        scene.overlay()
                .showText(110)
                .text(
                        "Marked Combustion Belts transport items continuously like ordinary Create belts"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(beltCenterTop)
                .placeNearTarget();
        scene.idle(120);

        scene.overlay()
                .showText(130)
                .text(
                        "Heat must enter through a genuine physical connection to one of the Belt's pulley shafts"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(beltStart))
                .placeNearTarget();
        scene.idle(140);

        scene.world().setBlock(
                rotorPos,
                superheatedRotor,
                false
        );
        scene.world().setBlock(
                conduitPos,
                conduitAlongZ,
                false
        );

        scene.world().showSection(
                util.select().position(rotorPos),
                Direction.DOWN
        );
        scene.world().showSection(
                util.select().position(conduitPos),
                Direction.SOUTH
        );

        scene.world().setKineticSpeed(
                sourceSelection,
                BELT_SPEED
        );
        scene.idle(25);

        setBeltHeat(
                scene,
                beltSelection,
                "seething",
                conduitPos,
                true
        );

        for (int index = 0; index < BELT_LENGTH; index++) {
            scene.effects().indicateSuccess(
                    beltStart.east(index)
            );
            scene.idle(4);
        }

        scene.overlay()
                .showText(135)
                .text(
                        "This heated Molten Rotor and Conduit supply Superheated heat directly into the starting pulley"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(145);

        scene.overlay()
                .showText(120)
                .text(
                        "The selected live heat tier is shared across every marked segment in this Belt chain"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(beltCenterTop)
                .placeNearTarget();
        scene.idle(130);

        clearBeltItems(scene, beltStart);
        scene.world().createItemOnBelt(
                beltStart,
                Direction.DOWN,
                sand.copy()
        );

        scene.overlay()
                .showText(145)
                .text(
                        "A recipe advances only while its item is moving, sufficient heat is present, and new qualifying segments are crossed"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(beltCenterTop)
                .placeNearTarget();
        scene.idle(155);

        scene.overlay()
                .showText(130)
                .text(
                        "This five-segment scene is condensed; real recipes still require their configured distance and processing time"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(beltCenterTop)
                .placeNearTarget();
        scene.idle(140);
        scene.overlay()
                .showText(155)
                .text(
                        "A Rotation Speed Controller is the best way to tune processing: slower Belts give items more heated time, while faster Belts increase throughput only when the recipe still meets both its distance and time requirements"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(beltCenterTop)
                .placeNearTarget();

        scene.idle(165);

        clearBeltItems(scene, beltStart);
        scene.world().createItemOnBelt(
                beltStart,
                Direction.DOWN,
                sand.copy()
        );
        scene.idle(20);

        scene.world().modifyBlock(
                rotorPos,
                state -> state.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        HeatLevel.NONE
                ),
                false
        );
        setBeltHeat(
                scene,
                beltSelection,
                "none",
                null,
                false
        );

        scene.overlay()
                .showText(130)
                .text(
                        "Losing the heat source does not stop transport, but processing immediately stops advancing"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(rotorCenter)
                .placeNearTarget();
        scene.idle(140);

        scene.world().modifyBlock(
                rotorPos,
                state -> state.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        HeatLevel.SEETHING
                ),
                false
        );
        setBeltHeat(
                scene,
                beltSelection,
                "seething",
                conduitPos,
                true
        );

        for (int index = 0; index < BELT_LENGTH; index++) {
            scene.effects().indicateSuccess(
                    beltStart.east(index)
            );
            scene.idle(3);
        }

        scene.overlay()
                .showText(125)
                .text(
                        "Restore sufficient heat before retained progress decays to continue the same process"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(conduitCenter)
                .placeNearTarget();
        scene.idle(135);


        clearBeltItems(scene, beltStart);
        scene.world().createItemOnBelt(
                beltCenter,
                Direction.DOWN,
                sand.copy()
        );

        scene.overlay()
                .showText(140)
                .text(
                        "When both distance and time requirements are met, the moving input converts into the recipe output"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(beltCenterTop)
                .placeNearTarget();


        scene.idle(8);
        clearBeltItems(scene, beltStart);
        scene.world().createItemOnBelt(
                beltCenter,
                Direction.DOWN,
                glass.copy()
        );
        scene.effects().indicateSuccess(beltCenter);
        scene.idle(135);


        scene.world().modifyBlock(
                rotorPos,
                state -> heatedRotor,
                false
        );
        setBeltHeat(
                scene,
                beltSelection,
                "kindled",
                conduitPos,
                true
        );

        clearBeltItems(scene, beltStart);
        scene.world().createItemOnBelt(
                beltStart,
                Direction.DOWN,
                sand.copy()
        );

        scene.overlay()
                .showText(130)
                .text(
                        "Heat below a recipe's minimum tier carries the input through unchanged"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(beltCenterTop)
                .placeNearTarget();
        scene.idle(140);

        scene.world().modifyBlock(
                rotorPos,
                state -> superheatedRotor,
                false
        );
        setBeltHeat(
                scene,
                beltSelection,
                "seething",
                conduitPos,
                true
        );

        scene.overlay()
                .showText(140)
                .text(
                        "A directly adjacent heated Combustion Belt pulley can relay heat into another marked pulley, but heat never crosses a Belt body from one end to the other"
                )
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(beltEnd))
                .placeNearTarget();
        scene.idle(150);

        scene.overlay()
                .showText(135)
                .text(
                        "Reactive Heat follows one physical chain: generate, transmit, renew, route, then process"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(beltCenterTop)
                .placeNearTarget();

        scene.idle(145);

        scene.markAsFinished();
    }

    private static void setBeltHeat(
            CreateSceneBuilder scene,
            Selection beltSelection,
            String serializedTier,
            BlockPos sourcePosition,
            boolean fromConduit
    ) {
        scene.world().modifyBlockEntityNBT(
                beltSelection,
                BeltBlockEntity.class,
                nbt -> {
                    nbt.putString(
                            "CombustionBeltHeatTier",
                            serializedTier
                    );
                    nbt.putBoolean(
                            "CombustionBeltHeatFromConduit",
                            fromConduit
                    );

                    if (sourcePosition == null) {
                        nbt.remove(
                                "CombustionBeltHeatSourcePos"
                        );
                    } else {
                        nbt.putLong(
                                "CombustionBeltHeatSourcePos",
                                sourcePosition.asLong()
                        );
                    }
                }
        );
    }

    private static void clearBeltItems(
            CreateSceneBuilder scene,
            BlockPos beltStart
    ) {
        for (int index = 0; index < BELT_LENGTH; index++) {
            scene.world().removeItemsFromBelt(
                    beltStart.east(index)
            );
        }
    }
}
