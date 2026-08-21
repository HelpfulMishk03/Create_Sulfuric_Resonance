package io.hxneyw.repo.content.recipes.precisionspraying;

import net.minecraft.world.item.ItemStack;

public record PrecisionSprayingDisplay(
        ItemStack input,
        ItemStack output,
        int fluidAmount
) {
}
