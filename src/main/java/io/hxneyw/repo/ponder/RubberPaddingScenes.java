package io.hxneyw.repo.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class RubberPaddingScenes {

    private RubberPaddingScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "rubber_padding.operation",
                "Bouncing and Handling with Rubber Padding"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9F);
        scene.setSceneOffsetY(-0.35F);

        BlockPos paddingPos = util.grid().at(2, 1, 2);
        BlockPos armPos = util.grid().at(1, 1, 2);
        BlockPos itemStartPos = util.grid().at(2, 4, 2);

        scene.world().setBlock(
                paddingPos,
                AllModBlocks.RUBBER_PADDING.get().defaultBlockState(),
                false
        );

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(
                util.select().position(paddingPos),
                Direction.DOWN
        );
        scene.idle(20);

        scene.overlay().showText(120)
                .text("Rubber Padding absorbs fall damage and bounces entities that land on it while bouncing is enabled")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(paddingPos))
                .placeNearTarget();
        scene.idle(130);

        scene.world().createItemEntity(
                util.vector().centerOf(itemStartPos),
                new Vec3(0.11, -0.55, 0.0),
                new ItemStack(Items.COPPER_INGOT, 8)
        );
        scene.idle(45);

        scene.overlay().showText(130)
                .text("Dropped item stacks use a damped bounce, retain most horizontal motion, and settle after repeated impacts")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(paddingPos))
                .placeNearTarget();
        scene.idle(140);

        scene.world().modifyEntities(
                ItemEntity.class,
                item -> item.setDeltaMovement(0.08, 0.28, 0.0)
        );
        scene.idle(35);

        scene.overlay().showText(130)
                .text("Living entities use their own bounce multiplier; suppressing bounce restores normal landing behavior instead")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().topOf(paddingPos))
                .placeNearTarget();
        scene.idle(140);

        scene.world().modifyEntities(
                ItemEntity.class,
                item -> item.discard()
        );
        scene.world().setBlock(
                armPos,
                AllBlocks.MECHANICAL_ARM.getDefaultState(),
                false
        );
        scene.world().showSection(
                util.select().position(armPos),
                Direction.EAST
        );
        scene.idle(25);

        scene.overlay().showControls(
                        util.vector().topOf(armPos).add(0.0, 0.45, 0.0),
                        Pointing.DOWN,
                        60
                )
                .withItem(new ItemStack(AllBlocks.MECHANICAL_ARM.get()));
        scene.overlay().showText(140)
                .text("Mechanical Arms treat the top of the Padding as one exposed item stack: they can deposit, merge matching items, and extract that stack")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(paddingPos))
                .placeNearTarget();
        scene.idle(150);

        scene.overlay().showText(120)
                .text("Rubber Padding, item bounce strength, and entity bounce strength can all be adjusted through configuration")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(paddingPos))
                .placeNearTarget();
        scene.idle(130);
        scene.markAsFinished();
    }
}
