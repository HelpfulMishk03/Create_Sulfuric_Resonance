package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateSulfuricResonance.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MoltenRotorBlockEntity>> MOLTEN_ROTOR =
            BLOCK_ENTITIES.register("molten_rotor", () ->
                    BlockEntityType.Builder.of(MoltenRotorBlockEntity::new,
                            ModBlocks.MOLTEN_ROTOR_FURNACE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);

        // CRITICAL: Register capability listener on the MOD event bus
        eventBus.addListener(ModBlockEntities::registerCapabilities);

        CreateSulfuricResonance.LOGGER.info("Block entities registered for Sulfuric Resonance");
    }

    /**
     * FIXED: Register ItemHandler capability for funnels and hoppers
     * This is called automatically during the capability registration event
     */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,  // The capability type
                MOLTEN_ROTOR.get(),              // Your block entity type
                (blockEntity, side) -> blockEntity.fuelHandler  // Expose the fuel handler from all sides
        );

        CreateSulfuricResonance.LOGGER.info("Registered ItemHandler capability for Molten Rotor");
    }
}