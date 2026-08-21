package io.hxneyw.repo;

import com.mojang.logging.LogUtils;
import io.hxneyw.repo.compat.arm.AllModArmInteractionPoints;
import io.hxneyw.repo.compat.automation.ModCapabilities;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeHostPayload;
import io.hxneyw.repo.content.ModTabs;
import io.hxneyw.repo.content.entities.ModEntities;
import io.hxneyw.repo.content.recipes.ModRecipeTypes;
import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipeRegistry;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipeRegistry;
import io.hxneyw.repo.content.process.ProcessMonitorArmPayload;
import io.hxneyw.repo.content.registry.*;
import io.hxneyw.repo.ponder.SulfuricResonancePonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod("sulfuricresonance")
public class CreateSulfuricResonance {
   public static final String MODID = "sulfuricresonance";
   public static final Logger LOGGER = LogUtils.getLogger();

   public CreateSulfuricResonance(IEventBus modEventBus, ModContainer modContainer) {
      Items.register(modEventBus);
      ModTabs.register(modEventBus);
      ModEntities.register(modEventBus);
      AllModBlocks.register(modEventBus);
      AllBlockEntities.register(modEventBus);
      ModRecipeTypes.register(modEventBus);
      CombustionBeltRecipeRegistry.register(modEventBus);
      SulfuricResonanceChamberRecipeRegistry.register(modEventBus);
      AllModArmInteractionPoints.register(modEventBus);
      AllModMenus.register(modEventBus);
      ModParticles.PARTICLE_TYPES.register(modEventBus);
      modEventBus.addListener(ModCapabilities::registerCapabilities);
      modEventBus.addListener(ProcessMonitorArmPayload::register);
      modEventBus.addListener(ThermalGaugeHostPayload::register);
      AllModSounds.SOUNDS.register(modEventBus);

      AllModFluids.register(modEventBus);
      modEventBus.addListener(
              CinderSandpaperComponents::modifyDefaultComponents
      );
      AllModEffects.register(modEventBus);
      NeoForge.EVENT_BUS.register(this);
      modContainer.registerConfig(Type.COMMON, Config.SPEC);
   }

   @SubscribeEvent
   public void onServerStarting(ServerStartingEvent event) {
   }

   @EventBusSubscriber(
      modid = "sulfuricresonance",
      value = {Dist.CLIENT}
   )
   public static class ClientModEvents {
      @SubscribeEvent
      public static void onClientSetup(FMLClientSetupEvent event) {
         event.enqueueWork(() ->
                 PonderIndex.addPlugin(new SulfuricResonancePonderPlugin())

         );
      }
   }
}
