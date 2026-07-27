package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("DataFlowIssue")
public class AllBlockEntities {
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "sulfuricresonance");
   public static final DeferredHolder<
           BlockEntityType<?>,
           BlockEntityType<MoltenRotorBlockEntity>
           > MOLTEN_ROTOR = BLOCK_ENTITIES.register(
           "molten_rotor",
           () -> Builder.of(
                   MoltenRotorBlockEntity::new,
                   AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
           ).build(null)
   );
   public static final DeferredHolder<
           BlockEntityType<?>,
           BlockEntityType<PerforatedSpritzerBlockEntity>
           > PERFORATED_SPRITZER = BLOCK_ENTITIES.register(
           "perforated_spritzer",
           () -> Builder.of(
                   PerforatedSpritzerBlockEntity::new,
                   AllModBlocks.PERFORATED_SPRITZER.get()
           ).build(null)
   );

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
      eventBus.addListener(AllBlockEntities::registerCapabilities);
      CreateSulfuricResonance.LOGGER.info("Block entities registered for Sulfuric Resonance");
   }

   private static void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.registerBlockEntity(FluidHandler.BLOCK, PERFORATED_SPRITZER.get(), (blockEntity, side) -> {
         if (side != Direction.UP) {
            return null;
         } else {
            if (blockEntity.fluidCapability == null) {
               blockEntity.refreshCapability();
            }

            return blockEntity.fluidCapability;
         }
      });
      CreateSulfuricResonance.LOGGER.info("Registered capabilities for block entities");
   }
}
