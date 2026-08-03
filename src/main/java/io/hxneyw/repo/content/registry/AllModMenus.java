package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.menu.ThermalRelaySwitchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AllModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(
                    Registries.MENU,
                    CreateSulfuricResonance.MODID
            );

    public static final DeferredHolder<
            MenuType<?>,
            MenuType<ThermalRelaySwitchMenu>
            > THERMAL_RELAY_SWITCH = MENUS.register(
            "thermal_relay_switch",
            () -> new MenuType<>(
                    ThermalRelaySwitchMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            )
    );

    private AllModMenus() {
    }

    public static void register(
            IEventBus eventBus
    ) {
        MENUS.register(eventBus);
    }
}
