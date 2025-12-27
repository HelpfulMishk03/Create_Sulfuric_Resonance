package io.hxneyw.repo.mixin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import io.hxneyw.repo.content.recipes.CombustionMixingRecipe;
import io.hxneyw.repo.content.recipes.ModRecipeTypes;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(value = BasinOperatingBlockEntity.class, remap = false)
public abstract class BasinOperatingMixin {

    /**
     * Inject combustion mixing recipes into the basin's recipe matching system
     * Only adds recipes that match the current basin contents
     */
    @Inject(
            method = "getMatchingRecipes",
            at = @At("RETURN"),
            remap = false
    )
    private void injectCombustionRecipes(CallbackInfoReturnable<List<Recipe<?>>> cir) {
        BasinOperatingBlockEntity self = (BasinOperatingBlockEntity)(Object)this;
        Level level = self.getLevel();

        if (level == null) return;

        Optional<BasinBlockEntity> basinOpt = createSulfuricResonance$getBasin(self);
        if (basinOpt.isEmpty()) return;

        BasinBlockEntity basin = basinOpt.get();

        if (basin.isEmpty()) return;

        List<RecipeHolder<CombustionMixingRecipe>> combustionRecipes =
                level.getRecipeManager()
                        .getAllRecipesFor(ModRecipeTypes.COMBUSTION_MIXING.get());

        if (combustionRecipes.isEmpty()) return;

        List<Recipe<?>> recipes = cir.getReturnValue();

        // Only add recipes that match the basin's current contents
        for (RecipeHolder<CombustionMixingRecipe> holder : combustionRecipes) {
            if (BasinRecipe.match(basin, holder.value())) {
                recipes.add(holder.value());
            }
        }
    }

    @Unique
    private Optional<BasinBlockEntity> createSulfuricResonance$getBasin(BasinOperatingBlockEntity self) {
        Level level = self.getLevel();
        if (level == null) return Optional.empty();

        var basinBE = level.getBlockEntity(self.getBlockPos().below(2));
        if (!(basinBE instanceof BasinBlockEntity)) {
            return Optional.empty();
        }
        return Optional.of((BasinBlockEntity) basinBE);
    }
}