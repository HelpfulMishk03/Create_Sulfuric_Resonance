package io.hxneyw.repo.content.recipes.sulfuricresonancechamber;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ClassCanBeRecord"})
public final class SulfuricResonanceChamberRecipe
        implements Recipe<SingleRecipeInput> {

    private final Ingredient substrate;
    private final Optional<Ingredient> catalyst;
    private final Optional<Ingredient> auxiliary;
    private final ItemStack result;
    private final int acidAmount;
    private final HeatRequirement minimumHeat;
    private final int minimumSpeed;
    private final int processingTime;

    public SulfuricResonanceChamberRecipe(
            Ingredient substrate,
            Optional<Ingredient> catalyst,
            Optional<Ingredient> auxiliary,
            ItemStack result,
            int acidAmount,
            HeatRequirement minimumHeat,
            int minimumSpeed,
            int processingTime
    ) {
        this.substrate = substrate;
        this.catalyst = catalyst;
        this.auxiliary = auxiliary;
        this.result = result;
        this.acidAmount = acidAmount;
        this.minimumHeat = minimumHeat;
        this.minimumSpeed = minimumSpeed;
        this.processingTime = processingTime;
    }

    public Ingredient substrate() {
        return substrate;
    }

    public Optional<Ingredient> catalyst() {
        return catalyst;
    }

    public Optional<Ingredient> auxiliary() {
        return auxiliary;
    }

    public ItemStack result() {
        return result;
    }

    public int acidAmount() {
        return acidAmount;
    }

    public HeatRequirement minimumHeat() {
        return minimumHeat;
    }

    public int minimumSpeed() {
        return minimumSpeed;
    }

    public int processingTime() {
        return processingTime;
    }

    public boolean matchesInputs(
            ItemStack input1,
            ItemStack input2,
            ItemStack input3
    ) {
        return substrate.test(input1)
                && matchesOptionalSlot(catalyst, input2)
                && matchesOptionalSlot(auxiliary, input3);
    }

    public boolean matchesPresentInputs(
            ItemStack input1,
            ItemStack input2,
            ItemStack input3
    ) {
        return (!input1.isEmpty() || !input2.isEmpty() || !input3.isEmpty())
                && (input1.isEmpty() || substrate.test(input1))
                && (input2.isEmpty()
                || catalyst.filter(value -> value.test(input2)).isPresent())
                && (input3.isEmpty()
                || auxiliary.filter(value -> value.test(input3)).isPresent());
    }

    public boolean acceptsInput(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case 0 -> substrate.test(stack);
            case 1 -> catalyst.filter(value -> value.test(stack)).isPresent();
            case 2 -> auxiliary.filter(value -> value.test(stack)).isPresent();
            default -> false;
        };
    }

    private static boolean matchesOptionalSlot(
            Optional<Ingredient> ingredient,
            ItemStack stack
    ) {
        return ingredient
                .map(value -> value.test(stack))
                .orElseGet(stack::isEmpty);
    }

    @Override
    public boolean matches(
            @NotNull SingleRecipeInput input,
            @NotNull Level level
    ) {
        return substrate.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(
            @NotNull SingleRecipeInput input,
            @NotNull HolderLookup.Provider registries
    ) {
        return result.copy();
    }

    @Override
    public @NotNull ItemStack getResultItem(
            @NotNull HolderLookup.Provider registries
    ) {
        return result.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(substrate);
        catalyst.ifPresent(ingredients::add);
        auxiliary.ifPresent(ingredients::add);
        return ingredients;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SulfuricResonanceChamberRecipeRegistry.SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return SulfuricResonanceChamberRecipeRegistry.TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public enum HeatRequirement {

        HEATED("heated", 1),
        SUPERHEATED("superheated", 3),
        COMBUSTION("combustion", 4);

        private final String serializedName;
        private final int minimumRank;

        HeatRequirement(String serializedName, int minimumRank) {
            this.serializedName = serializedName;
            this.minimumRank = minimumRank;
        }

        public String serializedName() {
            return serializedName;
        }

        public boolean accepts(MoltenRotorBlockEntity.RotorHeatLevel heat) {
            return heat != null && heat.rank >= minimumRank;
        }

        public static HeatRequirement fromSerializedName(String serializedName) {
            for (HeatRequirement value : values()) {
                if (value.serializedName.equals(serializedName)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(
                    "Unknown chamber heat requirement: " + serializedName
            );
        }
    }
}
