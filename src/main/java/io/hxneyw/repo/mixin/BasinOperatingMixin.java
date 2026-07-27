package io.hxneyw.repo.mixin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import io.hxneyw.repo.content.recipes.CombustionMixingRecipe;
import io.hxneyw.repo.content.recipes.ModRecipeTypes;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {BasinOperatingBlockEntity.class},
   remap = false
)
public abstract class BasinOperatingMixin {
   @Inject(
           method = {"getMatchingRecipes"},
           at = {@At("RETURN")},
           remap = false
   )
   private void injectCombustionRecipes(
           CallbackInfoReturnable<List<Recipe<?>>> cir
   ) {
      BasinOperatingBlockEntity self =
              (BasinOperatingBlockEntity)(Object)this;

      Level level = self.getLevel();

      if (level == null) {
         return;
      }

      Optional<BasinBlockEntity> basinOpt =
              this.createSulfuricResonance$getBasin(self);

      if (basinOpt.isEmpty()) {
         return;
      }

      BasinBlockEntity basin = basinOpt.get();

      if (basin.isEmpty()) {
         return;
      }

      List<RecipeHolder<CombustionMixingRecipe>> combustionRecipes =
              level.getRecipeManager()
                      .getAllRecipesFor(
                              ModRecipeTypes.COMBUSTION_MIXING.get()
                      );

      if (combustionRecipes.isEmpty()) {
         return;
      }

      List<Recipe<?>> recipes = cir.getReturnValue();

      for (RecipeHolder<CombustionMixingRecipe> holder
              : combustionRecipes) {

         if (BasinRecipe.match(basin, holder.value())) {
            recipes.add(holder.value());
         }
      }
   }
   @Unique
   private Optional<BasinBlockEntity>
   createSulfuricResonance$getBasin(
           BasinOperatingBlockEntity self
   ) {
      Level level = self.getLevel();

      if (level == null) {
         return Optional.empty();
      }

      BlockEntity basinBlockEntity =
              level.getBlockEntity(
                      self.getBlockPos().below(2)
              );

      return basinBlockEntity
              instanceof BasinBlockEntity basin
              ? Optional.of(basin)
              : Optional.empty();
   }
}
