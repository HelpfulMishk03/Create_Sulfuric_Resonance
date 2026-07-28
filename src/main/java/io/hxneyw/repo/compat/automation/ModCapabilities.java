package io.hxneyw.repo.compat.automation;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;

public final class ModCapabilities {

   private ModCapabilities() {
   }

   public static void registerCapabilities(
           RegisterCapabilitiesEvent event
   ) {
      event.registerBlockEntity(
              Capabilities.ItemHandler.BLOCK,
              AllBlockEntities.MOLTEN_ROTOR.get(),
              ModCapabilities::getMoltenRotorFuelHandler
      );
   }

   private static IItemHandler getMoltenRotorFuelHandler(
           MoltenRotorBlockEntity furnace,
           Direction side
   ) {
      return furnace.getAutomationFuelHandler(side);
   }
}