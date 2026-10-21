package io.hxneyw.repo.content.recipes.caddyprocessing;

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

import java.util.Optional;

public final class CaddyProcessingRecipe implements Recipe<SingleRecipeInput> {
    private final Processor processor;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int processingTime;
    private final Optional<ResourceLocation> fluid;
    private final int fluidAmount;
    private final int minimumHeatRank;

    public CaddyProcessingRecipe(Processor processor, Ingredient ingredient, ItemStack result,
                                 int processingTime, Optional<ResourceLocation> fluid, int fluidAmount,
                                 int minimumHeatRank) {
        this.processor = processor;
        this.ingredient = ingredient;
        this.result = result;
        this.processingTime = processingTime;
        this.fluid = fluid;
        this.fluidAmount = fluidAmount;
        this.minimumHeatRank = minimumHeatRank;
    }

    public Processor processor() { return processor; }
    public Ingredient ingredient() { return ingredient; }
    public ItemStack result() { return result.copy(); }
    public int processingTime() { return processingTime; }
    public Optional<ResourceLocation> fluid() { return fluid; }
    public int fluidAmount() { return fluidAmount; }
    public int minimumHeatRank() { return minimumHeatRank; }

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input,
                                       @NotNull HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(ingredient);
        return ingredients;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height > 0; }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return CaddyProcessingRecipeRegistry.SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return CaddyProcessingRecipeRegistry.TYPE.get();
    }

    @Override
    public boolean isSpecial() { return true; }

    public enum Processor {
        PRESS("press"), SAW("saw"), SPOUT("spout"),
        FAN_BLASTING("fan_blasting"), FAN_HAUNTING("fan_haunting"),
        FAN_SMOKING("fan_smoking"), FAN_SPLASHING("fan_splashing"), FAN_COOLING("fan_cooling"),
        COMBUSTION_BELT("combustion_belt");

        private final String id;

        Processor(String id) { this.id = id; }

        public String id() { return id; }

        public static Processor fromId(String id) {
            for (Processor processor : values()) {
                if (processor.id.equals(id)) return processor;
            }
            throw new IllegalArgumentException("Unknown Caddy processor: " + id);
        }
    }
}
