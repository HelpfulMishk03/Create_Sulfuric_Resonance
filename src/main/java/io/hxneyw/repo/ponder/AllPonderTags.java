package io.hxneyw.repo.ponder;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class AllPonderTags {
    public static final ResourceLocation FLUIDS = id("fluids");
    public static final ResourceLocation REACTIVE_HEAT = id("reactive_heat");
    public static final ResourceLocation SULFUR_CHEMISTRY = id("sulfur_chemistry");
    public static final ResourceLocation INTELLIGENT_INDUSTRY = id("intelligent_industry");

    private AllPonderTags() {
    }

    public static void register(
            PonderTagRegistrationHelper<ResourceLocation> helper
    ) {
        helper.registerTag(FLUIDS)
                .addToIndex()
                .item(AllModBlocks.PERFORATED_SPRITZER.get(), true, false)
                .title("Fluid Handling")
                .description("Move, store, spray, and consume Sulfuric Resonance fluids")
                .register();

        helper.registerTag(REACTIVE_HEAT)
                .addToIndex()
                .item(AllModBlocks.MOLTEN_ROTOR_FURNACE.get(), true, false)
                .title("Reactive Heat")
                .description("Generate, route, transform, and use thermochemical heat")
                .register();

        helper.registerTag(SULFUR_CHEMISTRY)
                .addToIndex()
                .item(AllModBlocks.SULFUR_BURNER.get(), true, false)
                .title("Sulfur Chemistry")
                .description("Process sulfur, acid-resistant materials, and resonance chemistry")
                .register();

        helper.registerTag(INTELLIGENT_INDUSTRY)
                .addToIndex()
                .item(AllModBlocks.PROCESS_MONITOR.get(), true, false)
                .title("Intelligent Industry")
                .description("Observe factory state, link devices, and turn conditions into action")
                .register();

        helper.addToTag(FLUIDS)
                .add(blockKey(AllModBlocks.PERFORATED_SPRITZER.get()))
                .add(itemKey(Items.PRECISION_SPRITZER.get()))
                .add(itemKey(Items.SULFURIC_ACID_BUCKET.get()))
                .add(blockKey(AllModBlocks.ASH_CERAMIC_CRUCIBLE.get()))
                .add(blockKey(AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()));

        helper.addToTag(REACTIVE_HEAT)
                .add(blockKey(AllModBlocks.MOLTEN_ROTOR_FURNACE.get()))
                .add(blockKey(AllModBlocks.THERMOCHEMICAL_SHAFT.get()))
                .add(blockKey(AllModBlocks.THERMOCHEMICAL_CONDUIT.get()))
                .add(blockKey(AllModBlocks.THERMOCHEMICAL_GEARBOX.get()))
                .add(blockKey(AllModBlocks.PARALLEL_THERMOCHEMICAL_GEARBOX.get()))
                .add(blockKey(AllModBlocks.THERMOCHEMICAL_COGWHEEL.get()))
                .add(blockKey(AllModBlocks.LARGE_THERMOCHEMICAL_COGWHEEL.get()))
                .add(itemKey(Items.COMBUSTION_BELT_CONNECTOR.get()))
                .add(blockKey(AllModBlocks.SULFUR_BURNER.get()))
                .add(blockKey(AllModBlocks.RESONANT_HEAT_INJECTOR.get()))
                .add(blockKey(AllModBlocks.THERMOCHEMICAL_LINK_DRIVE.get()))
                .add(blockKey(AllModBlocks.THERMAL_RELAY_SWITCH.get()))
                .add(blockKey(AllModBlocks.THERMAL_GAUGE.get()))
                .add(blockKey(AllModBlocks.LIVING_EMBER_LAMP.get()))
                .add(blockKey(AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()))
                .add(blockKey(AllModBlocks.THERMAL_WARNING_ALARM.get()));

        helper.addToTag(SULFUR_CHEMISTRY)
                .add(itemKey(Items.SULFUR.get()))
                .add(itemKey(Items.SULFURIC_ACID_BUCKET.get()))
                .add(blockKey(AllModBlocks.SULFUR_BURNER.get()))
                .add(blockKey(AllModBlocks.PERFORATED_SPRITZER.get()))
                .add(blockKey(AllModBlocks.ASH_CERAMIC_CRUCIBLE.get()))
                .add(blockKey(AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()));

        helper.addToTag(INTELLIGENT_INDUSTRY)
                .add(itemKey(Items.PRECISION_SPRITZER.get()))
                .add(blockKey(AllModBlocks.RUBBER_PADDING.get()))
                .add(blockKey(AllModBlocks.PROCESS_MONITOR.get()))
                .add(blockKey(AllModBlocks.PROCESS_GAUGE.get()))
                .add(blockKey(AllModBlocks.THERMAL_WARNING_ALARM.get()))
                .add(blockKey(AllModBlocks.THERMAL_RELAY_SWITCH.get()))
                .add(blockKey(AllModBlocks.THERMAL_GAUGE.get()))
                .add(blockKey(AllModBlocks.LIVING_EMBER_LAMP.get()))
                .add(blockKey(AllModBlocks.THERMOCHEMICAL_LINK_DRIVE.get()))
                .add(blockKey(AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()));

        helper.addToTag(ResourceLocation.parse("create:fluids"))
                .add(blockKey(AllModBlocks.PERFORATED_SPRITZER.get()))
                .add(itemKey(Items.PRECISION_SPRITZER.get()))
                .add(itemKey(Items.SULFURIC_ACID_BUCKET.get()));
    }

    private static ResourceLocation blockKey(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    private static ResourceLocation itemKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                CreateSulfuricResonance.MODID,
                path
        );
    }
}
