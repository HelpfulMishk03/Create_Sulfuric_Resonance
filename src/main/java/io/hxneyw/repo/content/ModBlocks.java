package io.hxneyw.repo.content;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.MoltenRotorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    // NeoForge uses DeferredRegister.Blocks (NOT ForgeRegistries!)
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CreateSulfuricResonance.MODID);

    // NeoForge uses DeferredBlock (NOT RegistryObject!)
    public static final DeferredBlock<Block> MOLTEN_ROTOR_FURNACE =
            BLOCKS.register("molten_rotor_furnace", () ->
                    new MoltenRotorBlock(BlockBehaviour.Properties.of()
                            .strength(5.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        CreateSulfuricResonance.LOGGER.info("Blocks registered for Sulfuric Resonance");
    }
}