package io.hxneyw.repo.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
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

public final class ThermochemicalShaftScenes {

    private ThermochemicalShaftScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "thermochemical_shaft.operation",
                "Routing Rotation and Thermochemical Heat"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9F);
        scene.setSceneOffsetY(-0.5F);

        BlockPos sourcePos = util.grid().at(1, 1, 2);
        BlockPos shaftPos = util.grid().at(2, 1, 2);
        Selection sourceSelection =
                util.select().position(sourcePos);
        Selection shaftSelection =
                util.select().position(shaftPos);
        Vec3 shaftCenter = util.vector().centerOf(shaftPos);
        Vec3 shaftTop = util.vector().topOf(shaftPos);

        BlockState furnaceState =
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                        .defaultBlockState()
                        .setValue(MoltenRotorBlock.FACING, Direction.NORTH)
                        .setValue(MoltenRotorBlock.HEAT_LEVEL, HeatLevel.KINDLED);
        BlockState shaftState =
                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        .defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);

        scene.showBasePlate();
        scene.idle(10);

        scene.world().setBlock(sourcePos, furnaceState, false);
        scene.world().setBlock(shaftPos, shaftState, false);
        scene.world().showIndependentSection(
                sourceSelection,
                Direction.DOWN
        );
        scene.world().showSection(
                shaftSelection,
                Direction.WEST
        );
        scene.world().setKineticSpeed(
                sourceSelection.add(shaftSelection),
                -64.0F
        );
        scene.idle(20);

        scene.overlay()
                .showText(100)
                .text(
                        "Thermochemical Shafts transmit ordinary kinetic rotation just like Create Shafts"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(110);

        scene.overlay()
                .showText(110)
                .text(
                        "They also carry thermochemical heat through a real, physically connected network"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(120);

        scene.overlay()
                .showText(120)
                .text(
                        "Ordinary Shafts cannot replace them inside a thermochemical route"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(130);

        scene.overlay()
                .showControls(
                        shaftTop.add(0.0, 0.35, 0.0),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .withItem(new ItemStack(AllItems.WRENCH.get()));

        scene.overlay()
                .showText(100)
                .text(
                        "Use a Wrench to rotate the Shaft between the X, Y, and Z axes, even while it is connected"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(110);

        scene.overlay()
                .showControls(
                        shaftTop.add(0.0, 0.35, 0.0),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .withItem(
                        new ItemStack(
                                AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                        )
                );

        scene.overlay()
                .showText(115)
                .text(
                        "Extend the line with more Thermochemical Shafts to carry rotation and heat together"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(125);

        scene.overlay()
                .showText(120)
                .text(
                        "From a Molten Rotor, the first Conduit must be reached within three Thermochemical Shafts"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(130);

        scene.overlay()
                .showText(135)
                .text(
                        "A Thermochemical Conduit renews the downstream shaft allowance"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(145);

        scene.overlay()
                .showText(120)
                .text(
                        "After a Conduit, up to ten transmission segments may continue before another renewal is needed"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(130);

        scene.overlay()
                .showText(145)
                .text(
                        "A Thermochemical Gearbox can turn or branch the route, but it does not renew the allowance"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(155);

        scene.overlay()
                .showText(130)
                .text(
                        "Breaking any required physical connection immediately interrupts thermochemical transmission"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(shaftCenter)
                .placeNearTarget();
        scene.idle(140);
        scene.overlay()
                .showText(120)
                .text(
                        "Reach a Thermochemical Conduit before the source-side transmission allowance is exhausted"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(shaftCenter)
                .placeNearTarget();

        scene.idle(130);
        scene.markAsFinished();
    }
}