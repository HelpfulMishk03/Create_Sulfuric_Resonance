package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberMenu;
import io.hxneyw.repo.content.menu.PrecisionSpritzerMenu;
import io.hxneyw.repo.content.menu.ThermalRelaySwitchMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AllModMenus {

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(
                    Registries.MENU,
                    CreateSulfuricResonance.MODID
            );

    public static final Supplier<MenuType<PrecisionSpritzerMenu>>
            PRECISION_SPRITZER = MENUS.register(
                    "precision_spritzer",
                    () -> new MenuType<>(
                            PrecisionSpritzerMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static final Supplier<MenuType<ThermalRelaySwitchMenu>>
            THERMAL_RELAY_SWITCH = MENUS.register(
                    "thermal_relay_switch",
                    () -> new MenuType<>(
                            ThermalRelaySwitchMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static final Supplier<MenuType<SulfuricResonanceChamberMenu>>
            SULFURIC_RESONANCE_CHAMBER = MENUS.register(
                    "sulfuric_resonance_chamber",
                    () -> new MenuType<>(
                            SulfuricResonanceChamberMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    private AllModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
