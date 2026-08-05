package io.hxneyw.repo.content;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItem;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import io.hxneyw.repo.content.items.*;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModFluids;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
@SuppressWarnings("unused")
public class Items {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("sulfuricresonance");
   public static final DeferredItem<Item> SULFUR = ITEMS.register("sulfur", () -> new SulfurItem(new Properties().stacksTo(64).fireResistant()));
   public static final DeferredItem<Item> ASH_CERAMIC = ITEMS.register("ash_ceramic", () -> new Item(new Properties().stacksTo(64)));

   public static final DeferredItem<Item> CINDER_FUEL_BRIQUETTE = ITEMS.register("cinder_fuel_briquette", () -> new Item(new Properties().stacksTo(64)));

   public static final DeferredItem<SandPaperItem> CINDER_SANDPAPER = ITEMS.register("cinder_sandpaper", () -> new SandPaperItem(new Properties()));

   public static final DeferredItem<Item> ASH_BRICK = ITEMS.register("ash_brick", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<BlockItem> ASH_BRICK_BLOCK_ITEM = ITEMS.register("ash_brick_block", () -> new BlockItem(AllModBlocks.ASH_BRICK_BLOCK.get(), new Properties()));
   public static final DeferredItem<BlockItem> ASH_BRICK_PILLAR_ITEM = ITEMS.register("ash_brick_pillar", () -> new BlockItem(AllModBlocks.ASH_BRICK_PILLAR.get(), new Properties()));

   public static final DeferredItem<BlockItem> ASH_BRICK_SLAB_ITEM = ITEMS.register("ash_brick_slab", () -> new BlockItem(AllModBlocks.ASH_BRICK_SLAB.get(), new Properties()));
   public static final DeferredItem<BlockItem> ASH_BRICK_STAIRS_ITEM = ITEMS.register("ash_brick_stairs", () -> new BlockItem(AllModBlocks.ASH_BRICK_STAIRS.get(), new Properties()));
   public static final DeferredItem<BlockItem> ASH_BRICK_WALL_ITEM = ITEMS.register("ash_brick_wall", () -> new BlockItem(AllModBlocks.ASH_BRICK_WALL.get(), new Properties()));
   public static final DeferredItem<Item> NETHERWOOD_DUST = ITEMS.register("netherwood_dust", () -> new NetherwoodDustItem(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> SPENT_ASH = ITEMS.register("spent_ash", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> EMBER_CATALYST = ITEMS.register("ember_catalyst", () -> new EmberCatalystItem(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> EMBERSOL = ITEMS.register("embersol", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> BLAZE_SHARD = ITEMS.register("blaze_shard", () -> new Item(new Properties().stacksTo(64).fireResistant()));
   public static final DeferredItem<Item> PYROCLASTIC_POWDER = ITEMS.register("pyroclastic_powder", () -> new Item(new Properties().stacksTo(64).fireResistant()));
   public static final DeferredItem<Item> PYROCLAST_BOMB = ITEMS.register("pyroclast_bomb", () -> new PyroclastBombItem(new Properties().stacksTo(16)));
   public static final DeferredItem<Item> REINFORCED_CINDER_COMPOUND = ITEMS.register("reinforced_cinder_compound", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> OBSIDIAN_FIBER_MOLD = ITEMS.register("obsidian_fiber_mold", () -> new Item(new Properties().stacksTo(16)));
   @SuppressWarnings("unused")
   public static final DeferredItem<BlockItem> ASH_CERAMIC_CRUCIBLE_ITEM = ITEMS.register("ash_ceramic_crucible", () -> new BlockItem(AllModBlocks.ASH_CERAMIC_CRUCIBLE.get(), new Properties()));
   public static final DeferredItem<Item> OBSIDIAN_FIBER_MOLD_FILLED = ITEMS.register("obsidian_fiber_mold_filled", () -> new Item(new Properties().stacksTo(16)));
   @SuppressWarnings("unused")
   public static final DeferredItem<BlockItem> ASHESIL_ITEM =
           ITEMS.register(
                   "ashesil",
                   () -> new BlockItem(
                           AllModBlocks.ASHESIL.get(),
                           new Properties()
                   )
           );
   public static final DeferredItem<Item> WET_ASH_CERAMIC =
           ITEMS.register(
                   "wet_ash_ceramic",
                   () -> new Item(
                           new Properties().stacksTo(64)
                   )
           );

   public static final DeferredItem<Item> UNFIRED_ASH_BRICK =
           ITEMS.register(
                   "unfired_ash_brick",
                   () -> new Item(
                           new Properties().stacksTo(64)
                   )
           );

   public static final DeferredItem<Item> ACID_RESISTANT_CERAMIC =
           ITEMS.register(
                   "acid_resistant_ceramic",
                   () -> new Item(
                           new Properties().stacksTo(64)
                   )
           );
   public static final DeferredItem<BlockItem> ASHESIL_PANE_ITEM =
           ITEMS.register(
                   "ashesil_pane",
                   () -> new BlockItem(
                           AllModBlocks.ASHESIL_PANE.get(),
                           new Properties()
                   )
           );

   public static final DeferredItem<LivingEmberLampItem> LIVING_EMBER_LAMP_ITEM =
           ITEMS.register(
                   "living_ember_lamp",
                   () -> new LivingEmberLampItem(
                           AllModBlocks.LIVING_EMBER_LAMP.get(),
                           new Properties()
                   )
           );

   public static final DeferredItem<Item> OBSIDIAN_FIBER = ITEMS.register("obsidian_fiber", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> OBSIDIAN_CLOTH = ITEMS.register("obsidian_cloth", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> UNFINISHED_FILAMENT = ITEMS.register("unfinished_filament", () -> new Item(new Properties().stacksTo(64)));

   public static final DeferredItem<Item> CINDER_FILAMENT = ITEMS.register("cinder_filament", () -> new Item(new Properties().stacksTo(64)));

   public static final DeferredItem<Item> ACID_ETCHED_COPPER_SHEET = ITEMS.register("acid_etched_copper_sheet", () -> new Item(new Properties().stacksTo(64)));

   public static final DeferredItem<SequencedAssemblyItem> INCOMPLETE_THERMOCHEMICAL_CASING = ITEMS.register("incomplete_thermochemical_casing", () -> new SequencedAssemblyItem(new Properties()));

   public static final DeferredItem<Item> THERMOCHEMICAL_CASING = ITEMS.register("thermochemical_casing", () -> new Item(new Properties().stacksTo(64)));

   public static final DeferredItem<Item> LATEX_CLUMP = ITEMS.register("latex_clump", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<ThermalRelaySwitchItem> THERMAL_RELAY_SWITCH_ITEM = ITEMS.register("thermal_relay_switch", () -> new ThermalRelaySwitchItem(AllModBlocks.THERMAL_RELAY_SWITCH.get(), new Properties()));

   public static final DeferredItem<CombustionBeltConnectorItem> COMBUSTION_BELT_CONNECTOR = ITEMS.register("combustion_belt_connector", () -> new CombustionBeltConnectorItem(new Properties().stacksTo(64)));

   public static final DeferredItem<BlockItem> THERMOCHEMICAL_CONDUIT_ITEM = ITEMS.register("thermochemical_conduit", () -> new BlockItem(AllModBlocks.THERMOCHEMICAL_CONDUIT.get(), new Properties()));



   public static final DeferredItem<BlockItem> THERMOCHEMICAL_SHAFT_ITEM = ITEMS.register("thermochemical_shaft", () -> new BlockItem(AllModBlocks.THERMOCHEMICAL_SHAFT.get(), new Properties()));

   public static final DeferredItem<BlockItem> THERMOCHEMICAL_GEARBOX_ITEM = ITEMS.register("thermochemical_gearbox", () -> new BlockItem(AllModBlocks.THERMOCHEMICAL_GEARBOX.get(), new Properties()));

   public static final DeferredItem<Item> UNREFINED_RUBBER = ITEMS.register("unrefined_rubber", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> VULCANIZED_RUBBER = ITEMS.register("vulcanized_rubber", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> MOLDED_RUBBER_GASKET = ITEMS.register("molded_rubber_gasket", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> INFERNAL_IMPELLER = ITEMS.register("infernal_impeller", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> IMPELLER_BLADE = ITEMS.register("impeller_blade", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> SHEATHED_IMPELLER_BLADE = ITEMS.register("sheathed_impeller_blade", () -> new SheathedImpellerBladeItem(new Properties().stacksTo(1)));
   public static final DeferredItem<BlockItem> PERFORATED_SPRITZER = ITEMS.register("perforated_spritzer", () -> new BlockItem(AllModBlocks.PERFORATED_SPRITZER.get(), new Properties()));
   public static final DeferredItem<Item> FLAMEBORNE_CORE = ITEMS.register("flameborne_core", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> RUBBER_PADDING = ITEMS.register("rubber_padding", () -> new RubberPaddingBlockItem(AllModBlocks.RUBBER_PADDING.get(), new Properties()));
   public static final DeferredItem<Item> CORRUPT_BLAZE_CAKE = ITEMS.register("corrupt_blaze_cake", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> SOUL_FIRED_BLAZE_CAKE = ITEMS.register("soul_fired_blaze_cake", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> MOLTEN_ROTOR_FURNACE = ITEMS.register("molten_rotor_furnace", () -> new BlockItem(AllModBlocks.MOLTEN_ROTOR_FURNACE.get(), new Properties().stacksTo(64)));
   public static final DeferredItem<BlockItem> SULFUR_BLOCK = ITEMS.register("sulfur_block", () -> new BlockItem(AllModBlocks.SULFUR_BLOCK.get(), new Properties()));
   public static final DeferredItem<BucketItem> SULFURIC_ACID_BUCKET = ITEMS.register("sulfuric_acid_bucket", () -> new BucketItem(AllModFluids.SULFURIC_ACID.get(), new Properties().craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)));

   public static void register(IEventBus eventBus) {
      LOGGER.info("Registering Sulfuric Resonance items");
      ITEMS.register(eventBus);
   }
}
