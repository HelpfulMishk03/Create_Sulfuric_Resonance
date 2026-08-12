package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.content.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Source;
import net.neoforged.neoforge.fluids.FluidType.Properties;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AllModFluids {
   public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, "sulfuricresonance");
   public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, "sulfuricresonance");
   public static final DeferredHolder<FluidType, FluidType> SULFURIC_ACID_TYPE = FLUID_TYPES.register(
      "sulfuric_acid",
      () -> new FluidType(Properties.create().density(1500).viscosity(4000).temperature(300).canConvertToSource(false).canDrown(true).canExtinguish(false))
   );
   public static final DeferredHolder<Fluid, Source> SULFURIC_ACID = FLUIDS.register("sulfuric_acid", () -> new Source(makeProperties()));
   public static final DeferredHolder<Fluid, Flowing> SULFURIC_ACID_FLOWING = FLUIDS.register("sulfuric_acid_flowing", () -> new Flowing(makeProperties()));

   private static net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties makeProperties() {
      return new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(SULFURIC_ACID_TYPE, SULFURIC_ACID, SULFURIC_ACID_FLOWING)
         .bucket(Items.SULFURIC_ACID_BUCKET)
         .block(AllModBlocks.SULFURIC_ACID_BLOCK)
         .levelDecreasePerBlock(2)
         .tickRate(20)
         .slopeFindDistance(3)
         .explosionResistance(100.0F);
   }

   public static void register(IEventBus eventBus) {
      FLUID_TYPES.register(eventBus);
      FLUIDS.register(eventBus);
   }
}
