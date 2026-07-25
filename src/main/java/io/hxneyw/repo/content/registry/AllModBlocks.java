package io.hxneyw.repo.content.registry;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.RubberPaddingBlock;
import io.hxneyw.repo.content.blocks.SulfuricAcidBlock;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public class AllModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("sulfuricresonance");
   public static final DeferredBlock<Block> MOLTEN_ROTOR_FURNACE = BLOCKS.register(
      "molten_rotor_furnace",
      () -> new MoltenRotorBlock(
         Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5.0F, 6.0F)
            .requiresCorrectToolForDrops()
            .sound(SoundType.METAL)
            .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            .noOcclusion()
            .lightLevel(state -> {
               HeatLevel heat = (HeatLevel)state.getValue(MoltenRotorBlock.HEAT_LEVEL);

               return switch (heat) {
                  case NONE -> 0;
                  case SMOULDERING, FADING -> 8;
                  case KINDLED -> 12;
                  case SEETHING -> 15;
                  default -> throw new MatchException(null, null);
               };
            })
      )
   );
   public static final DeferredBlock<Block> RUBBER_PADDING = BLOCKS.register(
      "rubber_padding",
      () -> new RubberPaddingBlock(
         Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .strength(1.5F, 6.0F)
            .requiresCorrectToolForDrops()
            .sound(SoundType.WOOL)
            .instrument(NoteBlockInstrument.BASS)
            .noOcclusion()
            .dynamicShape()
            .isValidSpawn((state, level, pos, entityType) -> false)
            .isSuffocating((state, level, pos) -> false)
      )
   );
   public static final DeferredBlock<Block> SULFUR_BLOCK = BLOCKS.register(
      "sulfur_block",
      () -> new Block(
         Properties.of()
            .mapColor(MapColor.COLOR_YELLOW)
            .strength(1.0F, 2.0F)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE)
            .instrument(NoteBlockInstrument.HARP)
      )
   );
   public static final DeferredBlock<LiquidBlock> SULFURIC_ACID_BLOCK = BLOCKS.register(
      "sulfuric_acid",
      () -> new SulfuricAcidBlock(
         (FlowingFluid)AllModFluids.SULFURIC_ACID.get(),
         Properties.of()
            .mapColor(MapColor.COLOR_YELLOW)
            .replaceable()
            .noCollission()
            .randomTicks()
            .strength(100.0F)
            .pushReaction(PushReaction.DESTROY)
            .noLootTable()
            .liquid()
      )
   );
   public static final DeferredBlock<Block> PERFORATED_SPRITZER = BLOCKS.register(
      "perforated_spritzer", () -> new PerforatedSpritzerBlock(Properties.of().strength(3.0F).sound(SoundType.METAL).noOcclusion().lightLevel(state -> 0))
   );

   public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
      CreateSulfuricResonance.LOGGER.info("Blocks registered for Sulfuric Resonance");
   }
}
