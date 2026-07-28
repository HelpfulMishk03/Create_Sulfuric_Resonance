package io.hxneyw.repo.content.blocks.moltenrotor;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.Config;
import io.hxneyw.repo.compat.fuel.FuelCompatibility;
import io.hxneyw.repo.compat.fuel.ResolvedFuel;
import io.hxneyw.repo.content.registry.AllBlockEntities;
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
      /*
       * Empty-hand interaction belongs to useWithoutItem().
       */
      if (stack.isEmpty()) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }

      if (stack.getItem() instanceof WrenchItem) {
         UseOnContext context = new UseOnContext(player, hand, hit);

         InteractionResult result = player.isShiftKeyDown()
                 ? this.onSneakWrenched(state, context)
                 : this.onWrenched(state, context);

         return result == InteractionResult.SUCCESS
                 ? ItemInteractionResult.SUCCESS
                 : ItemInteractionResult.FAIL;
      }

      /*
       * IMPORTANT: classify the held item without first requiring the client to
       * have a MoltenRotorBlockEntity instance. The old early furnace == null
       * branch routed every item into Item#useOn: logs placed as blocks and coal
       * appeared to do nothing.
       */
      ResolvedFuel resolvedFuel = FuelCompatibility.resolve(stack);
      MoltenRotorBlockEntity.FuelType fuelType =
              resolvedFuel == null || resolvedFuel.isInvalid()
                      ? null
                      : resolvedFuel.type();

      boolean creativeCake = stack.is(AllItems.CREATIVE_BLAZE_CAKE.get());
      boolean tnt = stack.is(Items.TNT);
      boolean netherStar = stack.is(Items.NETHER_STAR);
      boolean dragonBreath = stack.is(Items.DRAGON_BREATH);
      boolean recognizedFuel =
              fuelType != null
                      && fuelType != MoltenRotorBlockEntity.FuelType.NONE;

      /*
       * Non-fuels must skip the furnace's empty-hand/status interaction and
       * continue to the held item's own useOn method. BlockItems can therefore
       * place against the furnace normally.
       */
      if (!creativeCake && !tnt && !netherStar && !dragonBreath && !recognizedFuel) {
         return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
      }

      /*
       * The client has enough information to claim accepted furnace items now.
       * Actual mutation remains server-only.
       */
      if (level.isClientSide) {
         return ItemInteractionResult.SUCCESS;
      }

      MoltenRotorBlockEntity furnace = this.getBlockEntity(level, pos);
      if (furnace == null) {
         return ItemInteractionResult.FAIL;
      }

      if (creativeCake) {
         if (!furnace.isCreativeMode()) {
            furnace.setCreativeMode(true);
         } else {
            furnace.cycleCreativeTier();
         }
         return ItemInteractionResult.SUCCESS;
      }

      if (tnt) {
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
                    Level.ExplosionInteraction.BLOCK
            );

            player.sendSystemMessage(Component.literal("FURNACE EXPLODED!"));
            return ItemInteractionResult.SUCCESS;
         }

         if (!furnace.insertFuel(stack, false)) {
            return ItemInteractionResult.FAIL;
         }

         furnace.tntCooldown = 5;
         playInsertionSound(level, pos);

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
         return ItemInteractionResult.SUCCESS;
      }

      if (netherStar) {
         furnace.addUltimateFuel(6000);
         playInsertionSound(level, pos);

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
         return ItemInteractionResult.SUCCESS;
      }

      if (dragonBreath) {
         furnace.addUltimateFuel(4000);
         playInsertionSound(level, pos);

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
         return ItemInteractionResult.SUCCESS;
      }

      if (!furnace.insertFuel(stack, false)) {
         if (fuelType == MoltenRotorBlockEntity.FuelType.STICK) {
            player.sendSystemMessage(Component.literal("Sticks require logs to be burning."));
         } else if (fuelType == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE
                 && !furnace.getCurrentHeatTier().isAtLeast(MoltenRotorBlockEntity.RotorHeatLevel.SEETHING)) {
            player.sendSystemMessage(Component.literal("Soul-Fired Blaze Cake requires SEETHING heat."));
         }
         return ItemInteractionResult.FAIL;
      }

      playInsertionSound(level, pos);

      if (fuelType == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE) {
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
      MoltenRotorBlockEntity furnace = this.getBlockEntity(level, pos);

      // We must still return PASS here if there is no furnace or status to show
      if (furnace == null || !furnace.shouldShowStatus()) {
         return InteractionResult.PASS;
      }

      if (level.isClientSide) {
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

   private static void playInsertionSound(Level level, BlockPos pos) {
      level.playSound(
              null,
              pos,
              SoundEvents.BUNDLE_INSERT,
              SoundSource.BLOCKS,
              0.25F,
              0.95F + level.random.nextFloat() * 0.10F
      );
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
   public void animateTick(
           @NotNull BlockState state,
           @NotNull Level level,
           @NotNull BlockPos pos,
           @NotNull RandomSource random
   ) {
      MoltenRotorBlockEntity furnace = this.getBlockEntity(level, pos);

      if (furnace == null || furnace.getDisplayFuelTime() <= 0) {
         return;
      }

      Direction facing = state.getValue(FACING);
      float temperature = furnace.getDisplayTemperature();

      if (temperature < 300.0F) {
         this.spawnHeatingKindlingFire(
                 level,
                 pos,
                 facing,
                 random,
                 temperature
         );
         return;
      }

      float intensity = Math.clamp(
              (temperature - 200.0F) / 1000.0F,
              0.12F,
              1.0F
      );

      boolean radiant =
              furnace.getCurrentHeatTier()
                      == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT;

      MoltenRotorBlockEntity.FuelType activeFuelType =
              furnace.getRenderedFuelType();

      boolean blazeFuel =
              activeFuelType == MoltenRotorBlockEntity.FuelType.BLAZE_CAKE
                      || activeFuelType
                      == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE;

      boolean soulBlazeFuel =
              activeFuelType
                      == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE;

      if (random.nextInt(30) == 0) {
         level.playLocalSound(
                 pos.getX() + 0.5,
                 pos.getY() + 0.35,
                 pos.getZ() + 0.5,
                 SoundEvents.CAMPFIRE_CRACKLE,
                 SoundSource.BLOCKS,
                 0.35F + intensity * 0.25F,
                 0.75F + random.nextFloat() * 0.35F,
                 false
         );
      }

      this.spawnChamberFlames(
              level,
              pos,
              facing,
              random,
              intensity,
              radiant,
              blazeFuel,
              soulBlazeFuel
      );

      this.spawnInteriorExhaustFlow(
              level,
              pos,
              facing,
              random,
              intensity
      );

      /*
       * Original slit locations, direction and normal SMOKE type.
       * Only one slit emits on a given display tick so the exhaust
       * stays defined instead of building into a giant cloud.
       */
      if (random.nextFloat() < 0.44F) {
         double[] slitOffsets = {-0.25, 0.0, 0.25};
         double slitOffset = slitOffsets[random.nextInt(slitOffsets.length)];
         double y = pos.getY() + 0.85;
         double x;
         double z;
         double velX;
         double velY = 0.03;
         double velZ;

         switch (facing) {
            case NORTH:
               x = pos.getX() + 0.5 + slitOffset;
               z = pos.getZ() + 0.8;
               velX = (random.nextDouble() - 0.5) * 0.02;
               velZ = 0.02;
               break;
            case SOUTH:
               x = pos.getX() + 0.5 - slitOffset;
               z = pos.getZ() + 0.2;
               velX = (random.nextDouble() - 0.5) * 0.02;
               velZ = -0.02;
               break;
            case EAST:
               x = pos.getX() + 0.2;
               z = pos.getZ() + 0.5 + slitOffset;
               velX = -0.02;
               velZ = (random.nextDouble() - 0.5) * 0.02;
               break;
            case WEST:
               x = pos.getX() + 0.8;
               z = pos.getZ() + 0.5 - slitOffset;
               velX = 0.02;
               velZ = (random.nextDouble() - 0.5) * 0.02;
               break;
            default:
               return;
         }

         level.addParticle(
                 ParticleTypes.SMOKE,
                 x,
                 y,
                 z,
                 velX,
                 velY,
                 velZ
         );
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void spawnHeatingKindlingFire(
           Level level,
           BlockPos pos,
           Direction facing,
           RandomSource random,
           float temperature
   ) {
      float progress = Math.clamp(
              (temperature - 20.0F) / 280.0F,
              0.0F,
              1.0F
      );

      int particleCount = progress < 0.35F
              ? 1
              : progress < 0.75F
              ? 2
              : 3;

      int chance = progress < 0.35F
              ? 3
              : progress < 0.75F
              ? 2
              : 1;

      if (random.nextInt(chance) != 0) {
         return;
      }

      for (int index = 0; index < particleCount; index++) {
         double side =
                 (random.nextDouble() - 0.5)
                         * (0.10 + progress * 0.08);
         double depth =
                 0.06
                         + (random.nextDouble() - 0.5)
                         * (0.08 + progress * 0.05);
         double localY =
                 0.15
                         + random.nextDouble()
                         * (0.025 + progress * 0.07);

         double[] position = this.toWorldChamberPoint(
                 pos,
                 facing,
                 side,
                 localY,
                 depth
         );

         level.addParticle(
                 ParticleTypes.SMALL_FLAME,
                 position[0],
                 position[1],
                 position[2],
                 0.0,
                 0.002 + progress * 0.004,
                 0.0
         );
      }

      if (progress > 0.55F && random.nextInt(8) == 0) {
         double[] smokePosition = this.toWorldChamberPoint(
                 pos,
                 facing,
                 (random.nextDouble() - 0.5) * 0.08,
                 0.20,
                 0.06
         );

         level.addParticle(
                 ParticleTypes.SMOKE,
                 smokePosition[0],
                 smokePosition[1],
                 smokePosition[2],
                 0.0,
                 0.004,
                 0.0
         );
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void spawnChamberFlames(
           Level level,
           BlockPos pos,
           Direction facing,
           RandomSource random,
           float intensity,
           boolean radiant,
           boolean blazeFuel,
           boolean soulBlazeFuel
   ) {
      if (blazeFuel) {
         this.spawnBlazeCakeFlames(
                 level,
                 pos,
                 facing,
                 random,
                 intensity,
                 radiant,
                 soulBlazeFuel
         );
         return;
      }

      int flameChance = intensity < 0.35F
              ? 3
              : intensity < 0.70F
              ? 2
              : 1;

      if (random.nextInt(flameChance) != 0) {
         return;
      }

      int particleCount = intensity < 0.35F
              ? 1
              : intensity < 0.70F
              ? 2
              : 3;

      double[][] flameAnchors = {
              {-0.17, 0.00},
              {0.17, 0.00},
              {-0.18, 0.10},
              {0.18, 0.10},
              {-0.10, 0.18},
              {0.10, 0.18},
              {0.00, 0.06}
      };

      for (int particleIndex = 0;
           particleIndex < particleCount;
           particleIndex++) {
         double[] anchor =
                 flameAnchors[random.nextInt(flameAnchors.length)];
         double side =
                 anchor[0]
                         + (random.nextDouble() - 0.5) * 0.04;
         double depth =
                 anchor[1]
                         + (random.nextDouble() - 0.5) * 0.04;
         double localY =
                 0.17
                         + random.nextDouble()
                         * (0.04 + intensity * 0.05);

         double[] position = this.toWorldChamberPoint(
                 pos,
                 facing,
                 side,
                 localY,
                 depth
         );
         double upwardSpeed = 0.002 + intensity * 0.004;

         if (radiant && random.nextInt(6) == 0) {
            level.addParticle(
                    ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                    position[0], position[1], position[2],
                    0.0, upwardSpeed, 0.0
            );
         } else if (radiant && random.nextInt(12) == 0) {
            level.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    position[0], position[1], position[2],
                    0.0, upwardSpeed, 0.0
            );
         } else {
            level.addParticle(
                    ParticleTypes.SMALL_FLAME,
                    position[0], position[1], position[2],
                    0.0, upwardSpeed, 0.0
            );
         }

         if (intensity > 0.70F && random.nextInt(36) == 0) {
            level.addParticle(
                    ParticleTypes.LAVA,
                    position[0],
                    position[1] + 0.04,
                    position[2],
                    0.0,
                    0.010,
                    0.0
            );
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void spawnBlazeCakeFlames(
           Level level,
           BlockPos pos,
           Direction facing,
           RandomSource random,
           float intensity,
           boolean radiant,
           boolean soulBlazeFuel
   ) {
      /*
       * Blaze Cake fire uses a low ring around the cake's footprint
       * rather than the generic center anchors. This keeps the cake
       * visible while making the flame look attached to its edges.
       */
      int flameChance =
              radiant
                      ? 2
                      : intensity >= 0.55F
                      ? 3
                      : 4;

      if (random.nextInt(flameChance) != 0) {
         return;
      }

      double[][] ringAnchors = {
              {-0.14, 0.015},
              {0.14, 0.015},
              {-0.16, 0.075},
              {0.16, 0.075},
              {-0.10, 0.135},
              {0.10, 0.135}
      };

      int anchorIndex =
              random.nextInt(ringAnchors.length);

      this.spawnBlazeCakeFlameAt(
              level,
              pos,
              facing,
              random,
              intensity,
              radiant,
              soulBlazeFuel,
              ringAnchors[anchorIndex]
      );

      /*
       * A rare opposite flame gives superheated cakes a stable ring
       * without creating the former wall of particles.
       */
      if (intensity >= 0.70F && random.nextInt(8) == 0) {
         int oppositeIndex =
                 (anchorIndex + ringAnchors.length / 2)
                         % ringAnchors.length;

         this.spawnBlazeCakeFlameAt(
                 level,
                 pos,
                 facing,
                 random,
                 intensity,
                 radiant,
                 soulBlazeFuel,
                 ringAnchors[oppositeIndex]
         );
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void spawnBlazeCakeFlameAt(
           Level level,
           BlockPos pos,
           Direction facing,
           RandomSource random,
           float intensity,
           boolean radiant,
           boolean soulBlazeFuel,
           double[] anchor
   ) {
      double side =
              anchor[0]
                      + (random.nextDouble() - 0.5) * 0.025;

      double depth =
              anchor[1]
                      + (random.nextDouble() - 0.5) * 0.025;

      double localY =
              0.16
                      + random.nextDouble()
                      * (0.045 + intensity * 0.04);

      double[] position =
              this.toWorldChamberPoint(
                      pos,
                      facing,
                      side,
                      localY,
                      depth
              );

      double upwardSpeed =
              0.0025 + intensity * 0.0035;

      if (soulBlazeFuel && random.nextInt(2) == 0) {
         level.addParticle(
                 ParticleTypes.SOUL_FIRE_FLAME,
                 position[0],
                 position[1],
                 position[2],
                 0.0,
                 upwardSpeed,
                 0.0
         );
      } else if ((soulBlazeFuel || radiant)
              && random.nextInt(4) == 0) {
         level.addParticle(
                 ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                 position[0],
                 position[1],
                 position[2],
                 0.0,
                 upwardSpeed,
                 0.0
         );
      } else {
         level.addParticle(
                 ParticleTypes.SMALL_FLAME,
                 position[0],
                 position[1],
                 position[2],
                 0.0,
                 upwardSpeed,
                 0.0
         );
         if (soulBlazeFuel && random.nextInt(5) == 0) {
            level.addParticle(
                    ParticleTypes.SMOKE,
                    position[0],
                    position[1] + 0.025,
                    position[2],
                    0.0,
                    0.004,
                    0.0
            );
         }
      }
   }



   @OnlyIn(Dist.CLIENT)
   private void spawnInteriorExhaustFlow(
           Level level,
           BlockPos pos,
           Direction facing,
           RandomSource random,
           float intensity
   ) {
      /*
       * A light interior trail suggests that smoke is being pulled
       * toward the original rear slits without competing with them.
       */
      int smokeChance =
              intensity < 0.55F ? 24 : 18;

      if (random.nextInt(smokeChance) != 0) {
         return;
      }

      double[] channels = {
              -0.18,
              0.0,
              0.18
      };

      double side =
              channels[random.nextInt(channels.length)]
                      + (random.nextDouble() - 0.5) * 0.03;

      double[] position =
              this.toWorldChamberPoint(
                      pos,
                      facing,
                      side,
                      0.43 + random.nextDouble() * 0.08,
                      0.15 + random.nextDouble() * 0.08
              );

      Direction rear = facing.getOpposite();

      level.addParticle(
              ParticleTypes.SMOKE,
              position[0],
              position[1],
              position[2],
              rear.getStepX() * 0.012,
              0.012,
              rear.getStepZ() * 0.012
      );
   }

   @OnlyIn(Dist.CLIENT)
   private double[] toWorldChamberPoint(
           BlockPos pos,
           Direction facing,
           double side,
           double localY,
           double rearDepth
   ) {
      Direction rear = facing.getOpposite();
      Direction sideways = facing.getClockWise();

      return new double[]{
              pos.getX() + 0.5
                      + sideways.getStepX() * side
                      + rear.getStepX() * rearDepth,
              pos.getY() + localY,
              pos.getZ() + 0.5
                      + sideways.getStepZ() * side
                      + rear.getStepZ() * rearDepth
      };
   }
}