package io.hxneyw.repo.ponder;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.thermochemicalgearbox.ThermochemicalGearboxBlockEntity;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ThermochemicalGearboxScenes {

    private static final float SPEED = 32.0F;
    private static final Direction GEARBOX_SOURCE_FACE = Direction.WEST;

    private ThermochemicalGearboxScenes() {
    }

    @SuppressWarnings("DuplicatedCode")
    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "thermochemical_gearbox.operation",
                "Turning and Branching Thermochemical Routes"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.82F);
        scene.setSceneOffsetY(-0.7F);

        BlockPos gearboxPos = util.grid().at(2, 2, 2);
        BlockPos rotorPos = util.grid().at(0, 2, 2);
        BlockPos westInputPos = util.grid().at(1, 2, 2);
        BlockPos eastShaftPos = util.grid().at(3, 2, 2);
        BlockPos eastConduitPos = util.grid().at(4, 2, 2);
        BlockPos northShaftPos = util.grid().at(2, 2, 1);
        BlockPos northExtraPos = util.grid().at(2, 2, 0);
        BlockPos southShaftPos = util.grid().at(2, 2, 3);
        BlockPos upShaftPos = util.grid().at(2, 3, 2);
        BlockPos upExtraPos = util.grid().at(2, 4, 2);
        BlockPos downShaftPos = util.grid().at(2, 1, 2);

        Selection gearboxSelection =
                util.select().position(gearboxPos);
        Vec3 gearboxCenter =
                util.vector().centerOf(gearboxPos);
        Vec3 eastConduitCenter =
                util.vector().centerOf(eastConduitPos);
        Vec3 northExtraCenter =
                util.vector().centerOf(northExtraPos);
        Vec3 westInputCenter =
                util.vector().centerOf(westInputPos);
        Vec3 southBranchCenter =
                util.vector().centerOf(southShaftPos);

        ItemStack shaftItem =
                new ItemStack(
                        AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                );
        ItemStack conduitItem =
                new ItemStack(
                        AllModBlocks.THERMOCHEMICAL_CONDUIT.get()
                );

        BlockState shaftX =
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(
                                RotatedPillarBlock.AXIS,
                                Direction.Axis.X
                        );

        BlockState shaftY =
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(
                                RotatedPillarBlock.AXIS,
                                Direction.Axis.Y
                        );

        BlockState shaftZ =
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(
                                RotatedPillarBlock.AXIS,
                                Direction.Axis.Z
                        );

        BlockState conduitX =
                AllModBlocks.THERMOCHEMICAL_CONDUIT.get()
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
                gearboxSelection,
                Direction.DOWN
        );
        scene.idle(25);

        scene.overlay()
                .showText(120)
                .text(
                        "Thermochemical Gearboxes expose real rotational and thermochemical connections on all six faces"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(gearboxCenter)
                .placeNearTarget();
        scene.idle(130);

        scene.world().setBlock(
                rotorPos,
                heatedRotor,
                false
        );
        scene.world().setBlock(
                westInputPos,
                shaftX,
                false
        );
        scene.world().showSection(
                util.select().position(rotorPos),
                Direction.DOWN
        );
        scene.world().showSection(
                util.select().position(westInputPos),
                Direction.WEST
        );
        setKineticState(
                scene,
                util,
                rotorPos,
                speedForFace(Direction.WEST),
                null
        );
        setKineticState(
                scene,
                util,
                westInputPos,
                speedForFace(Direction.WEST),
                rotorPos
        );
        configureGearbox(
                scene,
                gearboxSelection,
                gearboxPos,
                westInputPos,
                rotorPos,
                true
        );
        scene.idle(25);

        scene.overlay()
                .showText(125)
                .text(
                        "A heated Molten Rotor can enter one face through a physically aligned Thermochemical Shaft"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(westInputCenter)
                .placeNearTarget();
        scene.idle(135);

        showPlacement(
                scene,
                util,
                eastShaftPos,
                shaftItem,
                shaftX,
                Direction.WEST,
                speedForFace(Direction.EAST),
                gearboxPos
        );

        showPlacement(
                scene,
                util,
                eastConduitPos,
                conduitItem,
                conduitX,
                Direction.WEST,
                speedForFace(Direction.EAST),
                eastShaftPos
        );

        scene.overlay()
                .showText(125)
                .text(
                        "The opposite face provides a straight-through output without requiring another block orientation"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(eastShaftPos))
                .placeNearTarget();
        scene.idle(135);

        scene.overlay()
                .showText(145)
                .text(
                        "This route reaches its first Conduit through one Shaft, the Gearbox, and one more Shaft: exactly three cost-one nodes"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(eastConduitCenter)
                .placeNearTarget();
        scene.idle(155);

        scene.effects().indicateSuccess(eastConduitPos);

        scene.overlay()
                .showText(120)
                .text(
                        "The Conduit then renews the downstream allowance to ten transmission segments"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(eastConduitCenter)
                .placeNearTarget();
        scene.idle(130);

        showPlacement(
                scene,
                util,
                northShaftPos,
                shaftItem,
                shaftZ,
                Direction.SOUTH,
                speedForFace(Direction.NORTH),
                gearboxPos
        );

        showPlacement(
                scene,
                util,
                southShaftPos,
                shaftItem,
                shaftZ,
                Direction.NORTH,
                speedForFace(Direction.SOUTH),
                gearboxPos
        );

        showExistingPlacement(
                scene,
                util,
                upShaftPos,
                shaftItem,
                speedForFace(Direction.UP),
                gearboxPos
        );

        showPlacement(
                scene,
                util,
                downShaftPos,
                shaftItem,
                shaftY,
                Direction.UP,
                speedForFace(Direction.DOWN),
                gearboxPos
        );

        scene.overlay()
                .showText(135)
                .text(
                        "With the west face used as input, all five remaining faces can operate as simultaneous output branches"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(gearboxCenter)
                .placeNearTarget();
        scene.idle(145);

        scene.effects().indicateSuccess(eastShaftPos);
        scene.effects().indicateSuccess(northShaftPos);
        scene.effects().indicateSuccess(southShaftPos);
        scene.effects().indicateSuccess(upShaftPos);
        scene.effects().indicateSuccess(downShaftPos);

        scene.overlay()
                .showText(125)
                .text(
                        "The same block can route straight ahead, turn ninety degrees, and send the network vertically"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(gearboxCenter)
                .placeNearTarget();
        scene.idle(135);

        scene.overlay()
                .showText(140)
                .text(
                        "Every branch receives the same heat tier and inherits the allowance remaining when the Gearbox was reached"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(gearboxCenter)
                .placeNearTarget();
        scene.idle(150);

        showPlacement(
                scene,
                util,
                northExtraPos,
                shaftItem,
                shaftZ,
                Direction.SOUTH,
                speedForFace(Direction.NORTH),
                northShaftPos
        );

        showPlacement(
                scene,
                util,
                upExtraPos,
                shaftItem,
                shaftY,
                Direction.DOWN,
                speedForFace(Direction.UP),
                upShaftPos
        );

        scene.overlay()
                .showText(150)
                .text(
                        "These farther Shafts still carry ordinary rotation, but they are beyond the source-side heat allowance until a Conduit is reached"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(northExtraCenter)
                .placeNearTarget();
        scene.idle(160);

        scene.world().setBlock(
                westInputPos,
                Blocks.AIR.defaultBlockState(),
                false
        );
        configureGearbox(
                scene,
                gearboxSelection,
                gearboxPos,
                westInputPos,
                rotorPos,
                false
        );
        setKineticState(scene, util, eastShaftPos, 0.0F, null);
        setKineticState(scene, util, eastConduitPos, 0.0F, null);
        setKineticState(scene, util, northShaftPos, 0.0F, null);
        setKineticState(scene, util, northExtraPos, 0.0F, null);
        setKineticState(scene, util, southShaftPos, 0.0F, null);
        setKineticState(scene, util, upShaftPos, 0.0F, null);
        setKineticState(scene, util, upExtraPos, 0.0F, null);
        setKineticState(scene, util, downShaftPos, 0.0F, null);
        scene.idle(25);

        scene.overlay()
                .showText(135)
                .text(
                        "Breaking the source-side connection stops the Gearbox and every branch beyond it"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(westInputCenter)
                .placeNearTarget();
        scene.idle(145);

        scene.world().setBlock(
                westInputPos,
                shaftX,
                false
        );
        setKineticState(
                scene,
                util,
                westInputPos,
                speedForFace(Direction.WEST),
                rotorPos
        );
        configureGearbox(
                scene,
                gearboxSelection,
                gearboxPos,
                westInputPos,
                rotorPos,
                true
        );
        setKineticState(
                scene,
                util,
                eastShaftPos,
                speedForFace(Direction.EAST),
                gearboxPos
        );
        setKineticState(
                scene,
                util,
                eastConduitPos,
                speedForFace(Direction.EAST),
                eastShaftPos
        );
        setKineticState(
                scene,
                util,
                northShaftPos,
                speedForFace(Direction.NORTH),
                gearboxPos
        );
        setKineticState(
                scene,
                util,
                northExtraPos,
                speedForFace(Direction.NORTH),
                northShaftPos
        );
        setKineticState(
                scene,
                util,
                southShaftPos,
                speedForFace(Direction.SOUTH),
                gearboxPos
        );
        setKineticState(
                scene,
                util,
                upShaftPos,
                speedForFace(Direction.UP),
                gearboxPos
        );
        setKineticState(
                scene,
                util,
                upExtraPos,
                speedForFace(Direction.UP),
                upShaftPos
        );
        setKineticState(
                scene,
                util,
                downShaftPos,
                speedForFace(Direction.DOWN),
                gearboxPos
        );
        scene.effects().indicateSuccess(westInputPos);
        scene.idle(25);

        scene.overlay()
                .showText(115)
                .text(
                        "Replace the missing Shaft and all physically connected branches resume immediately"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(westInputCenter)
                .placeNearTarget();
        scene.idle(125);

        scene.world().setBlock(
                southShaftPos,
                Blocks.AIR.defaultBlockState(),
                false
        );
        scene.idle(20);

        scene.overlay()
                .showText(130)
                .text(
                        "Breaking one output branch interrupts only that branch while the other connected faces continue operating"
                )
                .colored(PonderPalette.BLUE)
                .pointAt(southBranchCenter)
                .placeNearTarget();
        scene.idle(140);

        scene.world().setBlock(
                southShaftPos,
                shaftZ,
                false
        );
        setKineticState(
                scene,
                util,
                southShaftPos,
                speedForFace(Direction.SOUTH),
                gearboxPos
        );
        scene.effects().indicateSuccess(southShaftPos);
        scene.idle(25);

        scene.overlay()
                .showText(145)
                .text(
                        "A Gearbox costs one segment and routes heat without generating, multiplying, storing, or renewing it; only a Conduit renews allowance"
                )
                .colored(PonderPalette.MEDIUM)
                .pointAt(gearboxCenter)
                .placeNearTarget();
        scene.idle(155);
        scene.overlay()
                .showText(125)
                .text(
                        "Route a valid branch into a Combustion Belt pulley to turn the heat network into item processing"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(gearboxCenter)
                .placeNearTarget();

        scene.idle(135);
        scene.markAsFinished();
    }

    private static void showExistingPlacement(
            CreateSceneBuilder scene,
            SceneBuildingUtil util,
            BlockPos position,
            ItemStack item,
            float speed,
            BlockPos sourcePosition
    ) {
        scene.overlay()
                .showControls(
                        util.vector().topOf(position)
                                .add(0.0, 0.2, 0.0),
                        Pointing.DOWN,
                        25
                )
                .rightClick()
                .withItem(item);

        scene.idle(28);

        Selection selection =
                util.select().position(position);

        scene.world().showSection(
                selection,
                Direction.DOWN
        );
        scene.idle(3);

        setKineticState(
                scene,
                util,
                position,
                speed,
                sourcePosition
        );
        scene.idle(15);
    }

    private static void showPlacement(
            CreateSceneBuilder scene,
            SceneBuildingUtil util,
            BlockPos position,
            ItemStack item,
            BlockState state,
            Direction revealDirection,
            float speed,
            BlockPos sourcePosition
    ) {
        scene.overlay()
                .showControls(
                        util.vector().topOf(position)
                                .add(0.0, 0.2, 0.0),
                        Pointing.DOWN,
                        25
                )
                .rightClick()
                .withItem(item);

        scene.idle(28);

        scene.world().setBlock(
                position,
                state,
                false
        );
        scene.world().showSection(
                util.select().position(position),
                revealDirection
        );
        setKineticState(
                scene,
                util,
                position,
                speed,
                sourcePosition
        );
        scene.idle(18);
    }

    private static float speedForFace(Direction face) {
        return switch (face) {
            case WEST, NORTH, UP -> -SPEED;
            case EAST, SOUTH, DOWN -> SPEED;
        };
    }

    private static void setKineticState(
            CreateSceneBuilder scene,
            SceneBuildingUtil util,
            BlockPos position,
            float speed,
            BlockPos sourcePosition
    ) {
        Selection selection = util.select().position(position);

        scene.world().setKineticSpeed(
                selection,
                speed
        );

        scene.world().modifyBlockEntity(
                position,
                KineticBlockEntity.class,
                blockEntity -> {
                    blockEntity.source = sourcePosition;
                    blockEntity.setSpeed(speed);
                }
        );
    }

    private static void configureGearbox(
            CreateSceneBuilder scene,
            Selection selection,
            BlockPos gearboxPosition,
            BlockPos kineticSourcePosition,
            BlockPos heatSourcePosition,
            boolean active
    ) {
        float speed = active
                ? speedForFace(GEARBOX_SOURCE_FACE)
                : 0.0F;

        scene.world().setKineticSpeed(
                selection,
                speed
        );

        scene.world().modifyBlockEntity(
                gearboxPosition,
                ThermochemicalGearboxBlockEntity.class,
                gearbox -> {
                    gearbox.source = active
                            ? kineticSourcePosition
                            : null;
                    gearbox.setSpeed(speed);
                }
        );

        scene.world().modifyBlockEntityNBT(
                selection,
                ThermochemicalGearboxBlockEntity.class,
                nbt -> {
                    nbt.putFloat("Speed", speed);

                    if (!active) {
                        nbt.remove("Source");
                        nbt.putString("TransmittedHeatTier", "none");
                        nbt.remove("HeatSourcePos");
                        nbt.putInt("ThermochemicalPathLength", 0);
                        nbt.putInt("ThermochemicalSpanLimit", 0);
                        nbt.putInt(
                                "ThermochemicalRemainingAllowance",
                                0
                        );
                        nbt.putInt("ThermochemicalTemperature", 0);
                        return;
                    }

                    nbt.put(
                            "Source",
                            NbtUtils.writeBlockPos(
                                    kineticSourcePosition
                            )
                    );
                    nbt.putString(
                            "TransmittedHeatTier",
                            "seething"
                    );
                    nbt.putLong(
                            "HeatSourcePos",
                            heatSourcePosition.asLong()
                    );
                    nbt.putInt("ThermochemicalPathLength", 2);
                    nbt.putInt("ThermochemicalSpanLimit", 3);
                    nbt.putInt(
                            "ThermochemicalRemainingAllowance",
                            1
                    );
                    nbt.putInt("ThermochemicalTemperature", 1200);
                }
        );
    }
}