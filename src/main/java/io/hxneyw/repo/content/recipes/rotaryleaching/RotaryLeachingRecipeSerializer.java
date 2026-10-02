package io.hxneyw.repo.content.recipes.rotaryleaching;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public final class RotaryLeachingRecipeSerializer implements RecipeSerializer<RotaryLeachingRecipe> {
    public static final MapCodec<RotaryLeachingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(RotaryLeachingRecipe::ingredient),
            Codec.intRange(1, 64).fieldOf("input_count").forGetter(RotaryLeachingRecipe::inputCount),
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(RotaryLeachingRecipe::fluid),
            Codec.intRange(1, 1_000_000).fieldOf("fluid_amount").forGetter(RotaryLeachingRecipe::fluidAmount),
            ItemStack.CODEC.fieldOf("result").forGetter(RotaryLeachingRecipe::result),
            ItemStack.CODEC.fieldOf("byproduct").forGetter(RotaryLeachingRecipe::byproduct),
            Codec.intRange(1, 256).fieldOf("minimum_speed").forGetter(RotaryLeachingRecipe::minimumSpeed),
            Codec.intRange(1, 72_000).fieldOf("processing_time").forGetter(RotaryLeachingRecipe::processingTime)
    ).apply(instance, RotaryLeachingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RotaryLeachingRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull RotaryLeachingRecipe decode(@NotNull RegistryFriendlyByteBuf buffer) {
            return new RotaryLeachingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), buffer.readVarInt(), ResourceLocation.STREAM_CODEC.decode(buffer), buffer.readVarInt(), ItemStack.STREAM_CODEC.decode(buffer), ItemStack.STREAM_CODEC.decode(buffer), buffer.readVarInt(), buffer.readVarInt());
        }
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, RotaryLeachingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
            buffer.writeVarInt(recipe.inputCount());
            ResourceLocation.STREAM_CODEC.encode(buffer, recipe.fluid());
            buffer.writeVarInt(recipe.fluidAmount());
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
            ItemStack.STREAM_CODEC.encode(buffer, recipe.byproduct());
            buffer.writeVarInt(recipe.minimumSpeed());
            buffer.writeVarInt(recipe.processingTime());
        }
    };

    @Override
    public @NotNull MapCodec<RotaryLeachingRecipe> codec() { return CODEC; }
    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, RotaryLeachingRecipe> streamCodec() { return STREAM_CODEC; }
}
