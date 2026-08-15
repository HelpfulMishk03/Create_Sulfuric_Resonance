package io.hxneyw.repo.content.recipes.sulfuricresonancechamber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public final class SulfuricResonanceChamberRecipeSerializer
        implements RecipeSerializer<SulfuricResonanceChamberRecipe> {

    private static final Codec<
            SulfuricResonanceChamberRecipe.HeatRequirement
            > HEAT_CODEC = Codec.STRING.comapFlatMap(
                    name -> {
                        try {
                            return DataResult.success(
                                    SulfuricResonanceChamberRecipe
                                            .HeatRequirement
                                            .fromSerializedName(name)
                            );
                        } catch (IllegalArgumentException exception) {
                            return DataResult.error(exception::getMessage);
                        }
                    },
                    SulfuricResonanceChamberRecipe.HeatRequirement
                            ::serializedName
            );

    public static final MapCodec<SulfuricResonanceChamberRecipe>
            CODEC = RecordCodecBuilder.mapCodec(
                    instance -> instance.group(
                            Ingredient.CODEC
                                    .fieldOf("substrate")
                                    .forGetter(
                                            SulfuricResonanceChamberRecipe
                                                    ::substrate
                                    ),
                            Ingredient.CODEC
                                    .optionalFieldOf("catalyst")
                                    .forGetter(
                                            SulfuricResonanceChamberRecipe
                                                    ::catalyst
                                    ),
                            Ingredient.CODEC
                                    .optionalFieldOf("auxiliary")
                                    .forGetter(
                                            SulfuricResonanceChamberRecipe
                                                    ::auxiliary
                                    ),
                            ItemStack.CODEC
                                    .fieldOf("result")
                                    .forGetter(
                                            SulfuricResonanceChamberRecipe
                                                    ::result
                                    ),
                            Codec.intRange(1, 1500)
                                    .fieldOf("acid_amount")
                                    .forGetter(
                                            SulfuricResonanceChamberRecipe
                                                    ::acidAmount
                                    ),
                            HEAT_CODEC
                                    .fieldOf("minimum_heat")
                                    .forGetter(
                                            SulfuricResonanceChamberRecipe
                                                    ::minimumHeat
                                    ),
                            Codec.intRange(1, 256)
                                    .fieldOf("minimum_speed")
                                    .forGetter(
                                            SulfuricResonanceChamberRecipe
                                                    ::minimumSpeed
                                    ),
                            Codec.intRange(1, 72000)
                                    .fieldOf("processing_time")
                                    .forGetter(
                                            SulfuricResonanceChamberRecipe
                                                    ::processingTime
                                    )
                    ).apply(
                            instance,
                            SulfuricResonanceChamberRecipe::new
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            SulfuricResonanceChamberRecipe
            > STREAM_CODEC = new StreamCodec<>() {

        @Override
        public @NotNull SulfuricResonanceChamberRecipe decode(
                @NotNull RegistryFriendlyByteBuf buffer
        ) {
            Ingredient substrate =
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);

            Optional<Ingredient> catalyst = buffer.readBoolean()
                    ? Optional.of(
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)
            )
                    : Optional.empty();

            Optional<Ingredient> auxiliary = buffer.readBoolean()
                    ? Optional.of(
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)
            )
                    : Optional.empty();

            return new SulfuricResonanceChamberRecipe(
                    substrate,
                    catalyst,
                    auxiliary,
                    ItemStack.STREAM_CODEC.decode(buffer),
                    buffer.readVarInt(),
                    SulfuricResonanceChamberRecipe
                            .HeatRequirement
                            .fromSerializedName(buffer.readUtf()),
                    buffer.readVarInt(),
                    buffer.readVarInt()
            );
        }

        @Override
        public void encode(
                @NotNull RegistryFriendlyByteBuf buffer,
                SulfuricResonanceChamberRecipe recipe
        ) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(
                    buffer,
                    recipe.substrate()
            );

            buffer.writeBoolean(recipe.catalyst().isPresent());
            recipe.catalyst().ifPresent(ingredient ->
                    Ingredient.CONTENTS_STREAM_CODEC.encode(
                            buffer,
                            ingredient
                    )
            );

            buffer.writeBoolean(recipe.auxiliary().isPresent());
            recipe.auxiliary().ifPresent(ingredient ->
                    Ingredient.CONTENTS_STREAM_CODEC.encode(
                            buffer,
                            ingredient
                    )
            );

            ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
            buffer.writeVarInt(recipe.acidAmount());
            buffer.writeUtf(recipe.minimumHeat().serializedName());
            buffer.writeVarInt(recipe.minimumSpeed());
            buffer.writeVarInt(recipe.processingTime());
        }
    };

    @Override
    public @NotNull MapCodec<SulfuricResonanceChamberRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<
            RegistryFriendlyByteBuf,
            SulfuricResonanceChamberRecipe
            > streamCodec() {
        return STREAM_CODEC;
    }
}
