package io.hxneyw.repo.content.fluids;

import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties;
import org.jetbrains.annotations.NotNull;

public abstract class SulfuricAcidFluid extends BaseFlowingFluid {
   protected SulfuricAcidFluid(Properties properties) {
      super(properties);
   }

   public static class Flowing extends SulfuricAcidFluid {
      public Flowing(Properties properties) {
         super(properties);
      }

      protected void createFluidStateDefinition(@NotNull Builder<Fluid, FluidState> builder) {
         super.createFluidStateDefinition(builder);
         builder.add(new Property[]{LEVEL});
      }

      public int getAmount(FluidState state) {
         return (Integer)state.getValue(LEVEL);
      }

      public boolean isSource(@NotNull FluidState state) {
         return false;
      }
   }

   public static class Source extends SulfuricAcidFluid {
      public Source(Properties properties) {
         super(properties);
      }

      public int getAmount(@NotNull FluidState state) {
         return 8;
      }

      public boolean isSource(@NotNull FluidState state) {
         return true;
      }
   }
}
