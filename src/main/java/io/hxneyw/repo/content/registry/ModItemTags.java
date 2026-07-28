package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {

    public static final TagKey<Item> MOLTEN_ROTOR_STICKS =
            create("molten_rotor_fuels/sticks");

    public static final TagKey<Item> MOLTEN_ROTOR_LOGS =
            create("molten_rotor_fuels/logs");

    public static final TagKey<Item> MOLTEN_ROTOR_COAL =
            create("molten_rotor_fuels/coal");

    public static final TagKey<Item> MOLTEN_ROTOR_CHARCOAL =
            create("molten_rotor_fuels/charcoal");

    public static final TagKey<Item> MOLTEN_ROTOR_DENSE_FUEL =
            create("molten_rotor_fuels/dense_fuel");

    public static final TagKey<Item> MOLTEN_ROTOR_KELP =
            create("molten_rotor_fuels/kelp");

    public static final TagKey<Item> MOLTEN_ROTOR_EXPLOSIVE =
            create("molten_rotor_fuels/explosive");

    public static final TagKey<Item> MOLTEN_ROTOR_BLAZE_CAKE =
            create("molten_rotor_fuels/blaze_cake");

    public static final TagKey<Item> MOLTEN_ROTOR_SOUL_FIRED =
            create("molten_rotor_fuels/soul_fired");

    private ModItemTags() {
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(
                        CreateSulfuricResonance.MODID,
                        path
                )
        );
    }
}