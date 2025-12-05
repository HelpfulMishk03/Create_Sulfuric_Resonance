package io.hxneyw.repo;

import com.mojang.logging.LogUtils;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.ModTabs;
import io.hxneyw.repo.content.entities.ModEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(CreateSulfuricResonance.MODID)
public class CreateSulfuricResonance {

    public static final String MODID = "sulfuricresonance";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateSulfuricResonance(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Planning the meetup point, learning the high quality recipes...");

        modEventBus.addListener(this::commonSetup);

        // Register items
        Items.register(modEventBus);
        LOGGER.info("Items logged... ready for next batch sequence");

        // Register entities
        ModEntities.register(modEventBus);
        LOGGER.info("Entities registered for Sulfuric Resonance");

        // Register creative tabs
        ModTabs.register(modEventBus);
        LOGGER.info("Suit up, next batch contains harmful gasses. Creative tab registrar");

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[{}] Common setup complete. Wheres your cell phone?", MODID);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[{}] Server is starting. Which one..", MODID);
    }
}