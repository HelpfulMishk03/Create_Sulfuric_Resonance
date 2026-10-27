package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.element.ElementLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.NotNull;

/** Walkthrough of how the Scraper handles Caddy cargo at the end of a belt. */
public final class ScraperScenes {
    private ScraperScenes() {}

    public static void operation(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("scraper.operation", "Scraper: Separate Cargo at the Belt End");
        scene.configureBasePlate(0, 0, 5);

        BlockPos beltStart = util.grid().at(0, 1, 2);
        BlockPos beltEnd = util.grid().at(4, 1, 2);
        BlockPos scraperPos = util.grid().at(2, 2, 2);
        BlockPos storagePos = util.grid().at(2, 2, 3);
        var displayExamples = util.select().fromTo(util.grid().at(0, 1, 0), util.grid().at(4, 1, 0));
        var belt = util.select().fromTo(beltStart, beltEnd);
        var scraper = util.select().position(scraperPos);
        var storage = util.select().position(storagePos);
        ItemStack loadedChest = BlockCaddyItem.createStack(chestWithContents());
        ItemStack loadedStone = BlockCaddyItem.createStack(new ItemStack(Blocks.STONE));
        ItemStack emptyCaddy = BlockCaddyItem.createStack(ItemStack.EMPTY);

        scene.showBasePlate();
        scene.idle(8);

        scene.idle(4);
        // The structure owns the checkerboard, two cargo-filled Caddies, cargo
        // samples, belt block entities, Scraper, and output chest. Reveal these
        // saved blocks directly so setup cannot replace them or emit break dust.
        scene.world().showSection(displayExamples, Direction.UP);
        scene.idle(12);
        for (int x = 0; x <= 4; x++) {
            scene.world().showSection(util.select().position(util.grid().at(x, 1, 2)), Direction.UP);
            scene.idle(3);
        }
        scene.world().showSection(scraper, Direction.UP);
        scene.world().showSection(storage, Direction.SOUTH);
        scene.world().setKineticSpeed(belt, -16.0F);
        scene.idle(15);

        scene.overlay().showText(75)
                .text("The belt carries each Caddy to the Scraper scoop directly above its segment.")
                .attachKeyFrame().colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(scraperPos)).placeNearTarget();
        scene.idle(80);

        ElementLink<BeltItemElement> chestCaddy = scene.world().createItemOnBelt(
                beltStart, Direction.DOWN, loadedChest);
        scene.idle(60);
        scene.world().stallBeltItem(chestCaddy, true);
        scene.overlay().showText(75)
                .text("Storage-block Caddies enter connected storage with their cargo intact.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(scraperPos)).placeNearTarget();
        scene.idle(78);
        scene.world().removeItemsFromBelt(util.grid().at(2, 1, 2));
        scene.world().modifyBlockEntity(scraperPos,
                io.hxneyw.repo.content.blocks.ScraperBlockEntity.class,
                io.hxneyw.repo.content.blocks.ScraperBlockEntity::openFlaps);
        scene.world().modifyBlockEntity(storagePos, ChestBlockEntity.class,
                chest -> chest.setItem(0, chestWithContents()));
        scene.idle(8);

        ElementLink<BeltItemElement> stoneCaddy = scene.world().createItemOnBelt(
                beltStart, Direction.DOWN, loadedStone);
        scene.idle(60);
        scene.world().stallBeltItem(stoneCaddy, true);
        scene.overlay().showText(75)
                .text("Both the extracted block and its empty Caddy enter the connected chest.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(scraperPos)).placeNearTarget();
        scene.idle(75);
        scene.world().modifyBlockEntity(scraperPos,
                io.hxneyw.repo.content.blocks.ScraperBlockEntity.class,
                io.hxneyw.repo.content.blocks.ScraperBlockEntity::openFlaps);
        scene.world().removeItemsFromBelt(util.grid().at(2, 1, 2));
        scene.world().modifyBlockEntity(storagePos, ChestBlockEntity.class, chest -> {
            chest.setItem(1, new ItemStack(Blocks.STONE));
            chest.setItem(2, emptyCaddy.copy());
        });
        scene.effects().indicateSuccess(scraperPos);
        scene.idle(80);
    }

    public static void ignoresOrdinaryItems(@NotNull SceneBuilder builder,
                                            @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("scraper.non_caddy", "Scraper: Ordinary Items Pass Through");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(8);

        BlockPos beltStart = util.grid().at(0, 1, 2);
        BlockPos scraperPos = util.grid().at(2, 2, 2);
        var belt = util.select().fromTo(beltStart, util.grid().at(4, 1, 2));
        var scraper = util.select().position(scraperPos);
        var storage = util.select().position(util.grid().at(2, 2, 3));
        var examples = util.select().fromTo(util.grid().at(0, 1, 0), util.grid().at(4, 1, 0));
        scene.world().showSection(examples, Direction.UP);
        scene.idle(5);
        scene.world().showSection(belt, Direction.DOWN);
        scene.world().showSection(scraper, Direction.UP);
        scene.world().showSection(storage, Direction.SOUTH);
        scene.world().setKineticSpeed(belt, -16.0F);
        scene.idle(12);

        scene.overlay().showText(85)
                .text("The Scraper only collects block cargo carried in a Caddy.")
                .attachKeyFrame().colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(scraperPos)).placeNearTarget();
        scene.idle(90);

        scene.world().createItemOnBelt(
                beltStart, Direction.DOWN, new ItemStack(Items.COBBLESTONE));
        scene.overlay().showText(85)
                .text("Loose items and other non-Caddy belt contents are intentionally ignored and pass through.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(scraperPos)).placeNearTarget();
        scene.idle(100);
        scene.effects().indicateSuccess(scraperPos);
        scene.idle(75);
    }

    private static ItemStack chestWithContents() {
        CompoundTag chestData = new CompoundTag();
        chestData.putString("id", "minecraft:chest");
        ListTag items = new ListTag();
        CompoundTag diamond = new CompoundTag();
        diamond.putByte("Slot", (byte) 0);
        diamond.putString("id", "minecraft:diamond");
        diamond.putInt("count", 4);
        items.add(diamond);
        chestData.put("Items", items);
        ItemStack chest = new ItemStack(Blocks.CHEST);
        chest.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(chestData));
        return chest;
    }
}
