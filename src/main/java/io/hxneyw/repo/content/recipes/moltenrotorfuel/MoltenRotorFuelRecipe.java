package io.hxneyw.repo.content.recipes.moltenrotorfuel;

import io.hxneyw.repo.compat.fuel.ResolvedFuel;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Data-driven Molten Rotor fuel definition.
 *
 * <p>These recipes are intentionally special/non-crafting recipes. Datapacks
 * and KubeJS can add, replace, or remove them through the normal recipe
 * manager without CSR needing a hard dependency on KubeJS.</p>
 */
public final class MoltenRotorFuelRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final boolean enabled;
    private final float burnTimeTicks;
    private final float heatingRate;
    private final float maximumTemperature;
    private final int maximumUnits;
    private final MoltenRotorBlockEntity.FuelType behavior;
    private final int priority;

    public MoltenRotorFuelRecipe(
            Ingredient ingredient,
            boolean enabled,
            float burnTimeTicks,
            float heatingRate,
            float maximumTemperature,
            int maximumUnits,
            MoltenRotorBlockEntity.FuelType behavior,
            int priority
    ) {
        this.ingredient = ingredient;
        this.enabled = enabled;
        this.burnTimeTicks = burnTimeTicks;
        this.heatingRate = heatingRate;
        this.maximumTemperature = maximumTemperature;
        this.maximumUnits = maximumUnits;
        this.behavior = behavior;
        this.priority = priority;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public boolean enabled() {
        return enabled;
    }

    public float burnTimeTicks() {
        return burnTimeTicks;
    }

    public float heatingRate() {
        return heatingRate;
    }

    public float maximumTemperature() {
        return maximumTemperature;
    }

    public int maximumUnits() {
        return maximumUnits;
    }

    public MoltenRotorBlockEntity.FuelType behavior() {
        return behavior;
    }

    public int priority() {
        return priority;
    }

    public ResolvedFuel resolve(ResourceLocation recipeId) {
        if (!enabled) {
            return null;
        }

        return new ResolvedFuel(
                recipeId,
                behavior,
                burnTimeTicks,
                heatingRate,
                maximumTemperature,
                maximumUnits
        );
    }

    @Override
    public boolean matches(
            @NotNull SingleRecipeInput input,
            @NotNull Level level
    ) {
        return ingredient.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(
            @NotNull SingleRecipeInput input,
            @NotNull HolderLookup.Provider registries
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(
            @NotNull HolderLookup.Provider registries
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(ingredient);
        return ingredients;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return MoltenRotorFuelRecipeRegistry.SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return MoltenRotorFuelRecipeRegistry.TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
