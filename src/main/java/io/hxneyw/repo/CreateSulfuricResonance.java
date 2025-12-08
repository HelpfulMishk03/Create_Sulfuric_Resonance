package io.hxneyw.repo;

import com.mojang.logging.LogUtils;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.ModTabs;
import io.hxneyw.repo.content.ModBlocks;
import io.hxneyw.repo.content.ModBlockEntities;
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
        LOGGER.info("========== CREATE SULFURIC RESONANCE CONSTRUCTOR CALLED ==========");

        modEventBus.addListener(this::commonSetup);

        // Register items
        Items.register(modEventBus);
        LOGGER.info("========== ITEMS.REGISTER CALLED ==========");

        // Register creative tabs
        ModTabs.register(modEventBus);
        LOGGER.info("========== TABS.REGISTER CALLED ==========");

        // Register entities
        ModEntities.register(modEventBus);
        LOGGER.info("========== ENTITIES.REGISTER CALLED ==========");

        // Register blocks
        ModBlocks.register(modEventBus);
        LOGGER.info("========== BLOCKS.REGISTER CALLED ==========");

        // Register block entities
        ModBlockEntities.register(modEventBus);
        LOGGER.info("========== BLOCK ENTITIES.REGISTER CALLED ==========");

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[{}] Common setup complete.", MODID);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[{}] Server is starting.", MODID);
    }
}