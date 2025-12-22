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

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateSulfuricResonance.MODID);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SULFURIC_RESONANCE_TAB =
            CREATIVE_MODE_TABS.register("sulfuric_resonance", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sulfuricresonance.sulfuric_resonance"))
                    .icon(() -> new ItemStack(Items.SULFUR.get()))
                    .displayItems((params, output) -> {
                        // Items
                        output.accept(Items.REINFORCED_CINDER_COMPOUND.get());
                        output.accept(Items.OBSIDIAN_FIBER_MOLD.get());
                        output.accept(Items.OBSIDIAN_FIBER.get());
                        output.accept(Items.INFERNAL_IMPELLER.get());
                        output.accept(Items.FLAMEBORNE_CORE.get());
                        output.accept(Items.LATEX_CLUMP.get());
                        output.accept(Items.UNREFINED_RUBBER.get());
                        output.accept(Items.VULCANIZED_RUBBER.get());
                        output.accept(Items.SULFUR.get());
                        output.accept(Items.NETHERWOOD_DUST.get());
                        output.accept(Items.SPENT_ASH.get());
                        output.accept(Items.EMBER_CATALYST.get());
                        output.accept(Items.CORRUPT_BLAZE_CAKE.get());
                        output.accept(Items.SOUL_FIRED_BLAZE_CAKE.get());
                        output.accept(Items.EMBERSOL.get());
                        output.accept(Items.BLAZE_SHARD.get());
                        output.accept(Items.PYROCLASTIC_POWDER.get());
                        output.accept(Items.PYROCLAST_BOMB.get());

                        // Blocks (via their BlockItems)
                        output.accept(Items.MOLTEN_ROTOR_FURNACE.get());
                    })
                    .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        CreateSulfuricResonance.LOGGER.info("Creative tabs registered for Sulfuric Resonance");
    }
}