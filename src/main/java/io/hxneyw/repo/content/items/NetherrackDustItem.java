package io.hxneyw.repo.content.items;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NetherrackDustItem extends Item {
    public NetherrackDustItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(@NotNull ItemStack stack, @NotNull ItemEntity entity) {
        // No special behavior - just a regular item
        return false;
    }
}