package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.crucible.AshCeramicCrucibleBlockEntity;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.sulfurburner.SulfurBurnerBlockEntity;
import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalcogwheel.ThermochemicalCogwheelBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalConduitBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalgearbox.ThermochemicalGearboxBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalshaft.ThermochemicalShaftBlockEntity;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
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
            BlockEntityType<ThermochemicalCogwheelBlockEntity>
            > THERMOCHEMICAL_COGWHEEL =
            BLOCK_ENTITIES.register(
                    "thermochemical_cogwheel",
                    () -> Builder.of(
                            ThermochemicalCogwheelBlockEntity::new,
                            AllModBlocks.THERMOCHEMICAL_COGWHEEL.get(),
                            AllModBlocks.LARGE_THERMOCHEMICAL_COGWHEEL.get()
                    ).build(null)
            );

   public static final DeferredHolder<
           BlockEntityType<?>,
           BlockEntityType<AshCeramicCrucibleBlockEntity>
           > ASH_CERAMIC_CRUCIBLE = BLOCK_ENTITIES.register(
           "ash_ceramic_crucible",
           () -> Builder.of(
                   AshCeramicCrucibleBlockEntity::new,
                   AllModBlocks.ASH_CERAMIC_CRUCIBLE.get()
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

   public static final DeferredHolder<
           BlockEntityType<?>,
           BlockEntityType<LivingEmberLampBlockEntity>
           > LIVING_EMBER_LAMP = BLOCK_ENTITIES.register(
           "living_ember_lamp",
           () -> Builder.of(
                   LivingEmberLampBlockEntity::new,
                   AllModBlocks.LIVING_EMBER_LAMP.get()
           ).build(null)
   );

   public static final DeferredHolder<
           BlockEntityType<?>,
           BlockEntityType<ThermalRelaySwitchBlockEntity>
           > THERMAL_RELAY_SWITCH = BLOCK_ENTITIES.register(
           "thermal_relay_switch",
           () -> Builder.of(
                   ThermalRelaySwitchBlockEntity::new,
                   AllModBlocks.THERMAL_RELAY_SWITCH.get()
           ).build(null)
   );

   public static final DeferredHolder<
           BlockEntityType<?>,
           BlockEntityType<ThermochemicalConduitBlockEntity>
           > THERMOCHEMICAL_CONDUIT =
           BLOCK_ENTITIES.register(
                   "thermochemical_conduit",
                   () -> Builder.of(
                           ThermochemicalConduitBlockEntity::new,
                           AllModBlocks.THERMOCHEMICAL_CONDUIT.get()
                   ).build(null)
           );



   public static final DeferredHolder<
           BlockEntityType<?>,
           BlockEntityType<ThermochemicalShaftBlockEntity>
           > THERMOCHEMICAL_SHAFT =
           BLOCK_ENTITIES.register(
                   "thermochemical_shaft",
                   () -> Builder.of(
                           ThermochemicalShaftBlockEntity::new,
                           AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                   ).build(null)
           );

   public static final DeferredHolder<
           BlockEntityType<?>,
           BlockEntityType<ThermochemicalGearboxBlockEntity>
           > THERMOCHEMICAL_GEARBOX =
           BLOCK_ENTITIES.register(
                   "thermochemical_gearbox",
                   () -> Builder.of(
                           ThermochemicalGearboxBlockEntity::new,
                           AllModBlocks.THERMOCHEMICAL_GEARBOX.get()
                   ).build(null)
           );

    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<SulfurBurnerBlockEntity>
            > SULFUR_BURNER =
            BLOCK_ENTITIES.register(
                    "sulfur_burner",
                    () -> Builder.of(
                            SulfurBurnerBlockEntity::new,
                            AllModBlocks
                                    .SULFUR_BURNER
                                    .get()
                    ).build(null)
            );

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
      eventBus.addListener(AllBlockEntities::registerCapabilities);
      CreateSulfuricResonance.LOGGER.info("Block entities registered for Sulfuric Resonance");
   }

   private static void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.registerBlockEntity(FluidHandler.BLOCK, PERFORATED_SPRITZER.get(), (blockEntity, side) -> {
         if (side != null && side != Direction.UP) {
            return null;
         } else {
            if (blockEntity.fluidCapability == null) {
               blockEntity.refreshCapability();
            }

            return blockEntity.fluidCapability;
         }



      });
       event.registerBlockEntity(
               Capabilities.ItemHandler.BLOCK,
               SULFUR_BURNER.get(),
               (blockEntity, side) ->
                       blockEntity
                               .getAutomationFuelHandler()
       );
      event.registerBlockEntity(
              Capabilities.ItemHandler.BLOCK,
              ASH_CERAMIC_CRUCIBLE.get(),
              (blockEntity, side) -> blockEntity.getItemCapability()
      );

      event.registerBlockEntity(
              FluidHandler.BLOCK,
              ASH_CERAMIC_CRUCIBLE.get(),
              (blockEntity, side) -> blockEntity.getFluidCapability()
      );
      CreateSulfuricResonance.LOGGER.info("Registered capabilities for block entities");
   }
}
