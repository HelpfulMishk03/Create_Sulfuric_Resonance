package io.hxneyw.repo.content.recipes;

import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeTypes {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, CreateSulfuricResonance.MODID);

    private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER =
            DeferredRegister.create(Registries.RECIPE_TYPE, CreateSulfuricResonance.MODID);

    // Recipe Type
    public static final DeferredHolder<RecipeType<?>, RecipeType<CombustionMixingRecipe>> COMBUSTION_MIXING =
            TYPE_REGISTER.register("combustion_mixing", () -> {
                CreateSulfuricResonance.LOGGER.info(">>> REGISTERING RECIPE TYPE: combustion_mixing");
                return RecipeType.simple(ResourceLocation.fromNamespaceAndPath(CreateSulfuricResonance.MODID, "combustion_mixing"));
            });

    // Recipe Serializer
    public static final DeferredHolder<RecipeSerializer<?>, StandardProcessingRecipe.Serializer<CombustionMixingRecipe>> COMBUSTION_MIXING_SERIALIZER =
            SERIALIZER_REGISTER.register("combustion_mixing", () -> {
                CreateSulfuricResonance.LOGGER.info(">>> REGISTERING RECIPE SERIALIZER: combustion_mixing");
                return new StandardProcessingRecipe.Serializer<>(params -> {
                    CreateSulfuricResonance.LOGGER.info("========================================");
                    CreateSulfuricResonance.LOGGER.info(">>> CREATING COMBUSTION MIXING RECIPE!");
                    CreateSulfuricResonance.LOGGER.info("========================================");
                    return new CombustionMixingRecipe(params);
                });
            });

    // IRecipeTypeInfo for Create integration
    public static final IRecipeTypeInfo COMBUSTION_MIXING_TYPE_INFO = new IRecipeTypeInfo() {
        @Override
        public ResourceLocation getId() {
            return ResourceLocation.fromNamespaceAndPath(CreateSulfuricResonance.MODID, "combustion_mixing");
        }


        @Override
        public RecipeType<?> getType() {
            return COMBUSTION_MIXING.get();
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return COMBUSTION_MIXING_SERIALIZER.get();
        }
    };

    public static void register(IEventBus modEventBus) {
        CreateSulfuricResonance.LOGGER.info("========================================1");
        CreateSulfuricResonance.LOGGER.info(">>> ModRecipeTypes.register() CALLED");
        CreateSulfuricResonance.LOGGER.info("========================================1");

        SERIALIZER_REGISTER.register(modEventBus);
        TYPE_REGISTER.register(modEventBus);

        CreateSulfuricResonance.LOGGER.info("========================================1");
        CreateSulfuricResonance.LOGGER.info("✓✓✓ RECIPE TYPES REGISTERED ✓✓✓");
        CreateSulfuricResonance.LOGGER.info("========================================1");
    }
}