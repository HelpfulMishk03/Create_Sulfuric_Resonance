package io.hxneyw.repo.content.registry;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.MoltenRotorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CreateSulfuricResonance.MODID);

    public static final DeferredBlock<Block> MOLTEN_ROTOR_FURNACE =
            BLOCKS.register("molten_rotor_furnace", () ->
                    new MoltenRotorBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5.0f, 6.0f)
                            .requiresCorrectToolForDrops()  // CRITICAL LINE - requires iron pickaxe
                            .sound(SoundType.METAL)
                            .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.IRON_XYLOPHONE)
                            .noOcclusion()
                            .lightLevel(state -> {
                                // Map heat levels to light values (matching your MoltenRotorBlock.getLightEmission)
                                BlazeBurnerBlock.HeatLevel heat = state.getValue(MoltenRotorBlock.HEAT_LEVEL);
                                return switch (heat) {
                                    case NONE -> 0;
                                    case SMOULDERING, FADING -> 8;
                                    case KINDLED -> 12;
                                    case SEETHING -> 15;
                                };
                            })
                    ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        CreateSulfuricResonance.LOGGER.info("Blocks registered for Sulfuric Resonance");
    }
}