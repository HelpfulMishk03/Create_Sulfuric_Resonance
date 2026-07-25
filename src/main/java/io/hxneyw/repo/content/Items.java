package io.hxneyw.repo.content;

import com.mojang.logging.LogUtils;
import io.hxneyw.repo.content.items.EmberCatalystItem;
import io.hxneyw.repo.content.items.NetherwoodDustItem;
import io.hxneyw.repo.content.items.PyroclastBombItem;
import io.hxneyw.repo.content.items.RubberPaddingBlockItem;
import io.hxneyw.repo.content.items.SheathedImpellerBladeItem;
import io.hxneyw.repo.content.items.SulfurItem;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModFluids;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

public class Items {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final net.neoforged.neoforge.registries.DeferredRegister.Items ITEMS = DeferredRegister.createItems("sulfuricresonance");
   public static final DeferredItem<Item> SULFUR = ITEMS.register("sulfur", () -> new SulfurItem(new Properties().stacksTo(64).fireResistant()));
   public static final DeferredItem<Item> NETHERWOOD_DUST = ITEMS.register("netherwood_dust", () -> new NetherwoodDustItem(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> SPENT_ASH = ITEMS.register("spent_ash", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> EMBER_CATALYST = ITEMS.register("ember_catalyst", () -> new EmberCatalystItem(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> EMBERSOL = ITEMS.register("embersol", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> BLAZE_SHARD = ITEMS.register("blaze_shard", () -> new Item(new Properties().stacksTo(64).fireResistant()));
   public static final DeferredItem<Item> PYROCLASTIC_POWDER = ITEMS.register(
      "pyroclastic_powder", () -> new Item(new Properties().stacksTo(64).fireResistant())
   );
   public static final DeferredItem<Item> PYROCLAST_BOMB = ITEMS.register("pyroclast_bomb", () -> new PyroclastBombItem(new Properties().stacksTo(16)));
   public static final DeferredItem<Item> REINFORCED_CINDER_COMPOUND = ITEMS.register(
      "reinforced_cinder_compound", () -> new Item(new Properties().stacksTo(64))
   );
   public static final DeferredItem<Item> OBSIDIAN_FIBER_MOLD = ITEMS.register("obsidian_fiber_mold", () -> new Item(new Properties().stacksTo(16)));
   public static final DeferredItem<Item> OBSIDIAN_FIBER_MOLD_FILLED = ITEMS.register(
      "obsidian_fiber_mold_filled", () -> new Item(new Properties().stacksTo(16))
   );
   public static final DeferredItem<Item> OBSIDIAN_FIBER = ITEMS.register("obsidian_fiber", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> LATEX_CLUMP = ITEMS.register("latex_clump", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> UNREFINED_RUBBER = ITEMS.register("unrefined_rubber", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> VULCANIZED_RUBBER = ITEMS.register("vulcanized_rubber", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> MOLDED_RUBBER_GASKET = ITEMS.register("molded_rubber_gasket", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> INFERNAL_IMPELLER = ITEMS.register("infernal_impeller", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> IMPELLER_BLADE = ITEMS.register("impeller_blade", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> SHEATHED_IMPELLER_BLADE = ITEMS.register(
      "sheathed_impeller_blade", () -> new SheathedImpellerBladeItem(new Properties().stacksTo(1))
   );
   public static final DeferredItem<BlockItem> PERFORATED_SPRITZER = ITEMS.register(
      "perforated_spritzer", () -> new BlockItem((Block)AllModBlocks.PERFORATED_SPRITZER.get(), new Properties())
   );
   public static final DeferredItem<Item> FLAMEBORNE_CORE = ITEMS.register("flameborne_core", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> RUBBER_PADDING = ITEMS.register(
      "rubber_padding", () -> new RubberPaddingBlockItem((Block)AllModBlocks.RUBBER_PADDING.get(), new Properties())
   );
   public static final DeferredItem<Item> CORRUPT_BLAZE_CAKE = ITEMS.register("corrupt_blaze_cake", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> SOUL_FIRED_BLAZE_CAKE = ITEMS.register("soul_fired_blaze_cake", () -> new Item(new Properties().stacksTo(64)));
   public static final DeferredItem<Item> MOLTEN_ROTOR_FURNACE = ITEMS.register(
      "molten_rotor_furnace", () -> new BlockItem((Block)AllModBlocks.MOLTEN_ROTOR_FURNACE.get(), new Properties())
   );
   public static final DeferredItem<BlockItem> SULFUR_BLOCK = ITEMS.register(
      "sulfur_block", () -> new BlockItem((Block)AllModBlocks.SULFUR_BLOCK.get(), new Properties())
   );
   public static final DeferredItem<BucketItem> SULFURIC_ACID_BUCKET = ITEMS.register(
      "sulfuric_acid_bucket",
      () -> new BucketItem((Fluid)AllModFluids.SULFURIC_ACID.get(), new Properties().craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1))
   );

   public static void register(IEventBus eventBus) {
      LOGGER.info("Registering Sulfuric Resonance items");
      ITEMS.register(eventBus);
   }
}
