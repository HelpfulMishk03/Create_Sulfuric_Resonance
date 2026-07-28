package io.hxneyw.repo.compat.fuel.evilcraft;

import io.hxneyw.repo.compat.fuel.ResolvedFuel;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class EvilCraftFuelCompatibility {
    private static final ResourceLocation REINFORCED_UNDEAD_PLANK =
            ResourceLocation.fromNamespaceAndPath(
                    "evilcraft",
                    "reinforced_undead_planks"
            );

    private EvilCraftFuelCompatibility() {
    }

    public static ResolvedFuel resolve(ItemStack stack) {
        ResourceLocation itemId =
                BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (REINFORCED_UNDEAD_PLANK.equals(itemId)) {
            return new ResolvedFuel(
                    FuelType.GENERIC_LOW,
                    1000.0F,
                    9.0F,
                    600.0F,
                    16
            );
        }

        return null;
    }
}
