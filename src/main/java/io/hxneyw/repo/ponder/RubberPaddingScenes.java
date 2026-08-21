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
import net.minecraft.world.item.ItemStack;

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

        scene.overlay().showText(90)
                .text("Rubber Padding prevents fall damage and bounces entities that land on it")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(paddingPos))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(95)
                .text("Living entities bounce from their incoming fall speed; suppressing bounce restores a normal landing")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().topOf(paddingPos))
                .placeNearTarget();
        scene.idle(105);

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
                        40
                )
                .withItem(new ItemStack(AllBlocks.MECHANICAL_ARM.get()));
        scene.idle(50);

        scene.overlay().showText(105)
                .text("Mechanical Arms can deposit, merge, and extract the exposed item stack on top")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(paddingPos))
                .placeNearTarget();
        scene.idle(115);

        scene.overlay().showText(90)
                .text("Padding behavior, item bounce strength, and entity bounce strength are configurable")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(paddingPos))
                .placeNearTarget();
        scene.idle(100);
        scene.markAsFinished();
    }
}
