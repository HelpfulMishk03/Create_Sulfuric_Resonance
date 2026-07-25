package io.hxneyw.repo.content.fluids.spritzer;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.ComparatorUtil;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidHelper.FluidExchange;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class PerforatedSpritzerBlock extends Block implements IWrenchable, IBE<PerforatedSpritzerBlockEntity> {
   public PerforatedSpritzerBlock(Properties properties) {
      super(properties);
   }

   protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
   }

   public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, LivingEntity placer, @NotNull ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      AdvancementBehaviour.setPlacedBy(level, pos, placer);
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return this.makeShape();
   }

   private VoxelShape makeShape() {
      return Shapes.block();
   }

   @NotNull
   protected ItemInteractionResult useItemOn(
      ItemStack stack,
      @NotNull BlockState state,
      @NotNull Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hitResult
   ) {
      if (stack.isEmpty()) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      } else {
         PerforatedSpritzerBlockEntity be = (PerforatedSpritzerBlockEntity)this.getBlockEntity(level, pos);
         if (be == null) {
            return ItemInteractionResult.FAIL;
         } else {
            IFluidHandler fluidCapability = (IFluidHandler)level.getCapability(FluidHandler.BLOCK, pos, null);
            if (fluidCapability == null) {
               return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else {
               FluidStack prevFluidInTank = fluidCapability.getFluidInTank(0).copy();
               FluidExchange exchange = null;
               if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be)) {
                  exchange = FluidExchange.ITEM_TO_TANK;
               } else if (FluidHelper.tryFillItemFromBE(level, player, hand, stack, be)) {
                  exchange = FluidExchange.TANK_TO_ITEM;
               }

               if (exchange == null) {
                  return !GenericItemEmptying.canItemBeEmptied(level, stack) && !GenericItemFilling.canItemBeFilled(level, stack)
                     ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                     : ItemInteractionResult.SUCCESS;
               } else {
                  SoundEvent soundevent = null;
                  BlockState fluidState = null;
                  FluidStack fluidInTank = fluidCapability.getFluidInTank(0);
                  if (exchange == FluidExchange.ITEM_TO_TANK) {
                     Fluid fluid = fluidInTank.getFluid();
                     fluidState = fluid.defaultFluidState().createLegacyBlock();
                     soundevent = FluidHelper.getEmptySound(fluidInTank);
                  }

                  if (exchange == FluidExchange.TANK_TO_ITEM) {
                     Fluid fluid = prevFluidInTank.getFluid();
                     fluidState = fluid.defaultFluidState().createLegacyBlock();
                     soundevent = FluidHelper.getFillSound(prevFluidInTank);
                  }

                  if (soundevent != null && !level.isClientSide) {
                     float pitch = Mth.clamp(1.0F - 1.0F * fluidInTank.getAmount() / 3500.0F, 0.0F, 1.0F);
                     pitch /= 1.5F;
                     pitch += 0.5F;
                     pitch += (level.random.nextFloat() - 0.5F) / 4.0F;
                     level.playSound(null, pos, soundevent, SoundSource.BLOCKS, 0.5F, pitch);
                  }

                  if (!FluidStack.isSameFluidSameComponents(fluidInTank, prevFluidInTank)) {
                     if (level.isClientSide) {
                        BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, fluidState);
                        Vec3 vec = hitResult.getLocation();
                        Vec3 motion = player.position().subtract(vec).scale(0.05F);
                        vec = vec.add(motion);
                        level.addParticle(particleData, vec.x, vec.y, vec.z, motion.x, motion.y, motion.z);
                     }

                     be.sendDataImmediately();
                     be.setChanged();
                  }

                  return ItemInteractionResult.SUCCESS;
               }
            }
         }
      }
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
      return this.getBlockEntityOptional(level, pos).map(be -> ComparatorUtil.fractionToRedstoneLevel(be.getFillState())).orElse(0);
   }

   public Class<PerforatedSpritzerBlockEntity> getBlockEntityClass() {
      return PerforatedSpritzerBlockEntity.class;
   }

   public BlockEntityType<? extends PerforatedSpritzerBlockEntity> getBlockEntityType() {
      return (BlockEntityType<? extends PerforatedSpritzerBlockEntity>)AllBlockEntities.PERFORATED_SPRITZER.get();
   }
}
