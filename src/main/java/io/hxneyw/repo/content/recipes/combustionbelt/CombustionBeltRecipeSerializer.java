package io.hxneyw.repo.content.recipes.combustionbelt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public final class CombustionBeltRecipeSerializer
        implements RecipeSerializer<CombustionBeltRecipe> {

    private static final Codec<
            CombustionBeltRecipe.HeatRequirement
            > HEAT_CODEC =
            Codec.STRING.comapFlatMap(
                    name -> {
                        try {
                            return DataResult.success(
                                    CombustionBeltRecipe
                                            .HeatRequirement
                                            .fromSerializedName(
                                                    name
                                            )
                            );
                        } catch (
                                IllegalArgumentException
                                        exception
                        ) {
                            return DataResult.error(
                                    exception::getMessage
                            );
                        }
                    },
                    CombustionBeltRecipe
                            .HeatRequirement
                            ::serializedName
            );

    public static final MapCodec<CombustionBeltRecipe>
            CODEC =
            RecordCodecBuilder.mapCodec(
                    instance -> instance.group(
                            Ingredient.CODEC
                                    .fieldOf("ingredient")
                                    .forGetter(
                                            CombustionBeltRecipe
                                                    ::ingredient
                                    ),
                            ItemStack.CODEC
                                    .fieldOf("result")
                                    .forGetter(
                                            CombustionBeltRecipe
                                                    ::result
                                    ),
                            HEAT_CODEC
                                    .fieldOf("minimum_heat")
                                    .forGetter(
                                            CombustionBeltRecipe
                                                    ::minimumHeat
                                    ),
                            Codec.intRange(1, 1024)
                                    .fieldOf(
                                            "required_segments"
                                    )
                                    .forGetter(
                                            CombustionBeltRecipe
                                                    ::requiredSegments
                                    ),
                            Codec.intRange(0, 72000)
                                    .optionalFieldOf(
                                            "base_processing_ticks",
                                            0
                                    )
                                    .forGetter(
                                            CombustionBeltRecipe
                                                    ::baseProcessingTicks
                                    ),
                            Codec.intRange(0, 1200)
                                    .optionalFieldOf(
                                            "processing_ticks_per_item",
                                            0
                                    )
                                    .forGetter(
                                            CombustionBeltRecipe
                                                    ::processingTicksPerItem
                                    )
                    ).apply(
                            instance,
                            CombustionBeltRecipe::new
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            CombustionBeltRecipe
            > STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull CombustionBeltRecipe decode(
                        @NotNull RegistryFriendlyByteBuf buffer
                ) {
                    Ingredient ingredient =
                            Ingredient.CONTENTS_STREAM_CODEC
                                    .decode(buffer);

                    ItemStack result =
                            ItemStack.STREAM_CODEC
                                    .decode(buffer);

                    CombustionBeltRecipe.HeatRequirement
                            heat =
                            CombustionBeltRecipe
                                    .HeatRequirement
                                    .fromSerializedName(
                                            buffer.readUtf()
                                    );

                    int requiredSegments =
                            buffer.readVarInt();

                    int baseProcessingTicks =
                            buffer.readVarInt();

                    int processingTicksPerItem =
                            buffer.readVarInt();

                    return new CombustionBeltRecipe(
                            ingredient,
                            result,
                            heat,
                            requiredSegments,
                            baseProcessingTicks,
                            processingTicksPerItem
                    );
                }

                @Override
                public void encode(
                        @NotNull RegistryFriendlyByteBuf buffer,
                        CombustionBeltRecipe recipe
                ) {
                    Ingredient.CONTENTS_STREAM_CODEC
                            .encode(
                                    buffer,
                                    recipe.ingredient()
                            );

                    ItemStack.STREAM_CODEC.encode(
                            buffer,
                            recipe.result()
                    );

                    buffer.writeUtf(
                            recipe.minimumHeat()
                                    .serializedName()
                    );

                    buffer.writeVarInt(
                            recipe.requiredSegments()
                    );

                    buffer.writeVarInt(
                            recipe.baseProcessingTicks()
                    );

                    buffer.writeVarInt(
                            recipe.processingTicksPerItem()
                    );
                }
            };

    @Override
    public @NotNull MapCodec<CombustionBeltRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<
            RegistryFriendlyByteBuf,
            CombustionBeltRecipe
            > streamCodec() {
        return STREAM_CODEC;
    }
}
