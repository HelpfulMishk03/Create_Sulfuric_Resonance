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
                        output.accept(Items.SULFUR.get());
                        output.accept(Items.NETHERRACK_DUST.get());
                        output.accept(Items.SPENT_ASH.get());
                        output.accept(Items.EMBER_CATALYST.get());
                    })
                    .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        CreateSulfuricResonance.LOGGER.info("Creative tabs registered for Sulfuric Resonance");
    }
}