package io.hxneyw.repo.ponder;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class SulfuricResonancePonderPlugin implements PonderPlugin {
   @NotNull
   public String getModId() {
      CreateSulfuricResonance.LOGGER.info("Ponder Plugin loading for: {}", "sulfuricresonance");
      return "sulfuricresonance";
   }

   public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
      CreateSulfuricResonance.LOGGER.info("Registering Ponder scenes...");

      try {
         ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey((Block)AllModBlocks.PERFORATED_SPRITZER.get());
         CreateSulfuricResonance.LOGGER.info("Block ID: {}", blockId);
         helper.addStoryBoard(blockId, "perforated_spritzer/intro", PerforatedSpritzerScenes::intro, new ResourceLocation[]{AllPonderTags.FLUIDS});
         helper.addStoryBoard(
            blockId, "perforated_spritzer/mob_automation", PerforatedSpritzerScenes::mobAutomation, new ResourceLocation[]{AllPonderTags.FLUIDS}
         );
         CreateSulfuricResonance.LOGGER.info("Successfully registered 2 scenes for: {}", blockId);
      } catch (Exception var3) {
         CreateSulfuricResonance.LOGGER.error("Failed to register Ponder scenes!", var3);
      }
   }

   public void registerTags(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
      CreateSulfuricResonance.LOGGER.info("Registering Ponder tags...");

      try {
         AllPonderTags.register(helper);
         CreateSulfuricResonance.LOGGER.info("Successfully registered Ponder tags");
      } catch (Exception var3) {
         CreateSulfuricResonance.LOGGER.error("Failed to register Ponder tags!", var3);
      }
   }
}
