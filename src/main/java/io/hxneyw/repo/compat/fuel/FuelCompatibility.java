package io.hxneyw.repo.compat.fuel;

import io.hxneyw.repo.compat.fuel.evilcraft.EvilCraftFuelCompatibility;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;
import io.hxneyw.repo.content.registry.ModItemTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public final class FuelCompatibility {
    private static final ResourceLocation EVILCRAFT_DARK_STICK =
            ResourceLocation.fromNamespaceAndPath(
                    "evilcraft",
                    "dark_stick"
            );

    private FuelCompatibility() {
    }

    public static ResolvedFuel resolve(ItemStack stack) {
        if (stack.isEmpty() || isExplicitlyRejected(stack)) {
            return null;
        }

        ResolvedFuel evilCraft =
                EvilCraftFuelCompatibility.resolve(stack);

        if (evilCraft != null) {
            return evilCraft;
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_SOUL_FIRED)) {
            return ResolvedFuel.fromType(
                    FuelType.SOUL_FIRED_BLAZE_CAKE
            );
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_BLAZE_CAKE)) {
            return ResolvedFuel.fromType(FuelType.BLAZE_CAKE);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_EXPLOSIVE)) {
            return ResolvedFuel.fromType(FuelType.TNT);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_DENSE_FUEL)) {
            return ResolvedFuel.fromType(FuelType.COAL_BLOCK);
        }

        if (stack.is(Items.CINDER_FUEL_BRIQUETTE.get())) {
            return new ResolvedFuel(
                    FuelType.CHARCOAL,
                    FuelType.CHARCOAL.baseBurnTimeTicks * 1.5F,
                    FuelType.CHARCOAL.celsiusPerSecond,
                    FuelType.CHARCOAL.maxTempReachable,
                    FuelType.CHARCOAL.maxStackSize
            );
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_CHARCOAL)) {
            return ResolvedFuel.fromType(FuelType.CHARCOAL);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_COAL)
                || stack.is(ItemTags.COALS)
                || stack.is(CommonFuelTags.COAL_COKE)) {
            return ResolvedFuel.fromType(FuelType.COAL);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_KELP)) {
            return ResolvedFuel.fromType(FuelType.KELP_BLOCK);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_LOGS)
                || stack.is(ItemTags.LOGS_THAT_BURN)
                || stack.is(ItemTags.BAMBOO_BLOCKS)
                || stack.is(ItemTags.PLANKS)
                || stack.is(ItemTags.WOODEN_STAIRS)
                || stack.is(ItemTags.WOODEN_FENCES)
                || stack.is(ItemTags.FENCE_GATES)
                || stack.is(ItemTags.WOODEN_TRAPDOORS)
                || stack.is(ItemTags.WOODEN_DOORS)) {
            return ResolvedFuel.fromType(FuelType.LOG);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_STICKS)) {
            return ResolvedFuel.fromType(FuelType.STICK);
        }

        int furnaceBurnTime =
                stack.getBurnTime(RecipeType.SMELTING);

        if (furnaceBurnTime <= 0) {
            return null;
        }

        if (!isStructurallySuitableFuel(stack)) {
            return null;
        }

        if (furnaceBurnTime <= 400) {
            return ResolvedFuel.fromType(FuelType.GENERIC_LOW);
        }

        if (furnaceBurnTime <= 2000) {
            return ResolvedFuel.fromType(FuelType.GENERIC_MEDIUM);
        }

        return ResolvedFuel.fromType(FuelType.GENERIC_HIGH);
    }

    private static boolean isExplicitlyRejected(ItemStack stack) {
        return EVILCRAFT_DARK_STICK.equals(
                BuiltInRegistries.ITEM.getKey(stack.getItem())
        );
    }

    private static boolean isStructurallySuitableFuel(ItemStack stack) {
        return stack.is(ItemTags.LOGS_THAT_BURN)
                || stack.is(ItemTags.BAMBOO_BLOCKS)
                || stack.is(ItemTags.PLANKS)
                || stack.is(ItemTags.WOODEN_STAIRS)
                || stack.is(ItemTags.WOODEN_FENCES)
                || stack.is(ItemTags.FENCE_GATES)
                || stack.is(ItemTags.WOODEN_TRAPDOORS)
                || stack.is(ItemTags.WOODEN_DOORS)
                || stack.is(ItemTags.COALS)
                || stack.is(CommonFuelTags.COAL_COKE)
                || stack.is(ModItemTags.MOLTEN_ROTOR_LOGS)
                || stack.is(ModItemTags.MOLTEN_ROTOR_STICKS);
    }
}