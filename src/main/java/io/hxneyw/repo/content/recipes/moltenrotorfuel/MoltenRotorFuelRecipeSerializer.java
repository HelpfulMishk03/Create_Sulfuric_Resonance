package io.hxneyw.repo.content.recipes.moltenrotorfuel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public final class MoltenRotorFuelRecipeSerializer
        implements RecipeSerializer<MoltenRotorFuelRecipe> {

    private static final Codec<MoltenRotorBlockEntity.FuelType> BEHAVIOR_CODEC =
            Codec.STRING.comapFlatMap(
                    name -> {
                        MoltenRotorBlockEntity.FuelType type =
                                MoltenRotorBlockEntity.FuelType.fromSerializedId(name);
                        return type == MoltenRotorBlockEntity.FuelType.NONE
                                && !"none".equals(name)
                                ? DataResult.error(() ->
                                "Unknown Molten Rotor fuel behavior: " + name)
                                : DataResult.success(type);
                    },
                    type -> type.serializedId
            );

    public static final MapCodec<MoltenRotorFuelRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC
                            .fieldOf("ingredient")
                            .forGetter(MoltenRotorFuelRecipe::ingredient),
                    Codec.BOOL
                            .optionalFieldOf("enabled", true)
                            .forGetter(MoltenRotorFuelRecipe::enabled),
                    Codec.floatRange(0.0F, 1_000_000.0F)
                            .optionalFieldOf("burn_time_ticks", 0.0F)
                            .forGetter(MoltenRotorFuelRecipe::burnTimeTicks),
                    Codec.floatRange(0.0F, 10_000.0F)
                            .optionalFieldOf("heating_rate", 0.0F)
                            .forGetter(MoltenRotorFuelRecipe::heatingRate),
                    Codec.floatRange(0.0F, 100_000.0F)
                            .optionalFieldOf("maximum_temperature", 0.0F)
                            .forGetter(MoltenRotorFuelRecipe::maximumTemperature),
                    Codec.intRange(1, 64)
                            .optionalFieldOf("maximum_units", 1)
                            .forGetter(MoltenRotorFuelRecipe::maximumUnits),
                    BEHAVIOR_CODEC
                            .optionalFieldOf(
                                    "behavior",
                                    MoltenRotorBlockEntity.FuelType.GENERIC_MEDIUM
                            )
                            .forGetter(MoltenRotorFuelRecipe::behavior),
                    Codec.INT
                            .optionalFieldOf("priority", 0)
                            .forGetter(MoltenRotorFuelRecipe::priority)
            ).apply(instance, MoltenRotorFuelRecipe::new));

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            MoltenRotorFuelRecipe
            > STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull MoltenRotorFuelRecipe decode(
                @NotNull RegistryFriendlyByteBuf buffer
        ) {
            return new MoltenRotorFuelRecipe(
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                    buffer.readBoolean(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readVarInt(),
                    MoltenRotorBlockEntity.FuelType.fromSerializedId(
                            buffer.readUtf()
                    ),
                    buffer.readInt()
            );
        }

        @Override
        public void encode(
                @NotNull RegistryFriendlyByteBuf buffer,
                MoltenRotorFuelRecipe recipe
        ) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(
                    buffer,
                    recipe.ingredient()
            );
            buffer.writeBoolean(recipe.enabled());
            buffer.writeFloat(recipe.burnTimeTicks());
            buffer.writeFloat(recipe.heatingRate());
            buffer.writeFloat(recipe.maximumTemperature());
            buffer.writeVarInt(recipe.maximumUnits());
            buffer.writeUtf(recipe.behavior().serializedId);
            buffer.writeInt(recipe.priority());
        }
    };

    @Override
    public @NotNull MapCodec<MoltenRotorFuelRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<
            RegistryFriendlyByteBuf,
            MoltenRotorFuelRecipe
            > streamCodec() {
        return STREAM_CODEC;
    }
}
