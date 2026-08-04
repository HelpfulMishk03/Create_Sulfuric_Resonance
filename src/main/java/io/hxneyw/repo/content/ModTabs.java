package io.hxneyw.repo.content;

import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "sulfuricresonance");
    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SULFURIC_RESONANCE_TAB = CREATIVE_MODE_TABS.register(
      "sulfuric_resonance",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.sulfuricresonance.sulfuric_resonance"))
              .icon(() -> new ItemStack(Items.SULFUR.get()))
              .displayItems((params, output) -> {
                  // Machines and utility blocks
                  output.accept(Items.MOLTEN_ROTOR_FURNACE.get());
                  output.accept(Items.PERFORATED_SPRITZER.get());
                  output.accept(Items.LIVING_EMBER_LAMP_ITEM.get());
                  output.accept(Items.THERMAL_RELAY_SWITCH_ITEM.get());
                  output.accept(Items.COMBUSTION_BELT_CONNECTOR.get());
                  output.accept(
                          Items.THERMOCHEMICAL_CONDUIT_ITEM.get()
                  );
                  output.accept(Items.RUBBER_PADDING.get());

// Sulfur and combustion
                  output.accept(Items.SULFUR.get());
                  output.accept(Items.SULFUR_BLOCK.get());
                  output.accept(Items.SULFURIC_ACID_BUCKET.get());
                  output.accept(Items.EMBER_CATALYST.get());
                  output.accept(Items.FLAMEBORNE_CORE.get());
                  output.accept(Items.EMBERSOL.get());
                  output.accept(Items.BLAZE_SHARD.get());
                  output.accept(Items.CORRUPT_BLAZE_CAKE.get());
                  output.accept(Items.SOUL_FIRED_BLAZE_CAKE.get());
                  output.accept(Items.CINDER_FUEL_BRIQUETTE.get());

// Cinder and advanced machine materials
                  output.accept(Items.NETHERWOOD_DUST.get());
                  output.accept(Items.REINFORCED_CINDER_COMPOUND.get());
                  output.accept(Items.CINDER_SANDPAPER.get());
                  output.accept(Items.OBSIDIAN_FIBER_MOLD.get());
                  output.accept(Items.OBSIDIAN_FIBER.get());
                  output.accept(Items.OBSIDIAN_CLOTH.get());
                  output.accept(Items.UNFINISHED_FILAMENT.get());
                  output.accept(Items.CINDER_FILAMENT.get());
                  output.accept(Items.ACID_ETCHED_COPPER_SHEET.get());
                  output.accept(Items.INCOMPLETE_THERMOCHEMICAL_CASING.get());
                  output.accept(Items.THERMOCHEMICAL_CASING.get());
                  output.accept(Items.IMPELLER_BLADE.get());
                  output.accept(Items.SHEATHED_IMPELLER_BLADE.get());
                  output.accept(Items.INFERNAL_IMPELLER.get());

// Rubber processing
                  output.accept(Items.LATEX_CLUMP.get());
                  output.accept(Items.UNREFINED_RUBBER.get());
                  output.accept(Items.VULCANIZED_RUBBER.get());
                  output.accept(Items.MOLDED_RUBBER_GASKET.get());

// Ash materials and construction
                  output.accept(Items.SPENT_ASH.get());
                  output.accept(Items.ASH_CERAMIC.get());
                  output.accept(Items.ASH_CERAMIC_CRUCIBLE_ITEM.get());
                  output.accept(Items.ASH_BRICK.get());
                  output.accept(Items.ASH_BRICK_BLOCK_ITEM.get());
                  output.accept(Items.ASH_BRICK_SLAB_ITEM.get());
                  output.accept(Items.ASH_BRICK_STAIRS_ITEM.get());
                  output.accept(Items.ASH_BRICK_WALL_ITEM.get());
                  output.accept(Items.ASH_BRICK_PILLAR_ITEM.get());
                  output.accept(Items.ASHESIL_ITEM.get());
                  output.accept(Items.ASHESIL_PANE_ITEM.get());

// Pyroclastic equipment
                  output.accept(Items.PYROCLASTIC_POWDER.get());
                  output.accept(Items.PYROCLAST_BOMB.get());
              })
         .build()
   );

   public static void register(IEventBus eventBus) {
      CREATIVE_MODE_TABS.register(eventBus);
      CreateSulfuricResonance.LOGGER.info("Creative tabs registered for Sulfuric Resonance");
   }
}
