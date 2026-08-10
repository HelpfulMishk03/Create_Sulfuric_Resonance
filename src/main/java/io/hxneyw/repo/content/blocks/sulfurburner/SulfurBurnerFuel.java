package io.hxneyw.repo.content.blocks.sulfurburner;

import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SulfurBurnerFuel {

    public static final int SULFUR_BURN_TICKS = 400;
    public static final int SULFUR_BLOCK_BURN_TICKS = 3600;
    public static final int BRIQUETTE_BURN_TICKS = 1200;

    private static final TagKey<Item> SULFUR =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            "c",
                            "sulfur"
                    )
            );

    private SulfurBurnerFuel() {
    }

    public static boolean isFuel(ItemStack stack) {
        return getBurnTicks(stack) > 0;
    }

    public static int getBurnTicks(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        if (stack.is(Items.SULFUR_FUEL_BRIQUETTE.get())) {
            return BRIQUETTE_BURN_TICKS;
        }

        if (stack.is(
                AllModBlocks.SULFUR_BLOCK.get().asItem()
        )) {
            return SULFUR_BLOCK_BURN_TICKS;
        }

        if (stack.is(SULFUR)) {
            return SULFUR_BURN_TICKS;
        }

        return 0;
    }
}