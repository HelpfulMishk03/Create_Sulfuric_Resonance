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
// ============================================================
// MACHINES, THERMOCHEMICAL INFRASTRUCTURE, AND UTILITIES
// ============================================================

// Core machines
                  output.accept(Items.MOLTEN_ROTOR_FURNACE.get());
                  output.accept(Items.SULFUR_BURNER_ITEM.get());
                  output.accept(
                          Items.SULFURIC_RESONANCE_CHAMBER_ITEM.get()
                  );
                  output.accept(Items.ASH_CERAMIC_CRUCIBLE_ITEM.get());
                  output.accept(Items.PERFORATED_SPRITZER.get());

// Thermochemical network
                  output.accept(Items.THERMOCHEMICAL_SHAFT_ITEM.get());
                  output.accept(Items.THERMOCHEMICAL_COGWHEEL_ITEM.get());
                  output.accept(Items.LARGE_THERMOCHEMICAL_COGWHEEL_ITEM.get());
                  output.accept(Items.THERMOCHEMICAL_GEARBOX_ITEM.get());
                  output.accept(Items.THERMOCHEMICAL_LINK_DRIVE_ITEM.get());
                  output.accept(Items.THERMOCHEMICAL_CONDUIT_ITEM.get());
                  output.accept(Items.COMBUSTION_BELT_CONNECTOR.get());

// Heat control and indication
                  output.accept(Items.THERMAL_RELAY_SWITCH_ITEM.get());
                  output.accept(Items.LIVING_EMBER_LAMP_ITEM.get());

// Utility / construction
                  output.accept(Items.RUBBER_PADDING.get());


// ============================================================
// SULFUR CHEMISTRY
// ============================================================

// Base sulfur
                  output.accept(Items.SULFUR.get());
                  output.accept(Items.SULFUR_BLOCK.get());
                  output.accept(Items.SULFURIC_ACID_BUCKET.get());

// Sulfur-derived components
                  output.accept(Items.EMBER_CATALYST.get());
                  output.accept(Items.BRIMSTONE_CORE.get());
                  output.accept(Items.SULFUROUS_FUEL_COMPOUND.get());
                  output.accept(Items.SULFUR_FUEL_BRIQUETTE.get());

// Ember / combustion materials
                  output.accept(Items.EMBERSOL.get());
                  output.accept(Items.FLAMEBORNE_CORE.get());
                  output.accept(Items.BLAZE_SHARD.get());


// ============================================================
// INDUSTRIAL FUELS
// ============================================================

// Carbon fuels
                  output.accept(Items.COKE.get());
                  output.accept(Items.CARBON_DEPOSIT_BLOCK_ITEM);
                  output.accept(Items.INFERNAL_COKE.get());
                  output.accept(Items.INFERNAL_CARBON_DEPOSIT_BLOCK_ITEM);

// Specialty fuels
                  output.accept(Items.MOLTEN_EMBER_PELLET.get());
                  output.accept(Items.CINDER_FUEL_BRIQUETTE.get());

// Blaze Cake variants
                  output.accept(Items.CORRUPT_BLAZE_CAKE.get());
                  output.accept(Items.SOUL_FIRED_BLAZE_CAKE.get());


// ============================================================
// CINDER AND ADVANCED MACHINE MATERIALS
// ============================================================

// Base cinder materials
                  output.accept(Items.NETHERWOOD_DUST.get());
                  output.accept(Items.REINFORCED_CINDER_COMPOUND.get());
                  output.accept(Items.CINDER_SANDPAPER.get());

// Obsidian fiber chain
                  output.accept(Items.OBSIDIAN_FIBER_MOLD.get());
                  output.accept(Items.OBSIDIAN_FIBER.get());
                  output.accept(Items.OBSIDIAN_CLOTH.get());

// Filament chain
                  output.accept(Items.UNFINISHED_FILAMENT.get());
                  output.accept(Items.CINDER_FILAMENT.get());

// Machine construction materials
                  output.accept(Items.ACID_ETCHED_COPPER_SHEET.get());
                  output.accept(Items.RESONANT_COPPER.get());
                  output.accept(Items.THERMOCHEMICAL_CASING.get());

// Impeller chain
                  output.accept(Items.IMPELLER_BLADE.get());
                  output.accept(Items.SHEATHED_IMPELLER_BLADE.get());
                  output.accept(Items.INFERNAL_IMPELLER.get());


// ============================================================
// RUBBER PROCESSING
// ============================================================

                  output.accept(Items.LATEX_CLUMP.get());
                  output.accept(Items.UNREFINED_RUBBER.get());
                  output.accept(Items.VULCANIZED_RUBBER.get());
                  output.accept(Items.MOLDED_RUBBER_GASKET.get());


// ============================================================
// ASH, CERAMIC, AND ASHESIL MATERIALS
// ============================================================

// Ash ceramic progression
                  output.accept(Items.SPENT_ASH.get());
                  output.accept(Items.WET_ASH_CERAMIC.get());
                  output.accept(Items.ASH_CERAMIC.get());
                  output.accept(Items.ACID_RESISTANT_CERAMIC.get());

// Ash brick progression
                  output.accept(Items.UNFIRED_ASH_BRICK.get());
                  output.accept(Items.ASH_BRICK.get());

// Ash brick construction set
                  output.accept(Items.ASH_BRICK_BLOCK_ITEM.get());
                  output.accept(Items.ASH_BRICK_SLAB_ITEM.get());
                  output.accept(Items.ASH_BRICK_STAIRS_ITEM.get());
                  output.accept(Items.ASH_BRICK_WALL_ITEM.get());
                  output.accept(Items.ASH_BRICK_PILLAR_ITEM.get());

// Ashesil
                  output.accept(Items.ASHESIL_ITEM.get());
                  output.accept(Items.ASHESIL_PANE_ITEM.get());
                  output.accept(Items.TEMPERED_ASHESIL_ITEM.get());
                  output.accept(Items.TEMPERED_ASHESIL_PANE_ITEM.get());


// ============================================================
// PYROCLASTIC EQUIPMENT
// ============================================================

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
