package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;

public final class SulfuricAcidScenes {

    private SulfuricAcidScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "sulfuric_acid.operation",
                "Handling Sulfuric Acid"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9F);
        scene.setSceneOffsetY(-0.35F);

        BlockPos acidPos = util.grid().at(2, 1, 2);
        BlockPos reactionPos = util.grid().at(3, 1, 2);

        scene.showBasePlate();
        scene.idle(10);

        scene.overlay().showControls(
                        util.vector().centerOf(acidPos),
                        Pointing.DOWN,
                        50
                )
                .rightClick()
                .withItem(new ItemStack(Items.SULFURIC_ACID_BUCKET.get()));
        scene.world().setBlock(
                acidPos,
                AllModBlocks.SULFURIC_ACID_BLOCK.get().defaultBlockState(),
                false
        );
        scene.world().showSection(
                util.select().position(acidPos),
                Direction.DOWN
        );
        scene.idle(20);

        scene.overlay().showText(105)
                .text("Placed Sulfuric Acid applies Acid Burn to living entities that contact it")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(acidPos))
                .placeNearTarget();
        scene.idle(115);

        scene.overlay().showText(100)
                .text("Acid Burn refreshes to 100 ticks, or five seconds, while the entity remains exposed")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(acidPos).add(0.0, 0.8, 0.0))
                .placeNearTarget();
        scene.idle(110);
        scene.world().setBlock(
                reactionPos,
                Blocks.WATER.defaultBlockState(),
                false
        );
        scene.world().showSection(
                util.select().position(reactionPos),
                Direction.WEST
        );
        scene.effects().emitParticles(
                util.vector().centerOf(acidPos).add(0.45, 0.2, 0.0),
                scene.effects().simpleParticleEmitter(
                        ParticleTypes.LARGE_SMOKE,
                        Vec3.ZERO
                ),
                0.8F,
                8
        );
        scene.overlay().showText(105)
                .text("Contact with Water produces a visible steam and smoke reaction without consuming the Water block")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(reactionPos))
                .placeNearTarget();
        scene.idle(115);

        scene.world().hideSection(
                util.select().position(reactionPos),
                Direction.EAST
        );
        scene.idle(15);
        scene.world().setBlock(
                reactionPos,
                Blocks.LAVA.defaultBlockState(),
                false
        );
        scene.world().showSection(
                util.select().position(reactionPos),
                Direction.WEST
        );
        scene.idle(20);
        scene.effects().emitParticles(
                util.vector().centerOf(reactionPos),
                scene.effects().simpleParticleEmitter(
                        ParticleTypes.LARGE_SMOKE,
                        Vec3.ZERO
                ),
                0.9F,
                10
        );
        scene.world().setBlock(
                reactionPos,
                Blocks.OBSIDIAN.defaultBlockState(),
                false
        );
        scene.effects().indicateSuccess(reactionPos);
        scene.overlay().showText(105)
                .text("Source Lava touching Sulfuric Acid is converted into Obsidian")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(reactionPos))
                .placeNearTarget();
        scene.idle(115);

        scene.world().hideSection(
                util.select().position(reactionPos),
                Direction.EAST
        );
        scene.idle(15);
        scene.world().setBlock(
                reactionPos,
                Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 1),
                false
        );
        scene.world().showSection(
                util.select().position(reactionPos),
                Direction.WEST
        );
        scene.idle(20);
        scene.world().setBlock(
                reactionPos,
                Blocks.STONE.defaultBlockState(),
                false
        );
        scene.effects().indicateSuccess(reactionPos);
        scene.overlay().showText(100)
                .text("Flowing Lava is converted into Stone instead")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(reactionPos))
                .placeNearTarget();
        scene.idle(110);
        scene.markAsFinished();
    }
}
