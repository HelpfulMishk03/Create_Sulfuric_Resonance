package io.hxneyw.repo.content.blocks.moltenrotor;

import io.hxneyw.repo.content.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Client-side visual effects for the Molten Rotor.
 *
 * <p>This class is deliberately behavior-preserving: particle probabilities,
 * positions, velocities, sounds, and fuel/tier checks are copied unchanged
 * from MoltenRotorBlock.</p>
 */
public final class MoltenRotorParticles {
   private MoltenRotorParticles() {
   }

   private static MoltenRotorBlockEntity getBlockEntity(Level level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof MoltenRotorBlockEntity moltenRotor
              ? moltenRotor
              : null;
   }

   @OnlyIn(Dist.CLIENT)
   public static void animateTick(
           @NotNull BlockState state,
           @NotNull Level level,
           @NotNull BlockPos pos,
           @NotNull RandomSource random
   ) {
      MoltenRotorBlockEntity furnace = getBlockEntity(level, pos);

      if (furnace == null || furnace.getDisplayFuelTime() <= 0) {
         return;
      }

      Direction facing = state.getValue(MoltenRotorBlock.FACING);
      float temperature = furnace.getDisplayTemperature();

      if (temperature < 300.0F) {
         spawnHeatingKindlingFire(
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

      spawnChamberFlames(
              level,
              pos,
              facing,
              random,
              intensity,
              radiant,
              blazeFuel,
              soulBlazeFuel
      );

      spawnInteriorExhaustFlow(
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
      if (random.nextFloat() < 0.37F) {
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
   private static void spawnHeatingKindlingFire(
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

         double[] position = toWorldChamberPoint(
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
         double[] smokePosition = toWorldChamberPoint(
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
   private static void spawnChamberFlames(
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
         spawnBlazeCakeFlames(
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

         double[] position = toWorldChamberPoint(
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
   private static void spawnBlazeCakeFlames(
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

      spawnBlazeCakeFlameAt(
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

         spawnBlazeCakeFlameAt(
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
   private static void spawnBlazeCakeFlameAt(
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
              toWorldChamberPoint(
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
   private static void spawnInteriorExhaustFlow(
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
              toWorldChamberPoint(
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
   private static double[] toWorldChamberPoint(
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
