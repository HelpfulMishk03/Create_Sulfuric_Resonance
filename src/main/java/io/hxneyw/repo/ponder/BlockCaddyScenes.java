package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import io.hxneyw.repo.content.blocks.BlockCaddyBlockEntity;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.content.registry.ModParticles;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
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
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.Map;
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
        ItemStack orientedLog = new ItemStack(Items.OAK_LOG);
        orientedLog.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(Map.of("axis", "x")));
        // Populate the cargo BE before revealing the scene so Ponder's first
        // rendered frame cannot show a HAS_CARGO shell with an empty BE.
        scene.world().modifyBlockEntityNBT(util.select().position(caddyPos), BlockCaddyBlockEntity.class,
                nbt -> nbt.put("Cargo", orientedLog.saveOptional(
                        scene.world().getHolderLookupProvider())));
        scene.world().showSection(util.select().position(emptyCaddyPos), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().position(caddyPos), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().position(blockExamplePos), Direction.UP);
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
        scene.idle(5);

        scene.overlay().showText(70)
                .text("The cargo stays inside the frame when the Caddy is moved.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(caddyPos)).placeNearTarget();
        scene.idle(130);

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
        ItemStack chestCargo = chestWithContents();
        scene.world().modifyBlockEntityNBT(util.select().position(caddyPos), BlockCaddyBlockEntity.class,
                nbt -> nbt.put("Cargo", chestCargo.saveOptional(
                        scene.world().getHolderLookupProvider())));
        scene.world().showSection(util.select().position(chestExamplePos), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().position(caddyPos), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().position(barrelExamplePos), Direction.UP);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("Load a chest into the Caddy; its inventory travels with it.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(caddyPos)).placeNearTarget();
        scene.idle(75);
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

    public static void precisionSpritzer(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("block_caddy.precision_spritzer", "Processing Caddy Cargo with a Precision Spritzer");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(1.1F);

        BlockPos beltStart = util.grid().at(0, 1, 2);
        BlockPos beltCenter = util.grid().at(2, 1, 2);
        BlockPos beltEnd = util.grid().at(4, 1, 2);
        BlockPos spritzerPos = util.grid().at(2, 3, 2);
        BlockPos displayCaddyPos = util.grid().at(0, 1, 0);
        BlockPos exposedCopperExample = util.grid().at(1, 1, 0);
        BlockPos copperExample = util.grid().at(4, 1, 0);
        var belt = util.select().fromTo(beltStart, beltEnd);
        var spritzer = util.select().position(spritzerPos);
        var displayCaddy = util.select().position(displayCaddyPos);
        ItemStack exposedCopperCaddy = BlockCaddyItem.createStack(new ItemStack(Blocks.EXPOSED_COPPER));
        ItemStack copperCaddy = BlockCaddyItem.createStack(new ItemStack(Blocks.COPPER_BLOCK));

        scene.showBasePlate();
        scene.idle(8);

        scene.idle(4);
        scene.world().showSection(displayCaddy, Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().position(exposedCopperExample)
                .add(util.select().position(copperExample)), Direction.UP);
        scene.idle(5);
        for (int x = 0; x <= 4; x++) {
            scene.world().showSection(util.select().position(util.grid().at(x, 1, 2)), Direction.UP);
            scene.idle(2);
        }
        scene.world().showSection(spritzer, Direction.DOWN);
        scene.world().modifyBlockEntity(spritzerPos, PerforatedSpritzerBlockEntity.class, be -> {
            be.getTankInventory().setFluid(new FluidStack(AllModFluids.SULFURIC_ACID.get(), 3500));
            be.setItemFilter(new ItemStack(Blocks.EXPOSED_COPPER));
            if (be.getFluidLevel() != null) {
                be.getFluidLevel().chase(1.0, 0.5, Chaser.EXP);
            }
        });
        scene.world().setKineticSpeed(belt, -16.0F);
        scene.idle(10);

        scene.overlay().showText(75)
                .text("The Precision Spritzer is filled with sulfuric acid and filtered for Exposed Copper.")
                .attachKeyFrame().colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(spritzerPos)).placeNearTarget();
        scene.idle(80);

        ElementLink<BeltItemElement> caddy = scene.world().createItemOnBelt(
                beltStart, Direction.DOWN, exposedCopperCaddy);
        scene.idle(60);
        scene.world().stallBeltItem(caddy, true);
        scene.overlay().showText(75)
                .text("The belt carries the Exposed Copper Caddy into the spray point.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(beltCenter)).placeNearTarget();
        scene.idle(85);

        scene.overlay().showText(70)
                .text("Three acid contacts remove one oxidation stage while the Caddy waits.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(beltCenter)).placeNearTarget();
        Vec3 sprayCenter = util.vector().centerOf(spritzerPos).add(0.0, -0.55, 0.0);
        for (int pulse = 0; pulse < 3; pulse++) {
            scene.effects().emitParticles(sprayCenter,
                    scene.effects().simpleParticleEmitter(ModParticles.ACID_DRIP.get(),
                            new Vec3(0.0, -0.1, 0.0)), 0.8F, 12);
            scene.idle(10);
        }
        scene.idle(55);
        scene.world().changeBeltItemTo(caddy, copperCaddy);
        scene.effects().indicateSuccess(beltCenter);
        scene.idle(8);
        scene.world().stallBeltItem(caddy, false);
        scene.idle(80);

        scene.overlay().showText(70)
                .text("The transformed Copper Block leaves the Spritzer on the same belt.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(beltEnd)).placeNearTarget();
        scene.idle(75);
    }

    public static void sawAndSpout(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("block_caddy.saw_spout", "Processing Cargo with Saws and Spouts");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(1.1F);

        BlockPos beltStart = util.grid().at(0, 1, 2);
        BlockPos beltEnd = util.grid().at(4, 1, 2);
        BlockPos sawBeltPos = util.grid().at(1, 1, 2);
        BlockPos spoutBeltPos = util.grid().at(3, 1, 2);
        BlockPos sawPos = sawBeltPos.above(2);
        BlockPos spoutPos = spoutBeltPos.above(2);
        var belt = util.select().fromTo(beltStart, beltEnd);
        var saw = util.select().position(sawPos);
        var spout = util.select().position(spoutPos);
        ItemStack oakLog = BlockCaddyItem.createStack(new ItemStack(Items.OAK_LOG));
        ItemStack strippedLog = BlockCaddyItem.createStack(new ItemStack(Blocks.STRIPPED_OAK_LOG));
        ItemStack powder = BlockCaddyItem.createStack(new ItemStack(Blocks.WHITE_CONCRETE_POWDER));
        ItemStack concrete = BlockCaddyItem.createStack(new ItemStack(Blocks.WHITE_CONCRETE));

        scene.showBasePlate();
        scene.idle(8);
        scene.world().showSection(belt, Direction.DOWN);
        scene.world().showSection(saw, Direction.DOWN);
        scene.world().showSection(spout, Direction.DOWN);
        scene.world().modifyBlockEntity(sawPos, SawBlockEntity.class, be -> be.setSpeed(32));
        scene.world().modifyBlockEntity(spoutPos, SpoutBlockEntity.class, be -> be.processingTicks = -1);
        scene.world().setKineticSpeed(belt, -16.0F);
        scene.idle(12);

        scene.overlay().showText(90)
                .text("A Caddy can carry blocks through supported Create processing machines.")
                .attachKeyFrame().colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(beltStart)).placeNearTarget();
        scene.idle(95);

        ElementLink<BeltItemElement> logCaddy = scene.world().createItemOnBelt(
                beltStart, Direction.DOWN, oakLog);
        scene.idle(35);
        scene.world().stallBeltItem(logCaddy, true);
        scene.overlay().showText(80)
                .text("The Saw strips the oak log cargo; the Caddy continues with the transformed block.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(sawPos)).placeNearTarget();
        scene.idle(25);
        scene.world().changeBeltItemTo(logCaddy, strippedLog);
        scene.effects().indicateSuccess(sawPos);
        scene.idle(12);
        scene.world().stallBeltItem(logCaddy, false);
        scene.idle(60);
        scene.world().removeItemsFromBelt(util.grid().at(4, 1, 2));

        ElementLink<BeltItemElement> powderCaddy = scene.world().createItemOnBelt(
                util.grid().at(1, 1, 2), Direction.DOWN, powder);
        scene.idle(28);
        scene.world().stallBeltItem(powderCaddy, true);
        scene.overlay().showText(80)
                .text("The Spout washes white concrete powder into concrete while preserving the Caddy.")
                .attachKeyFrame().colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(spoutPos)).placeNearTarget();
        scene.idle(25);
        scene.world().changeBeltItemTo(powderCaddy, concrete);
        scene.effects().indicateSuccess(spoutPos);
        scene.idle(12);
        scene.world().stallBeltItem(powderCaddy, false);
        scene.idle(45);

        scene.overlay().showText(115)
                .text("Caddy cargo also supports Combustion Belts, fans, compacting presses, and other added processing recipes.")
                .attachKeyFrame().colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(util.grid().at(2, 1, 2))).placeNearTarget();
        scene.idle(120);
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
