package io.hxneyw.repo.content;

import com.mojang.logging.LogUtils;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.items.EmberCatalystItem;
import io.hxneyw.repo.content.items.NetherwoodDustItem;
import io.hxneyw.repo.content.items.PyroclastBombItem;
import io.hxneyw.repo.content.items.SulfurItem;
import io.hxneyw.repo.content.registry.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

public class Items {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CreateSulfuricResonance.MODID);

    public static final DeferredItem<Item> SULFUR = ITEMS.register("sulfur",
            () -> new SulfurItem(new Item.Properties()
                    .stacksTo(64)
                    .fireResistant()
            )
    );

    public static final DeferredItem<Item> NETHERWOOD_DUST = ITEMS.register("netherwood_dust",
            () -> new NetherwoodDustItem(new Item.Properties()
                    .stacksTo(64)
            )
    );

    public static final DeferredItem<Item> SPENT_ASH = ITEMS.register("spent_ash",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );

    public static final DeferredItem<Item> EMBER_CATALYST = ITEMS.register("ember_catalyst",
            () -> new EmberCatalystItem(new Item.Properties() // Changed from Item to EmberCatalystItem
                    .stacksTo(64)
            )
    );
    public static final DeferredItem<Item> EMBERSOL = ITEMS.register("embersol",
            () -> new Item(new Item.Properties() // Changed from Item to EmberCatalystItem
                    .stacksTo(64)
            )
    );
    public static final DeferredItem<Item> BLAZE_SHARD = ITEMS.register("blaze_shard",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .fireResistant()
            )
    );
    public static final DeferredItem<Item> PYROCLASTIC_POWDER = ITEMS.register("pyroclastic_powder",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .fireResistant()
            )
    );
    public static final DeferredItem<Item> PYROCLAST_BOMB = ITEMS.register("pyroclast_bomb",
            () -> new PyroclastBombItem(new Item.Properties()
                    .stacksTo(16)
            )
    );
    public static final DeferredItem<Item> REINFORCED_CINDER_COMPOUND = ITEMS.register("reinforced_cinder_compound",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );
    public static final DeferredItem<Item> OBSIDIAN_FIBER_MOLD = ITEMS.register("obsidian_fiber_mold",
            () -> new Item(new Item.Properties()
                    .stacksTo(16)
            )
    );
    public static final DeferredItem<Item> OBSIDIAN_FIBER_MOLD_FILLED = ITEMS.register("obsidian_fiber_mold_filled",
            () -> new Item(new Item.Properties()
                    .stacksTo(16)
            )
    );
    public static final DeferredItem<Item> OBSIDIAN_FIBER = ITEMS.register("obsidian_fiber",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );
    public static final DeferredItem<Item> LATEX_CLUMP = ITEMS.register("latex_clump",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );
    public static final DeferredItem<Item> UNREFINED_RUBBER = ITEMS.register("unrefined_rubber",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );
    public static final DeferredItem<Item> VULCANIZED_RUBBER = ITEMS.register("vulcanized_rubber",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );
    public static final DeferredItem<Item> INFERNAL_IMPELLER = ITEMS.register("infernal_impeller",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );
    public static final DeferredItem<Item> FLAMEBORNE_CORE = ITEMS.register("flameborne_core",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );

    public static final DeferredItem<Item> MOLTEN_ROTOR_FURNACE = ITEMS.register("molten_rotor_furnace",
            () -> new BlockItem(ModBlocks.MOLTEN_ROTOR_FURNACE.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> CORRUPT_BLAZE_CAKE = ITEMS.register("corrupt_blaze_cake",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );

    public static final DeferredItem<Item> SOUL_FIRED_BLAZE_CAKE = ITEMS.register("soul_fired_blaze_cake",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
            )
    );

    public static void register(IEventBus eventBus) {
        LOGGER.info("Registering Sulfuric Resonance items");
        ITEMS.register(eventBus);
    }
}