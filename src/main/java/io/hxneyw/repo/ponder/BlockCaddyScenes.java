package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import io.hxneyw.repo.content.blocks.BlockCaddyBlockEntity;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.element.ElementLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

/** Practical, source-accurate walkthroughs for the reusable Block Caddy. */
public final class BlockCaddyScenes {
    private BlockCaddyScenes() {}

    public static void handLoading(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("block_caddy.hand_loading", "Loading and Reusing a Block Caddy");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos emptyCaddyPos = util.grid().at(1, 1, 2);
        BlockPos caddyPos = util.grid().at(2, 1, 2);
        BlockPos blockExamplePos = util.grid().at(3, 1, 2);
        scene.world().setBlock(emptyCaddyPos, AllModBlocks.BLOCK_CADDY.get().defaultBlockState(), true);
        scene.world().setBlock(caddyPos, AllModBlocks.BLOCK_CADDY.get().defaultBlockState(), true);
        scene.world().setBlock(blockExamplePos, Blocks.OAK_PLANKS.defaultBlockState(), true);
        scene.world().showSection(util.select().fromTo(emptyCaddyPos, blockExamplePos), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("Empty Caddies carry one block and can be reused.")
                .attachKeyFrame().colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(caddyPos)).placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(75)
                .text("Right-click an empty Caddy with a placeable block to load it.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(caddyPos)).placeNearTarget();
        scene.idle(80);
        scene.world().modifyBlockEntity(caddyPos, BlockCaddyBlockEntity.class,
                caddy -> caddy.setCargo(new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS)));
        scene.idle(5);

        scene.overlay().showText(70)
                .text("The cargo stays inside the frame when the Caddy is moved.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(caddyPos)).placeNearTarget();
        scene.idle(75);

        scene.overlay().showText(70)
                .text("Sneak + right-click the Caddy item to unload and reuse it.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(caddyPos)).placeNearTarget();
        scene.idle(75);
    }

    public static void factoryLoading(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("block_caddy.factory_loading", "Loading Caddies on a Moving Belt");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(1.1F);
        scene.setSceneOffsetY(-0.2F);

        BlockPos beltStart = util.grid().at(0, 1, 2);
        BlockPos beltCenter = util.grid().at(2, 1, 2);
        BlockPos beltEnd = util.grid().at(4, 1, 2);
        BlockPos deployerPos = util.grid().at(2, 3, 2);
        var belt = util.select().fromTo(beltStart, beltEnd);
        var deployer = util.select().position(deployerPos);
        var drive = util.select().fromTo(2, 1, 3, 3, 3, 4);

        ItemStack emptyCaddy = BlockCaddyItem.createStack(ItemStack.EMPTY);
        ItemStack filledCaddy = BlockCaddyItem.createStack(
                new ItemStack(net.minecraft.world.item.Items.COBBLESTONE));
        ItemStack blockSupply = new ItemStack(net.minecraft.world.item.Items.COBBLESTONE);

        scene.showBasePlate();
        scene.idle(8);
        scene.world().showSection(drive, Direction.SOUTH);
        scene.world().setKineticSpeed(drive, -32.0F);
        scene.idle(10);
        scene.world().showSection(belt, Direction.DOWN);
        scene.world().setKineticSpeed(belt, -24.0F);
        scene.idle(10);
        scene.world().showSection(deployer, Direction.DOWN);
        scene.world().setKineticSpeed(deployer, -32.0F);
        scene.world().modifyBlockEntityNBT(deployer, DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", blockSupply.saveOptional(scene.world().getHolderLookupProvider())));
        scene.idle(18);

        scene.overlay().showText(75)
                .text("A Deployer loads empty Caddies with placeable blocks.")
                .attachKeyFrame().colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(beltCenter)).placeNearTarget();
        ElementLink<BeltItemElement> caddy = scene.world().createItemOnBelt(
                beltStart, Direction.DOWN, emptyCaddy);
        scene.idle(35);
        scene.world().stallBeltItem(caddy, true);
        scene.idle(8);
        scene.world().moveDeployer(deployerPos, 1.0F, 15);
        scene.idle(16);
        scene.world().changeBeltItemTo(caddy, filledCaddy);
        scene.effects().indicateSuccess(beltCenter);
        scene.idle(8);
        scene.world().moveDeployer(deployerPos, -1.0F, 15);
        scene.idle(16);

        scene.overlay().showText(75)
                .text("Each activation fills one Caddy. Filled Caddies pass through unchanged.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(beltCenter)).placeNearTarget();
        scene.world().stallBeltItem(caddy, false);
        scene.idle(80);
        scene.overlay().showText(70)
                .text("A Depot can also load a Caddy as a stationary workstation.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(beltEnd)).placeNearTarget();
        scene.idle(75);
    }

    public static void chestCargo(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("block_caddy.chest_cargo", "Moving Storage with Chest Caddies");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos chestExamplePos = util.grid().at(1, 1, 2);
        BlockPos caddyPos = util.grid().at(2, 1, 2);
        BlockPos barrelExamplePos = util.grid().at(3, 1, 2);
        scene.world().setBlock(chestExamplePos, Blocks.CHEST.defaultBlockState(), true);
        scene.world().setBlock(caddyPos, AllModBlocks.BLOCK_CADDY.get().defaultBlockState(), true);
        scene.world().setBlock(barrelExamplePos, Blocks.BARREL.defaultBlockState(), true);
        scene.world().showSection(util.select().fromTo(chestExamplePos, barrelExamplePos), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("Load a chest into the Caddy; its inventory travels with it.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(caddyPos)).placeNearTarget();
        scene.idle(75);
        scene.world().modifyBlockEntity(caddyPos, BlockCaddyBlockEntity.class,
                caddy -> caddy.setCargo(new ItemStack(Blocks.CHEST)));
        scene.idle(5);

        scene.overlay().showText(70)
                .text("Sneak + right-click a chest Caddy to open its inventory.")
                .attachKeyFrame().colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(caddyPos)).placeNearTarget();
        scene.idle(75);

        scene.overlay().showText(70)
                .text("The Scraper stores chest cargo intact in connected storage.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(caddyPos)).placeNearTarget();
        scene.idle(75);
    }
}
