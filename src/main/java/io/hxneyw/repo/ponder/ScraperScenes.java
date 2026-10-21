package io.hxneyw.repo.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.element.ElementLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

/** Walkthrough of how the Scraper handles Caddy cargo at the end of a belt. */
public final class ScraperScenes {
    private ScraperScenes() {}

    public static void operation(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("scraper.operation", "Separating Cargo with the Scraper");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(1.1F);
        scene.setSceneOffsetY(-0.15F);

        BlockPos beltStart = util.grid().at(0, 1, 2);
        BlockPos beltEnd = util.grid().at(4, 1, 2);
        BlockPos scraperPos = util.grid().at(2, 2, 2);
        BlockPos storagePos = util.grid().at(2, 2, 3);
        var belt = util.select().fromTo(beltStart, beltEnd);
        var scraper = util.select().position(scraperPos);
        var storage = util.select().position(storagePos);
        ItemStack loadedChest = BlockCaddyItem.createStack(new ItemStack(Blocks.CHEST));
        ItemStack loadedStone = BlockCaddyItem.createStack(new ItemStack(Blocks.STONE));
        ItemStack emptyCaddy = BlockCaddyItem.createStack(ItemStack.EMPTY);

        scene.showBasePlate();
        scene.idle(8);
        scene.world().setBlocks(belt, AllBlocks.BELT.get().defaultBlockState(), false);
        scene.world().setBlock(scraperPos, AllModBlocks.SCRAPER.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.SOUTH), true);
        scene.world().setBlock(storagePos, Blocks.CHEST.defaultBlockState(), true);
        scene.world().showSection(util.select().fromTo(beltStart, beltEnd), Direction.DOWN);
        scene.world().showSection(scraper, Direction.DOWN);
        scene.world().showSection(storage, Direction.SOUTH);
        scene.world().setKineticSpeed(belt, -24.0F);
        scene.idle(18);

        scene.overlay().showText(75)
                .text("Place the Scraper at the belt end, facing compatible storage.")
                .attachKeyFrame().colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(scraperPos)).placeNearTarget();
        scene.idle(80);

        ElementLink<BeltItemElement> chestCaddy = scene.world().createItemOnBelt(
                beltStart, Direction.DOWN, loadedChest);
        scene.idle(38);
        scene.world().stallBeltItem(chestCaddy, true);
        scene.overlay().showText(75)
                .text("Storage-block Caddies enter connected storage with their cargo intact.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(scraperPos)).placeNearTarget();
        scene.idle(80);
        scene.world().stallBeltItem(chestCaddy, false);
        scene.idle(35);

        ElementLink<BeltItemElement> stoneCaddy = scene.world().createItemOnBelt(
                beltStart, Direction.DOWN, loadedStone);
        scene.idle(38);
        scene.world().stallBeltItem(stoneCaddy, true);
        scene.overlay().showText(75)
                .text("Ordinary block cargo is extracted; the emptied Caddy is returned separately.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(scraperPos)).placeNearTarget();
        scene.idle(80);
        scene.world().changeBeltItemTo(stoneCaddy, emptyCaddy);
        scene.effects().indicateSuccess(scraperPos);
        scene.world().stallBeltItem(stoneCaddy, false);
        scene.idle(35);
    }
}
