package io.hxneyw.repo.content.recipes;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CombustionMixingRecipe extends BasinRecipe {

    public CombustionMixingRecipe(ProcessingRecipeParams params) {
        super(ModRecipeTypes.COMBUSTION_MIXING_TYPE_INFO, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 64;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 2;
    }

    @Override
    public boolean canRequireHeat() {
        return true;
    }

    @Override
    public boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(RecipeInput input, @NotNull Level level) {
        return false;
    }
}