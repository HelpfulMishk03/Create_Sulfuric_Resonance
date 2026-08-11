package io.hxneyw.repo.client.gui;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.registry.AllModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class SulfuricResonanceChamberClient {

    private SulfuricResonanceChamberClient() {
    }

    @SubscribeEvent
    public static void registerScreens(
            RegisterMenuScreensEvent event
    ) {
        event.register(
                AllModMenus.SULFURIC_RESONANCE_CHAMBER.get(),
                SulfuricResonanceChamberScreen::new
        );
    }
}
