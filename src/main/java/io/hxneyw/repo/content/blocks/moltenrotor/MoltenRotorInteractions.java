package io.hxneyw.repo.content.blocks.moltenrotor;

import com.simibubi.create.AllItems;
import io.hxneyw.repo.Config;
import io.hxneyw.repo.compat.fuel.FuelCompatibility;
import io.hxneyw.repo.compat.fuel.ResolvedFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;


final class MoltenRotorInteractions {
   private MoltenRotorInteractions() {
   }

   static ItemInteractionResult useItemOn(
           ItemStack stack,
           Level level,
           BlockPos pos,
           Player player
   ) {

      if (stack.isEmpty()) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }


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


      if (!creativeCake && !tnt && !netherStar && !dragonBreath && !recognizedFuel) {
         return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
      }


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
