package io.hxneyw.repo.content;

import com.mojang.logging.LogUtils;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.items.EmberCatalystItem;
import io.hxneyw.repo.content.items.NetherrackDustItem;
import io.hxneyw.repo.content.items.SulfurItem;
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

    public static final DeferredItem<Item> NETHERRACK_DUST = ITEMS.register("netherrack_dust",
            () -> new NetherrackDustItem(new Item.Properties()
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
    public static void register(IEventBus eventBus) {
        LOGGER.info("Registering Sulfuric Resonance items");
        ITEMS.register(eventBus);
    }
}