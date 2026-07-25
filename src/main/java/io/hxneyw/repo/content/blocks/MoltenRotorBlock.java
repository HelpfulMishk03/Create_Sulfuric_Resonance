package io.hxneyw.repo.content.blocks;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.Config;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModSounds;
import io.hxneyw.repo.content.registry.AllVoxelShapes;
import io.hxneyw.repo.content.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level.ExplosionInteraction;
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

      MoltenRotorBlockEntity furnace = this.getBlockEntity(level, pos);
      if (furnace == null) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      if (level.isClientSide) {
         return ItemInteractionResult.SUCCESS;
      }

      if (stack.is(AllItems.CREATIVE_BLAZE_CAKE.get())) {
         if (!furnace.isCreativeMode()) {
            furnace.setCreativeMode(true);
         } else {
            furnace.cycleCreativeTier();
         }

         return ItemInteractionResult.SUCCESS;
      }

      if (stack.is(Items.TNT)) {
         if (Config.TNT_CAN_EXPLODE.get()
                 && furnace.tntCooldown <= 0
                 && level.random.nextFloat() < 0.25F) {

            furnace.tntCooldown = 20;
            level.removeBlock(pos, false);
            level.explode(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    1.5F,
                    ExplosionInteraction.BLOCK
            );
            player.sendSystemMessage(Component.literal("FURNACE EXPLODED!"));
            return ItemInteractionResult.SUCCESS;
         }

         if (!furnace.insertFuel(stack, false)) {
            return ItemInteractionResult.FAIL;
         }

         furnace.tntCooldown = 5;
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }

         return ItemInteractionResult.SUCCESS;
      }

      if (stack.is(Items.NETHER_STAR)) {
         furnace.addUltimateFuel(6000);

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }

         player.sendSystemMessage(Component.literal("Nether Star inserted (+300s, radiant heat)."));
         return ItemInteractionResult.SUCCESS;
      }

      if (stack.is(Items.DRAGON_BREATH)) {
         furnace.addUltimateFuel(4000);

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }

         player.sendSystemMessage(Component.literal("Dragon's Breath inserted (+200s, radiant heat)."));
         return ItemInteractionResult.SUCCESS;
      }

      boolean isFuel = stack.is(Items.STICK)
              || stack.is(ItemTags.LOGS)
              || stack.is(Items.COAL)
              || stack.is(Items.CHARCOAL)
              || stack.is(Items.COAL_BLOCK)
              || stack.is(Items.DRIED_KELP_BLOCK)
              || stack.is(AllItems.BLAZE_CAKE.get())
              || stack.is(io.hxneyw.repo.content.Items.SOUL_FIRED_BLAZE_CAKE.get());

      if (!isFuel) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      if (!furnace.insertFuel(stack, false)) {
         if (stack.is(Items.STICK)) {
            player.sendSystemMessage(Component.literal("Sticks require logs to be burning."));
         } else if (stack.is(io.hxneyw.repo.content.Items.SOUL_FIRED_BLAZE_CAKE.get())
                 && !furnace.getCurrentHeatTier().isAtLeast(MoltenRotorBlockEntity.RotorHeatLevel.SEETHING)) {
            player.sendSystemMessage(Component.literal("Soul-Fired Blaze Cake requires SEETHING heat."));
         }

         return ItemInteractionResult.FAIL;
      }

      if (stack.is(ItemTags.LOGS)) {
         level.playSound(
                 null,
                 pos,
                 AllModSounds.LOG_INSERT.get(),
                 SoundSource.BLOCKS,
                 1.0F,
                 level.random.nextFloat() * 0.5F + 0.7F
         );
      }

      if (stack.is(AllItems.BLAZE_CAKE.get())) {
         player.sendSystemMessage(Component.literal("Blaze Cake inserted (+150s)."));
      }

      if (stack.is(io.hxneyw.repo.content.Items.SOUL_FIRED_BLAZE_CAKE.get())) {
         player.sendSystemMessage(Component.literal("Soul-Fired Blaze Cake inserted (+175s, maximum heat)."));
      }

      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }

      return ItemInteractionResult.SUCCESS;
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
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }

      MoltenRotorBlockEntity furnace = this.getBlockEntity(level, pos);
      if (furnace == null || !furnace.shouldShowStatus()) {
         return InteractionResult.SUCCESS;
      }

      int temp = furnace.getDisplayTemperature();
      int fuel = furnace.getDisplayFuelTime();
      int cooldown = furnace.getDisplayCooldownTime();
      float rpm = furnace.getGeneratedSpeed();
      float stressUnits = furnace.getTotalStressOutput();
      String tierName = furnace.getHeatTierName();

      ChatFormatting tierColor = switch (furnace.getCurrentHeatTier()) {
         case NONE -> ChatFormatting.GRAY;
         case SMOULDERING, FADING -> ChatFormatting.YELLOW;
         case KINDLED -> ChatFormatting.GOLD;
         case SEETHING -> ChatFormatting.RED;
         case RADIANT -> ChatFormatting.DARK_PURPLE;
      };

      player.sendSystemMessage(
              Component.literal(tierName + " (" + temp + "°C)")
                      .withStyle(tierColor)
      );

      if (fuel > 0) {
         int fuelSeconds = fuel / 20;
         player.sendSystemMessage(
                 Component.literal(
                         String.format(
                                 "Fuel: %ds | RPM: %d | SU: %d",
                                 fuelSeconds,
                                 (int)rpm,
                                 (int)stressUnits
                         )
                 ).withStyle(ChatFormatting.GRAY)
         );
      } else if (cooldown > 0) {
         int cooldownSeconds = cooldown / 20;
         player.sendSystemMessage(
                 Component.literal(
                         String.format(
                                 "Cooling: %ds | RPM: %d | SU: %d",
                                 cooldownSeconds,
                                 (int)rpm,
                                 (int)stressUnits
                         )
                 ).withStyle(ChatFormatting.AQUA)
         );
      } else if (rpm > 0.0F) {
         player.sendSystemMessage(
                 Component.literal(
                         String.format(
                                 "RPM: %d | SU: %d",
                                 (int)rpm,
                                 (int)stressUnits
                         )
                 ).withStyle(ChatFormatting.GRAY)
         );
      }

      return InteractionResult.SUCCESS;
   }

   @Nullable
   private MoltenRotorBlockEntity getBlockEntity(Level level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof MoltenRotorBlockEntity moltenRotor ? moltenRotor : null;
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
   public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
      HeatLevel heat = state.getValue(HEAT_LEVEL);
      if (heat.isAtLeast(HeatLevel.SMOULDERING)) {
         Direction facing = state.getValue(FACING);
         MoltenRotorBlockEntity furnace = this.getBlockEntity(level, pos);
         boolean isRadiant = furnace != null && furnace.getCurrentHeatTier() == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT;
         if (random.nextInt(30) == 0) {
            level.playLocalSound(
               pos.getX() + 0.5,
               pos.getY() + 0.5,
               pos.getZ() + 0.5,
               SoundEvents.CAMPFIRE_CRACKLE,
               SoundSource.BLOCKS,
               0.5F + random.nextFloat() * 0.2F,
               random.nextFloat() * 0.7F + 0.6F,
               false
            );
         }

         if (isRadiant) {
            if (random.nextInt(3) == 0) {
               level.addParticle(
                       ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                  pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                  pos.getY() + 0.6,
                  pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                  0.0,
                  0.01,
                  0.0
               );
            }

            if (random.nextInt(3) == 0) {
               level.addParticle(
                  ParticleTypes.SOUL_FIRE_FLAME,
                  pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                  pos.getY() + 0.6,
                  pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                  0.0,
                  0.01,
                  0.0
               );
            }

            if (random.nextInt(4) == 0) {
               level.addParticle(
                  ParticleTypes.END_ROD,
                  pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                  pos.getY() + 0.7,
                  pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                  (random.nextDouble() - 0.5) * 0.02,
                  0.02,
                  (random.nextDouble() - 0.5) * 0.02
               );
            }
         } else if (heat.isAtLeast(HeatLevel.KINDLED) && random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
         }

         if (random.nextInt(1) == 0) {
            double[][] slitOffsets = new double[][]{{-0.25, 0.85}, {0.0, 0.85}, {0.25, 0.85}};

            for (double[] slit : slitOffsets) {
               double y = pos.getY() + slit[1];
               double x;
               double z;
               double velX;
               double velY;
               double velZ;
               switch (facing) {
                  case NORTH:
                     x = pos.getX() + 0.5 + slit[0];
                     z = pos.getZ() + 0.8;
                     velX = (random.nextDouble() - 0.5) * 0.02;
                     velY = 0.03;
                     velZ = 0.02;
                     break;
                  case SOUTH:
                     x = pos.getX() + 0.5 - slit[0];
                     z = pos.getZ() + 0.2;
                     velX = (random.nextDouble() - 0.5) * 0.02;
                     velY = 0.03;
                     velZ = -0.02;
                     break;
                  case EAST:
                     x = pos.getX() + 0.2;
                     z = pos.getZ() + 0.5 + slit[0];
                     velX = -0.02;
                     velY = 0.03;
                     velZ = (random.nextDouble() - 0.5) * 0.02;
                     break;
                  case WEST:
                     x = pos.getX() + 0.8;
                     z = pos.getZ() + 0.5 - slit[0];
                     velX = 0.02;
                     velY = 0.03;
                     velZ = (random.nextDouble() - 0.5) * 0.02;
                     break;
                  default:
                     return;
               }

               level.addParticle(ParticleTypes.SMOKE, x, y, z, velX, velY, velZ);
            }
         }
      }
   }
}
