package io.hxneyw.repo.mixin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import io.hxneyw.repo.content.blocks.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.recipes.CombustionMixingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {BasinRecipe.class},
   remap = false
)
public abstract class BasinRecipeMixin {
   @Inject(
      method = {"match(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;)Z"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void enforceCombustionRequirements(BasinBlockEntity basin, Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
      if (recipe instanceof CombustionMixingRecipe combustion) {
         if (combustion.getRequiredHeat() == HeatCondition.SUPERHEATED && !createSulfuricResonance$hasRadiantHeat(basin)) {
            cir.setReturnValue(false);
         }
      }
   }

   @Unique
   private static boolean createSulfuricResonance$hasRadiantHeat(BasinBlockEntity basin) {
      Level level = basin.getLevel();
      if (level == null) {
         return false;
      } else {
         BlockPos heaterPos = basin.getBlockPos().below();
         BlockState heaterState = level.getBlockState(heaterPos);
         if (!(heaterState.getBlock() instanceof MoltenRotorBlock)) {
            return false;
         } else {
            return level.getBlockEntity(heaterPos) instanceof MoltenRotorBlockEntity rotor
               ? rotor.getCurrentHeatTier() == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT
               : false;
         }
      }
   }
}
