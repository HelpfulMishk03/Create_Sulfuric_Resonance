package io.hxneyw.repo.content;

import com.mojang.logging.LogUtils;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.items.EmberCatalystItem;
import io.hxneyw.repo.content.items.NetherwoodDustItem;
import io.hxneyw.repo.content.items.PyroclastBombItem;
import io.hxneyw.repo.content.items.SulfurItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import java.util.List;

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
            () -> new BlockItem(ModBlocks.MOLTEN_ROTOR_FURNACE.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                            @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
                    // Intentionally empty - prevents default BlockItem tooltip that adds mod name twice
                    // The game will still add mod name once automatically from the creative tab
                }
            }
    );

    public static void register(IEventBus eventBus) {
        LOGGER.info("Registering Sulfuric Resonance items");
        ITEMS.register(eventBus);
    }
}