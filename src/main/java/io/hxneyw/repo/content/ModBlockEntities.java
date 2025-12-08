package io.hxneyw.repo.content;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    // FIXED: Changed from ForgeRegistries to Registries (NeoForge)
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateSulfuricResonance.MODID);

    // The build() method in 1.21+ doesn't need the DataFixTypes parameter anymore
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MoltenRotorBlockEntity>> MOLTEN_ROTOR =
            BLOCK_ENTITIES.register("molten_rotor", () ->
                    BlockEntityType.Builder.of(MoltenRotorBlockEntity::new,
                            ModBlocks.MOLTEN_ROTOR_FURNACE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
        CreateSulfuricResonance.LOGGER.info("Block entities registered for Sulfuric Resonance");
    }
}