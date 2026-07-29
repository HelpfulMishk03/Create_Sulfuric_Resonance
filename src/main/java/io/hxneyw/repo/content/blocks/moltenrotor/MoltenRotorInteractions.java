package io.hxneyw.repo.content.blocks.moltenrotor;

import com.simibubi.create.AllItems;
import io.hxneyw.repo.Config;
import io.hxneyw.repo.compat.fuel.FuelCompatibility;
import io.hxneyw.repo.compat.fuel.ResolvedFuel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Owns Molten Rotor held-item routing, fuel insertion feedback, insertion
 * sounds, and empty-hand status output.
 *
 * <p>This class intentionally preserves the existing interaction behavior.
 * Wrench handling remains in {@link MoltenRotorBlock} because those callbacks
 * are inherited from Create's {@code IWrenchable} API.</p>
 */
final class MoltenRotorInteractions {
   private MoltenRotorInteractions() {
   }

   static ItemInteractionResult useItemOn(
           ItemStack stack,
           Level level,
           BlockPos pos,
           Player player
   ) {
      /*
       * Empty-hand interaction belongs to useWithoutItem().
       */
      if (stack.isEmpty()) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      /*
       * IMPORTANT: classify the held item without first requiring the client to
       * have a MoltenRotorBlockEntity instance. The old early furnace == null
       * branch routed every item into Item#useOn: logs placed as blocks and coal
       * appeared to do nothing.
       */
      ResolvedFuel resolvedFuel = FuelCompatibility.resolve(stack);
      MoltenRotorBlockEntity.FuelType fuelType =
              resolvedFuel == null || resolvedFuel.isInvalid()
                      ? null
                      : resolvedFuel.type();

      boolean creativeCake = stack.is(AllItems.CREATIVE_BLAZE_CAKE.get());
      boolean tnt = stack.is(Items.TNT);
      boolean netherStar = stack.is(Items.NETHER_STAR);
      boolean dragonBreath = stack.is(Items.DRAGON_BREATH);
      boolean recognizedFuel =
              fuelType != null
                      && fuelType != MoltenRotorBlockEntity.FuelType.NONE;

      /*
       * Non-fuels must skip the furnace's empty-hand/status interaction and
       * continue to the held item's own useOn method. BlockItems can therefore
       * place against the furnace normally.
       */
      if (!creativeCake && !tnt && !netherStar && !dragonBreath && !recognizedFuel) {
         return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
      }

      /*
       * The client has enough information to claim accepted furnace items now.
       * Actual mutation remains server-only.
       */
      if (level.isClientSide) {
         return ItemInteractionResult.SUCCESS;
      }

      MoltenRotorBlockEntity furnace = getBlockEntity(level, pos);
      if (furnace == null) {
         return ItemInteractionResult.FAIL;
      }

      if (creativeCake) {
         if (!furnace.isCreativeMode()) {
            furnace.setCreativeMode(true);
         } else {
            furnace.cycleCreativeTier();
         }
         return ItemInteractionResult.SUCCESS;
      }

      if (tnt) {
         if (Config.TNT_CAN_EXPLODE.get()
                 && furnace.tntCooldown <= 0
                 && level.random.nextFloat() < 0.25F) {

            furnace.tntCooldown = 20;
            level.removeBlock(pos, false);

            level.explode(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    1.5F,
                    Level.ExplosionInteraction.BLOCK
            );

            player.sendSystemMessage(Component.literal("FURNACE EXPLODED!"));
            return ItemInteractionResult.SUCCESS;
         }

         if (!furnace.insertFuel(stack, false)) {
            return ItemInteractionResult.FAIL;
         }

         furnace.tntCooldown = 5;
         playInsertionSound(level, pos);

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
         return ItemInteractionResult.SUCCESS;
      }

      if (netherStar) {
         furnace.addUltimateFuel(6000);
         playInsertionSound(level, pos);

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
         return ItemInteractionResult.SUCCESS;
      }

      if (dragonBreath) {
         furnace.addUltimateFuel(4000);
         playInsertionSound(level, pos);

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
         return ItemInteractionResult.SUCCESS;
      }

      if (!furnace.insertFuel(stack, false)) {
         if (fuelType == MoltenRotorBlockEntity.FuelType.STICK) {
            player.sendSystemMessage(Component.literal("Sticks require logs to be burning."));
         } else if (fuelType == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE
                 && !furnace.getCurrentHeatTier().isAtLeast(MoltenRotorBlockEntity.RotorHeatLevel.SEETHING)) {
            player.sendSystemMessage(Component.literal("Soul-Fired Blaze Cake requires SEETHING heat."));
         }
         return ItemInteractionResult.FAIL;
      }

      playInsertionSound(level, pos);

      if (fuelType == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE) {
         player.sendSystemMessage(Component.literal("Soul-Fired Blaze Cake inserted (+175s, maximum heat)."));
      }

      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }

      return ItemInteractionResult.SUCCESS;
   }

   static InteractionResult useWithoutItem(
           Level level,
           BlockPos pos,
           Player player
   ) {
      MoltenRotorBlockEntity furnace = getBlockEntity(level, pos);

      // Return PASS if there is no furnace or no status to show.
      if (furnace == null || !furnace.shouldShowStatus()) {
         return InteractionResult.PASS;
      }

      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }

      int temp = furnace.getDisplayTemperature();
      int fuel = furnace.getDisplayFuelTime();
      int cooldown = furnace.getDisplayCooldownTime();
      float rpm = furnace.getGeneratedSpeed();
      float stressUnits = furnace.getTotalStressOutput();
      String tierName = furnace.getHeatTierName();

      ChatFormatting tierColor = switch (furnace.getCurrentHeatTier()) {
         case NONE -> ChatFormatting.GRAY;
         case SMOULDERING, FADING -> ChatFormatting.YELLOW;
         case KINDLED -> ChatFormatting.GOLD;
         case SEETHING -> ChatFormatting.RED;
         case RADIANT -> ChatFormatting.DARK_PURPLE;
      };

      player.sendSystemMessage(
              Component.literal(tierName + " (" + temp + "°C)")
                      .withStyle(tierColor)
      );

      if (fuel > 0) {
         int fuelSeconds = fuel / 20;
         player.sendSystemMessage(
                 Component.literal(
                         String.format(
                                 "Fuel: %ds | RPM: %d | SU: %d",
                                 fuelSeconds,
                                 (int) rpm,
                                 (int) stressUnits
                         )
                 ).withStyle(ChatFormatting.GRAY)
         );
      } else if (cooldown > 0) {
         int cooldownSeconds = cooldown / 20;
         player.sendSystemMessage(
                 Component.literal(
                         String.format(
                                 "Cooling: %ds | RPM: %d | SU: %d",
                                 cooldownSeconds,
                                 (int) rpm,
                                 (int) stressUnits
                         )
                 ).withStyle(ChatFormatting.AQUA)
         );
      } else if (rpm > 0.0F) {
         player.sendSystemMessage(
                 Component.literal(
                         String.format(
                                 "RPM: %d | SU: %d",
                                 (int) rpm,
                                 (int) stressUnits
                         )
                 ).withStyle(ChatFormatting.GRAY)
         );
      }

      return InteractionResult.SUCCESS;
   }

   private static void playInsertionSound(Level level, BlockPos pos) {
      level.playSound(
              null,
              pos,
              SoundEvents.BUNDLE_INSERT,
              SoundSource.BLOCKS,
              0.25F,
              0.95F + level.random.nextFloat() * 0.10F
      );
   }

   private static MoltenRotorBlockEntity getBlockEntity(Level level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof MoltenRotorBlockEntity moltenRotor
              ? moltenRotor
              : null;
   }
}
