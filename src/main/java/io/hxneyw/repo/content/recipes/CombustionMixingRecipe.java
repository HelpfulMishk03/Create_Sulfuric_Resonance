package io.hxneyw.repo.content.recipes;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;


public class CombustionMixingRecipe extends BasinRecipe {

    public CombustionMixingRecipe(ProcessingRecipeParams params) {
        super(ModRecipeTypes.COMBUSTION_MIXING_TYPE_INFO, params);
    }

    // All other methods are inherited from BasinRecipe with the correct behavior:
    // - getMaxInputCount() = 64
    // - getMaxOutputCount() = 4
    // - getMaxFluidInputCount() = 2
    // - getMaxFluidOutputCount() = 2
    // - canRequireHeat() = true
    // - canSpecifyDuration() = true
    // - matches() = false (ProcessingRecipes don't use standard matching)
}