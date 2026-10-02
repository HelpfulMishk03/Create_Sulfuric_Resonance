package io.hxneyw.repo.content.recipes.rotaryleaching;

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

public final class RotaryLeachingRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient ingredient;
    private final int inputCount;
    private final ResourceLocation fluid;
    private final int fluidAmount;
    private final ItemStack result;
    private final ItemStack byproduct;
    private final int minimumSpeed;
    private final int processingTime;

    public RotaryLeachingRecipe(Ingredient ingredient, int inputCount, ResourceLocation fluid, int fluidAmount, ItemStack result, ItemStack byproduct, int minimumSpeed, int processingTime) {
        this.ingredient = ingredient;
        this.inputCount = inputCount;
        this.fluid = fluid;
        this.fluidAmount = fluidAmount;
        this.result = result;
        this.byproduct = byproduct;
        this.minimumSpeed = minimumSpeed;
        this.processingTime = processingTime;
    }

    public Ingredient ingredient() { return ingredient; }
    public int inputCount() { return inputCount; }
    public ResourceLocation fluid() { return fluid; }
    public int fluidAmount() { return fluidAmount; }
    public ItemStack result() { return result.copy(); }
    public ItemStack byproduct() { return byproduct.copy(); }
    public int minimumSpeed() { return minimumSpeed; }
    public int processingTime() { return processingTime; }

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) { return ingredient.test(input.item()); }
    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, @NotNull HolderLookup.Provider registries) { return result.copy(); }
    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) { return result.copy(); }
    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(ingredient);
        return ingredients;
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height > 0; }
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return RotaryLeachingRecipeRegistry.SERIALIZER.get(); }
    @Override
    public @NotNull RecipeType<?> getType() { return RotaryLeachingRecipeRegistry.TYPE.get(); }
    @Override
    public boolean isSpecial() { return true; }
}
