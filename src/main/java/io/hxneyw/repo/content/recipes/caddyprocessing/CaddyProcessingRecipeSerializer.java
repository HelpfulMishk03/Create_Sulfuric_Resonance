package io.hxneyw.repo.content.recipes.caddyprocessing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class CaddyProcessingRecipeSerializer implements RecipeSerializer<CaddyProcessingRecipe> {
    private static final Codec<CaddyProcessingRecipe.Processor> PROCESSOR_CODEC = Codec.STRING.comapFlatMap(
            id -> {
                try {
                    return DataResult.success(CaddyProcessingRecipe.Processor.fromId(id));
                } catch (IllegalArgumentException exception) {
                    return DataResult.error(exception::getMessage);
                }
            }, CaddyProcessingRecipe.Processor::id);

    public static final MapCodec<CaddyProcessingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PROCESSOR_CODEC.fieldOf("processor").forGetter(CaddyProcessingRecipe::processor),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CaddyProcessingRecipe::ingredient),
            ItemStack.CODEC.fieldOf("result").forGetter(CaddyProcessingRecipe::result),
            Codec.intRange(1, 72_000).fieldOf("processing_time").forGetter(CaddyProcessingRecipe::processingTime),
            ResourceLocation.CODEC.optionalFieldOf("fluid").forGetter(CaddyProcessingRecipe::fluid),
            Codec.intRange(0, 1_000_000).optionalFieldOf("fluid_amount", 0)
                    .forGetter(CaddyProcessingRecipe::fluidAmount),
            Codec.intRange(0, 4).optionalFieldOf("minimum_heat_rank", 0)
                    .forGetter(CaddyProcessingRecipe::minimumHeatRank)
    ).apply(instance, CaddyProcessingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CaddyProcessingRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull CaddyProcessingRecipe decode(@NotNull RegistryFriendlyByteBuf buffer) {
            CaddyProcessingRecipe.Processor processor = CaddyProcessingRecipe.Processor.fromId(buffer.readUtf());
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            int processingTime = buffer.readVarInt();
            Optional<ResourceLocation> fluid = buffer.readBoolean()
                    ? Optional.of(ResourceLocation.STREAM_CODEC.decode(buffer)) : Optional.empty();
            int fluidAmount = buffer.readVarInt();
            int minimumHeatRank = buffer.readVarInt();
            return new CaddyProcessingRecipe(processor, ingredient, result, processingTime, fluid, fluidAmount,
                    minimumHeatRank);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, CaddyProcessingRecipe recipe) {
            buffer.writeUtf(recipe.processor().id());
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
            buffer.writeVarInt(recipe.processingTime());
            buffer.writeBoolean(recipe.fluid().isPresent());
            recipe.fluid().ifPresent(fluid -> ResourceLocation.STREAM_CODEC.encode(buffer, fluid));
            buffer.writeVarInt(recipe.fluidAmount());
            buffer.writeVarInt(recipe.minimumHeatRank());
        }
    };

    @Override public @NotNull MapCodec<CaddyProcessingRecipe> codec() { return CODEC; }
    @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, CaddyProcessingRecipe> streamCodec() { return STREAM_CODEC; }
}
