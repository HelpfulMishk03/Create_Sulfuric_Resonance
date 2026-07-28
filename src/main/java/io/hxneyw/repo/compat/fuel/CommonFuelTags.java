package io.hxneyw.repo.compat.fuel;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
@SuppressWarnings("unused")
public final class CommonFuelTags {
    public static final TagKey<Item> MINECRAFT_COALS =
            create("minecraft", "coals");

    public static final TagKey<Item> COMMON_WOODEN_RODS =
            create("c", "rods/wooden");

    public static final TagKey<Item> ATM10_TINY_COALS =
            create("atm10", "tiny_coals");

    public static final TagKey<Item> COAL_COKE =
            create("c", "coal_coke");

    private CommonFuelTags() {
    }

    private static TagKey<Item> create(String namespace, String path) {
        return TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(namespace, path)
        );
    }
}
