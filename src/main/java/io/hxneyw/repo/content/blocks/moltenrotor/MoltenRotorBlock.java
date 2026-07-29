package io.hxneyw.repo.content.blocks.moltenrotor;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllVoxelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoltenRotorBlock extends DirectionalKineticBlock implements IBE<MoltenRotorBlockEntity>, IWrenchable {
   public static final EnumProperty<HeatLevel> HEAT_LEVEL = BlazeBurnerBlock.HEAT_LEVEL;

   public MoltenRotorBlock(Properties props) {
      super(props);
      this.registerDefaultState(this.defaultBlockState().setValue(HEAT_LEVEL, HeatLevel.NONE));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(HEAT_LEVEL);
   }

   public Axis getRotationAxis(BlockState state) {
      return state.getValue(FACING).getCounterClockWise().getAxis();
   }

   public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
      Direction facing = state.getValue(FACING);
      return face == facing.getCounterClockWise() || face == facing.getClockWise();
   }

   public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
      Direction currentFacing = originalState.getValue(FACING);
      Direction newFacing = currentFacing.getClockWise();

      while (newFacing.getAxis().isVertical()) {
         newFacing = newFacing.getClockWise();
      }

      return originalState.setValue(FACING, newFacing);
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx) {
      return this.defaultBlockState()
              .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
              .setValue(HEAT_LEVEL, HeatLevel.NONE);
   }

   public Class<MoltenRotorBlockEntity> getBlockEntityClass() {
      return MoltenRotorBlockEntity.class;
   }

   public BlockEntityType<? extends MoltenRotorBlockEntity> getBlockEntityType() {
      return AllBlockEntities.MOLTEN_ROTOR.get();
   }

   public int getLightEmission(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
      return switch (state.getValue(HEAT_LEVEL)) {
         case NONE -> 0;
         case SMOULDERING, FADING -> 8;
         case KINDLED -> 12;
         case SEETHING -> 15;
      };
   }

   @NotNull
   protected VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return context == CollisionContext.empty()
              ? Shapes.box(0.0625, 0.0, 0.1875, 0.9375, 1.0, 0.875)
              : AllVoxelShapes.MoltenRotor.getShape(state.getValue(FACING));
   }

   @NotNull
   protected VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return AllVoxelShapes.MoltenRotor.getShape(state.getValue(FACING));
   }

   @OnlyIn(Dist.CLIENT)
   public boolean addLandingEffects(
           @NotNull BlockState state1,
           @NotNull ServerLevel level,
           @NotNull BlockPos pos,
           @NotNull BlockState state2,
           @NotNull LivingEntity entity,
           int numberOfParticles
   ) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean addRunningEffects(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
      return true;
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MoltenRotorBlockEntity furnace && !level.isClientSide) {
         for (ItemStack queuedFuel : furnace.drainPendingFuelForDrop()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), queuedFuel);
         }
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }
   @NotNull
   @Override
   protected ItemInteractionResult useItemOn(
           @NotNull ItemStack stack,
           @NotNull BlockState state,
           @NotNull Level level,
           @NotNull BlockPos pos,
           @NotNull Player player,
           @NotNull InteractionHand hand,
           @NotNull BlockHitResult hit
   ) {
      if (stack.getItem() instanceof WrenchItem) {
         UseOnContext context = new UseOnContext(player, hand, hit);

         InteractionResult result = player.isShiftKeyDown()
                 ? this.onSneakWrenched(state, context)
                 : this.onWrenched(state, context);

         return result == InteractionResult.SUCCESS
                 ? ItemInteractionResult.SUCCESS
                 : ItemInteractionResult.FAIL;
      }

      return MoltenRotorInteractions.useItemOn(
              stack,
              level,
              pos,
              player
      );
   }

   @NotNull
   @Override
   protected InteractionResult useWithoutItem(
           @NotNull BlockState state,
           @NotNull Level level,
           @NotNull BlockPos pos,
           @NotNull Player player,
           @NotNull BlockHitResult hit
   ) {
      return MoltenRotorInteractions.useWithoutItem(
              level,
              pos,
              player
      );
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
      super.onPlace(state, level, pos, oldState, isMoving);
      if (!level.isClientSide && !oldState.is(this)) {
         level.scheduleTick(pos, this, 1);
      }
   }

   public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
      super.tick(state, level, pos, random);
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof MoltenRotorBlockEntity furnace) {
         furnace.initializeKinetics();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(
           @NotNull BlockState state,
           @NotNull Level level,
           @NotNull BlockPos pos,
           @NotNull RandomSource random
   ) {
      MoltenRotorParticles.animateTick(state, level, pos, random);
   }

}