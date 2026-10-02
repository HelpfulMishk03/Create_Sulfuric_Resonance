package io.hxneyw.repo.compat.arm;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.rotaryleacher.RotaryLeacherBlock;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlock;
import io.hxneyw.repo.content.blocks.RubberPaddingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class AllModArmInteractionPoints {
   public static final DeferredRegister<ArmInteractionPointType> ARM_INTERACTION_POINTS = DeferredRegister.create(
           CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE, "sulfuricresonance"
   );

   public static final DeferredHolder<ArmInteractionPointType, MoltenRotorType> MOLTEN_ROTOR = ARM_INTERACTION_POINTS.register(
           "molten_rotor", MoltenRotorType::new
   );

   public static final DeferredHolder<ArmInteractionPointType, RubberPaddingType> RUBBER_PADDING = ARM_INTERACTION_POINTS.register(
           "rubber_padding", RubberPaddingType::new
   );

   public static final DeferredHolder<ArmInteractionPointType, SulfuricResonanceChamberType> SULFURIC_RESONANCE_CHAMBER = ARM_INTERACTION_POINTS.register(
           "sulfuric_resonance_chamber", SulfuricResonanceChamberType::new
   );

   public static final DeferredHolder<ArmInteractionPointType, RotaryLeacherType> ROTARY_LEACHER = ARM_INTERACTION_POINTS.register(
           "rotary_leacher", RotaryLeacherType::new
   );

   public static void register(IEventBus eventBus) {
      ARM_INTERACTION_POINTS.register(eventBus);
   }

   public static class MoltenRotorType extends ArmInteractionPointType {
      public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
         return state.getBlock() instanceof MoltenRotorBlock;
      }

      public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
         return new MoltenRotorArmPoint(this, level, pos, state);
      }
   }

   public static class SulfuricResonanceChamberType extends ArmInteractionPointType {
      @Override
      public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
         return state.getBlock() instanceof SulfuricResonanceChamberBlock;
      }

      @Override
      public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
         return new SulfuricResonanceChamberArmPoint(this, level, pos, state);
      }
   }

   public static class RotaryLeacherType extends ArmInteractionPointType {
      @Override
      public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
         return state.getBlock() instanceof RotaryLeacherBlock
                 && state.getValue(RotaryLeacherBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER;
      }

      @Override
      public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
         return new RotaryLeacherArmPoint(this, level, pos, state);
      }
   }

   public static class RubberPaddingType extends ArmInteractionPointType {
      public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
         return state.getBlock() instanceof RubberPaddingBlock;
      }

      public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
         return new RubberPaddingArmPoint(this, level, pos, state);
      }
   }
}
