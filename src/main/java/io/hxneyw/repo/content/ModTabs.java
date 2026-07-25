package io.hxneyw.repo.content;

import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTabs {
   public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "sulfuricresonance");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SULFURIC_RESONANCE_TAB = CREATIVE_MODE_TABS.register(
      "sulfuric_resonance",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.sulfuricresonance.sulfuric_resonance"))
         .icon(() -> new ItemStack((ItemLike)Items.SULFUR.get()))
         .displayItems((params, output) -> {
            output.accept((ItemLike)Items.REINFORCED_CINDER_COMPOUND.get());
            output.accept((ItemLike)Items.OBSIDIAN_FIBER_MOLD.get());
            output.accept((ItemLike)Items.OBSIDIAN_FIBER.get());
            output.accept((ItemLike)Items.INFERNAL_IMPELLER.get());
            output.accept((ItemLike)Items.IMPELLER_BLADE.get());
            output.accept((ItemLike)Items.SHEATHED_IMPELLER_BLADE.get());
            output.accept((ItemLike)Items.FLAMEBORNE_CORE.get());
            output.accept((ItemLike)Items.LATEX_CLUMP.get());
            output.accept((ItemLike)Items.UNREFINED_RUBBER.get());
            output.accept((ItemLike)Items.VULCANIZED_RUBBER.get());
            output.accept((ItemLike)Items.MOLDED_RUBBER_GASKET.get());
            output.accept((ItemLike)Items.SULFUR.get());
            output.accept((ItemLike)Items.SULFUR_BLOCK.get());
            output.accept((ItemLike)Items.SULFURIC_ACID_BUCKET.get());
            output.accept((ItemLike)Items.NETHERWOOD_DUST.get());
            output.accept((ItemLike)Items.SPENT_ASH.get());
            output.accept((ItemLike)Items.EMBER_CATALYST.get());
            output.accept((ItemLike)Items.CORRUPT_BLAZE_CAKE.get());
            output.accept((ItemLike)Items.SOUL_FIRED_BLAZE_CAKE.get());
            output.accept((ItemLike)Items.EMBERSOL.get());
            output.accept((ItemLike)Items.BLAZE_SHARD.get());
            output.accept((ItemLike)Items.PYROCLASTIC_POWDER.get());
            output.accept((ItemLike)Items.PYROCLAST_BOMB.get());
            output.accept((ItemLike)Items.MOLTEN_ROTOR_FURNACE.get());
            output.accept((ItemLike)Items.RUBBER_PADDING.get());
            output.accept((ItemLike)Items.PERFORATED_SPRITZER.get());
         })
         .build()
   );

   public static void register(IEventBus eventBus) {
      CREATIVE_MODE_TABS.register(eventBus);
      CreateSulfuricResonance.LOGGER.info("Creative tabs registered for Sulfuric Resonance");
   }
}
