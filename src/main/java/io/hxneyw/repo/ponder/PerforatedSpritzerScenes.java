package io.hxneyw.repo.ponder;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlock;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.content.registry.ModParticles;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

public class PerforatedSpritzerScenes {
   public static void intro(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("perforated_spritzer.intro", "Using the Perforated Spritzer");
      BlockPos middleBasePlate = util.grid().at(2, -1, 2);
      scene.world().setBlock(middleBasePlate, Blocks.WHITE_CONCRETE.defaultBlockState(), false);
      scene.configureBasePlate(0, 0, 5);
      scene.showBasePlate();
      scene.world().showSection(util.select().position(middleBasePlate), Direction.DOWN);
      scene.idle(10);
      BlockPos farmlandPos = util.grid().at(2, 0, 2);
      BlockPos spritzerPos = util.grid().at(2, 2, 2);
      BlockPos motorPos = util.grid().at(3, 2, 2);
      BlockPos pumpPos = util.grid().at(2, 3, 2);
      BlockPos cogwheelPos = util.grid().at(3, 3, 2);
      BlockPos tankPos = util.grid().at(2, 4, 2);
      scene.world().showSection(util.select().position(spritzerPos), Direction.DOWN);
      scene.idle(20);
      scene.overlay()
              .showText(80)
              .text("The Perforated Spritzer can store and disperse fluids in a downward fashion")
              .attachKeyFrame()
              .pointAt(util.vector().topOf(spritzerPos))
              .placeNearTarget();
      scene.idle(90);
      scene.overlay()
              .showText(60)
              .text("It will hold up to 3500mB of any fluid")
              .colored(PonderPalette.BLUE)
              .pointAt(util.vector().centerOf(spritzerPos))
              .placeNearTarget();
      scene.idle(70);
      scene.overlay()
              .showText(85)
              .text("Once it reaches 3500mB, the Spritzer can begin spraying supported fluid on its own")
              .colored(PonderPalette.MEDIUM)
              .pointAt(util.vector().topOf(spritzerPos))
              .placeNearTarget();
      scene.idle(95);
      FluidStack tankWater = new FluidStack(Fluids.WATER, 8000);
      scene.world().modifyBlockEntity(tankPos, FluidTankBlockEntity.class, be -> be.getTankInventory().setFluid(tankWater));
      scene.world().setKineticSpeed(util.select().position(motorPos), 32.0F);
      scene.world().setKineticSpeed(util.select().position(cogwheelPos), 32.0F);
      scene.world().setKineticSpeed(util.select().position(pumpPos), -32.0F);
      scene.world()
              .showSection(
                      util.select().position(tankPos).add(util.select().position(pumpPos)).add(util.select().position(cogwheelPos)).add(util.select().position(motorPos)),
                      Direction.DOWN
              );
      scene.idle(20);
      scene.world().propagatePipeChange(pumpPos);
      scene.idle(10);
      scene.overlay()
              .showText(90)
              .text("Pipes and pumps are useful for keeping the internal tank supplied while it sprays")
              .colored(PonderPalette.OUTPUT)
              .pointAt(util.vector().topOf(spritzerPos))
              .placeNearTarget();
      scene.idle(100);
      scene.overlay()
              .showText(80)
              .text("Filling the Spritzer to 3500mB arms its automatic spray cycle")
              .attachKeyFrame()
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().centerOf(spritzerPos))
              .placeNearTarget();
      scene.idle(90);

      for (int i = 0; i <= 7; i++) {
         int amount = i * 500;
         scene.world().modifyBlockEntity(spritzerPos, PerforatedSpritzerBlockEntity.class, be -> {
            be.getTankInventory().setFluid(new FluidStack(Fluids.WATER, amount));
            if (be.getFluidLevel() != null) {
               float fillState = amount / 3500.0F;
               be.getFluidLevel().chase(fillState, 0.5, Chaser.EXP);
            }
         });
         scene.idle(8);
      }

      scene.idle(20);
      scene.overlay()
              .showText(70)
              .text("...then it begins spraying the fluid downward automatically!")
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().blockSurface(spritzerPos, Direction.DOWN))
              .placeNearTarget();
      scene.idle(10);
      Vec3 sprayCenter = util.vector().centerOf(spritzerPos).add(0.0, -0.4, 0.0);

      for (int i = 0; i < 60; i++) {
         double xOffset = (Math.random() - 0.5) * 0.6;
         double zOffset = (Math.random() - 0.5) * 0.6;
         Vec3 particlePos = sprayCenter.add(xOffset, 0.0, zOffset);
         scene.effects().emitParticles(particlePos, scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_WATER, particlePos), 0.5F, 3);
         scene.idle(2);
      }

      scene.idle(30);
      scene.world().hideSection(util.select().position(farmlandPos), Direction.DOWN);
      scene.idle(5);
      scene.world().setBlocks(util.select().position(farmlandPos), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0), false);
      scene.world().showIndependentSection(util.select().position(farmlandPos), Direction.UP);
      scene.idle(20);
      scene.overlay()
              .showText(80)
              .text("The Spritzer can hydrate farmland")
              .attachKeyFrame()
              .colored(PonderPalette.BLUE)
              .pointAt(util.vector().centerOf(farmlandPos).add(0.0, 0.5, 0.0))
              .placeNearTarget();
      scene.idle(60);

      for (int i = 0; i < 40; i++) {
         Vec3 sprayPos = util.vector().centerOf(spritzerPos).add(0.0, -0.3, 0.0);
         scene.effects().emitParticles(sprayPos, scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_WATER, sprayPos), 0.3F, 2);
         scene.idle(3);
      }

      scene.idle(10);

      emitRandomWaterSpray(scene, util, spritzerPos, 40);

      scene.idle(10);
      scene.addKeyframe();
      scene.world().modifyBlock(farmlandPos, state -> Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7), false);
      scene.idle(10);
      scene.overlay()
              .showText(80)
              .text("...it can also water crops!")
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().blockSurface(farmlandPos, Direction.UP))
              .placeNearTarget();
      scene.idle(90);
      BlockPos cropPos = farmlandPos.above();
      scene.world().setBlock(cropPos, Blocks.CARROTS.defaultBlockState().setValue(CarrotBlock.AGE, 0), false);
      scene.world().showSection(util.select().position(cropPos), Direction.DOWN);
      scene.idle(30);

      for (int age : new int[]{2, 4, 6}) {
         emitRandomWaterSpray(scene, util, spritzerPos, 20);
         scene.world().modifyBlock(
                 cropPos,
                 state -> state.setValue(CarrotBlock.AGE, age),
                 false
         );
         scene.idle(20);
      }
      scene.overlay()
              .showText(95)
              .text("Crop growth can be nudged once the farmland below reaches moisture level 4; full hydration is not required")
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().blockSurface(farmlandPos, Direction.UP))
              .placeNearTarget();
      scene.idle(105);

      scene.addKeyframe();
      scene.overlay().showControls(
                      util.vector().topOf(spritzerPos),
                      Pointing.DOWN,
                      60
              )
              .rightClick()
              .withItem(new ItemStack(
                      io.hxneyw.repo.content.Items.LOGIC_BANK.get()
              ));
      scene.overlay()
              .showText(100)
              .text("Right-click a Perforated Spritzer with a Logic Bank to upgrade it into a Precision Spritzer")
              .colored(PonderPalette.INPUT)
              .pointAt(util.vector().centerOf(spritzerPos))
              .placeNearTarget();
      scene.idle(70);
      scene.world().modifyBlock(
              spritzerPos,
              state -> state.setValue(PerforatedSpritzerBlock.PRECISION, true),
              false
      );
      scene.idle(40);
      scene.overlay()
              .showText(120)
              .text("The Precision Spritzer only spends fluid when a valid selected item, block, or entity target is present")
              .attachKeyFrame()
              .colored(PonderPalette.BLUE)
              .pointAt(util.vector().centerOf(spritzerPos))
              .placeNearTarget();
      scene.idle(130);
      scene.overlay()
              .showText(120)
              .text("Empty-hand right-click opens the internal filter lists; multiple Item and Entity selections can stay active at the same time")
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().topOf(spritzerPos))
              .placeNearTarget();
      scene.idle(130);
      scene.overlay()
              .showText(110)
              .text("The Item and Entity tabs only choose which saved list you are editing; switching tabs does not disable the other filter")
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().topOf(spritzerPos))
              .placeNearTarget();
      scene.idle(120);

      scene.world().hideSection(util.select().position(cropPos), Direction.DOWN);
      scene.idle(10);
      scene.world().setBlock(
              cropPos,
              Blocks.AIR.defaultBlockState(),
              false
      );
      scene.world().setBlock(
              farmlandPos,
              Blocks.OXIDIZED_COPPER.defaultBlockState(),
              false
      );
      scene.effects().indicateSuccess(farmlandPos);
      fillSpritzer(scene, spritzerPos, AllModFluids.SULFURIC_ACID.get());
      scene.world().modifyBlockEntity(
              spritzerPos,
              PerforatedSpritzerBlockEntity.class,
              be -> be.setItemFilter(new ItemStack(Blocks.OXIDIZED_COPPER))
      );
      scene.idle(25);
      scene.overlay().showControls(
                      util.vector().topOf(spritzerPos),
                      Pointing.DOWN,
                      60
              )
              .rightClick();
      scene.overlay()
              .showText(120)
              .text("With Sulfuric Acid and a matching Item Filter, the exposed copper floor block needs three 25mB spray contacts before one oxidation stage is removed")
              .attachKeyFrame()
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().centerOf(farmlandPos))
              .placeNearTarget();
      scene.idle(130);
      for (int contact = 0; contact < 3; contact++) {
         emitSprayPulse(
                 scene,
                 util,
                 spritzerPos,
                 ModParticles.ACID_DRIP.get(),
                 new Vec3(0.0, -0.10, 0.0)
         );
         scene.idle(28);
      }
      scene.world().setBlock(
              farmlandPos,
              Blocks.WEATHERED_COPPER.defaultBlockState(),
              false
      );
      scene.effects().indicateSuccess(farmlandPos);
      scene.idle(20);
      scene.overlay()
              .showText(105)
              .text("Only the first block exposed to the spray can receive contacts; blocks behind it are ignored until exposed")
              .colored(PonderPalette.MEDIUM)
              .pointAt(util.vector().centerOf(farmlandPos))
              .placeNearTarget();
      scene.idle(115);
      scene.markAsFinished();
   }

   public static void mobAutomation(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title(
              "perforated_spritzer.mob_automation",
              "Mob Automation with the Perforated Spritzer"
      );

      
      BlockPos chestPos = util.grid().at(0, 1, 2);
      BlockPos funnelPos = util.grid().at(1, 1, 2);
      BlockPos targetPos = util.grid().at(2, 0, 2);
      BlockPos spritzerPos = util.grid().at(2, 2, 2);
      BlockPos pumpMotorPos = util.grid().at(2, 2, 1);
      BlockPos cogwheelPos = util.grid().at(2, 3, 1);
      BlockPos pumpPos = util.grid().at(2, 3, 2);
      BlockPos tankPos = util.grid().at(2, 4, 2);
      BlockPos fanPos = util.grid().at(3, 1, 2);
      BlockPos fanMotorPos = util.grid().at(4, 1, 2);

      scene.configureBasePlate(0, 0, 5);
      scene.showBasePlate();
      scene.idle(10);

      scene.world().showSection(
              util.select().position(chestPos)
                      .add(util.select().position(funnelPos)),
              Direction.EAST
      );
      scene.idle(10);

      scene.world().showSection(
              util.select().position(fanPos)
                      .add(util.select().position(fanMotorPos)),
              Direction.WEST
      );
      scene.idle(10);

      scene.world().showSection(
              util.select().position(spritzerPos)
                      .add(util.select().position(pumpMotorPos))
                      .add(util.select().position(cogwheelPos))
                      .add(util.select().position(pumpPos))
                      .add(util.select().position(tankPos)),
              Direction.DOWN
      );
      scene.idle(25);

      
      scene.overlay()
              .showText(100)
              .text("This setup uses a Perforated Spritzer to damage mobs and collect their drops")
              .attachKeyFrame()
              .colored(PonderPalette.MEDIUM)
              .pointAt(util.vector().centerOf(spritzerPos))
              .placeNearTarget();
      scene.idle(110);

      fillSupplyTank(scene, tankPos, AllModFluids.SULFURIC_ACID.get());
      fillSpritzer(scene, spritzerPos, AllModFluids.SULFURIC_ACID.get());
      startPump(scene, util, pumpMotorPos, cogwheelPos, pumpPos);
      scene.world().propagatePipeChange(pumpPos);
      scene.idle(20);

      createStationaryBabyZombie(scene, util, targetPos);
      scene.idle(15);

      
      scene.overlay()
              .showText(120)
              .text("Sulfuric Acid applies Acid Burn and repeatedly damages living entities beneath the Spritzer")
              .attachKeyFrame()
              .colored(PonderPalette.RED)
              .pointAt(util.vector().centerOf(targetPos).add(0.0, 0.9, 0.0))
              .placeNearTarget();

      for (int pulse = 0; pulse < 8; pulse++) {
         emitSprayPulse(
                 scene,
                 util,
                 spritzerPos,
                 ModParticles.ACID_DRIP.get(),
                 new Vec3(0.0, -0.10, 0.0)
         );

         if (pulse % 2 == 0) {
            scene.world().modifyEntities(Zombie.class, zombie -> {
               zombie.hurtDuration = 10;
               zombie.hurtTime = 10;
            });
         }

         scene.idle(12);
      }
      scene.idle(30);

      scene.world().modifyEntities(
              Zombie.class,
              zombie -> zombie.remove(RemovalReason.KILLED)
      );

      Vec3 dropPos = util.vector().centerOf(targetPos).add(0.0, 0.65, 0.0);
      scene.world().createItemEntity(
              dropPos,
              Vec3.ZERO,
              new ItemStack(Items.ROTTEN_FLESH, 2)
      );
      scene.idle(15);

      
      scene.overlay()
              .showText(90)
              .text("When the mob is defeated, its drops remain beneath the Spritzer")
              .colored(PonderPalette.GREEN)
              .pointAt(dropPos)
              .placeNearTarget();
      scene.idle(100);

      scene.world().setKineticSpeed(
              util.select().position(fanMotorPos),
              -32.0F
      );
      scene.world().setKineticSpeed(
              util.select().position(fanPos),
              -32.0F
      );

      
      scene.overlay()
              .showText(110)
              .text("An Encased Fan pushes those dropped items toward the Funnel")
              .attachKeyFrame()
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().centerOf(fanPos))
              .placeNearTarget();

      for (int tick = 0; tick < 22; tick++) {
         scene.world().modifyEntities(
                 ItemEntity.class,
                 item -> item.setDeltaMovement(-0.085, 0.015, 0.0)
         );
         scene.idle(2);
      }
      scene.idle(70);

      
      scene.overlay()
              .showText(100)
              .text("The Funnel inserts the loot into the attached Chest automatically")
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().blockSurface(chestPos, Direction.UP))
              .placeNearTarget();

      scene.idle(25);
      scene.world().modifyEntities(
              ItemEntity.class,
              item -> item.remove(RemovalReason.DISCARDED)
      );
      scene.idle(85);

      scene.world().setKineticSpeed(
              util.select().position(fanMotorPos)
                      .add(util.select().position(fanPos)),
              0.0F
      );

      scene.addKeyframe();
      fillSupplyTank(scene, tankPos, Fluids.LAVA);
      fillSpritzer(scene, spritzerPos, Fluids.LAVA);
      scene.idle(20);

      createStationaryBabyZombie(scene, util, targetPos);
      scene.idle(15);

      
      scene.overlay()
              .showText(110)
              .text("Supplying Lava instead ignites entities caught beneath the Spritzer")
              .attachKeyFrame()
              .colored(PonderPalette.OUTPUT)
              .pointAt(util.vector().centerOf(targetPos).add(0.0, 0.9, 0.0))
              .placeNearTarget();


      for (int pulse = 0; pulse < 5; pulse++) {
         emitSprayPulse(
                 scene,
                 util,
                 spritzerPos,
                 ParticleTypes.FALLING_LAVA,
                 new Vec3(0.0, -0.08, 0.0)
         );

         scene.world().modifyEntities(Zombie.class, zombie -> {
            zombie.igniteForSeconds(5.0F);
            zombie.setRemainingFireTicks(100);
            zombie.setSharedFlagOnFire(true);
            zombie.hurtDuration = 10;
            zombie.hurtTime = 10;
         });
         emitEntityFirePulse(scene, util, targetPos, pulse);
         scene.idle(10);
      }

      for (int pulse = 5; pulse < 17; pulse++) {
         emitEntityFirePulse(scene, util, targetPos, pulse);
         scene.idle(5);
      }
      scene.idle(10);

      scene.addKeyframe();
      fillSupplyTank(scene, tankPos, Fluids.WATER);
      fillSpritzer(scene, spritzerPos, Fluids.WATER);
      scene.idle(20);

      
      scene.overlay()
              .showText(110)
              .text("Water has the opposite use, extinguishing burning entities below")
              .attachKeyFrame()
              .colored(PonderPalette.BLUE)
              .pointAt(util.vector().centerOf(targetPos).add(0.0, 0.9, 0.0))
              .placeNearTarget();

      for (int pulse = 0; pulse < 4; pulse++) {
         emitSprayPulse(
                 scene,
                 util,
                 spritzerPos,
                 ParticleTypes.FALLING_WATER,
                 new Vec3(0.0, -0.10, 0.0)
         );

         if (pulse == 0) {
            scene.world().modifyEntities(Zombie.class, zombie -> {
               zombie.clearFire();
               zombie.setRemainingFireTicks(0);
               zombie.setSharedFlagOnFire(false);
            });
         }

         if (pulse == 1) {
            emitEntityExtinguishBurst(scene, util, targetPos);
         }
         scene.idle(12);
      }
      scene.idle(72);

      
      scene.overlay()
              .showText(110)
              .text("Choose the supplied fluid to damage mobs, ignite them, or extinguish fire")
              .colored(PonderPalette.GREEN)
              .pointAt(util.vector().centerOf(spritzerPos))
              .placeNearTarget();
      scene.idle(120);
      scene.markAsFinished();
   }

   private static void emitRandomWaterSpray(
           CreateSceneBuilder scene,
           SceneBuildingUtil util,
           BlockPos spritzerPos,
           int pulses
   ) {
      Vec3 center = util.vector().centerOf(spritzerPos);

      for (int pulse = 0; pulse < pulses; pulse++) {
         double xOffset = (Math.random() - 0.5) * 0.6;
         double zOffset = (Math.random() - 0.5) * 0.6;
         Vec3 sprayPos = center.add(xOffset, -0.3, zOffset);
         scene.effects().emitParticles(
                 sprayPos,
                 scene.effects().simpleParticleEmitter(
                         ParticleTypes.FALLING_WATER,
                         sprayPos
                 ),
                 0.3F,
                 2
         );
         scene.idle(3);
      }
   }

   private static void startPump(
           CreateSceneBuilder scene,
           SceneBuildingUtil util,
           BlockPos motorPos,
           BlockPos cogwheelPos,
           BlockPos pumpPos
   ) {
      scene.world().setKineticSpeed(
              util.select().position(motorPos),
              32.0F
      );
      scene.world().setKineticSpeed(
              util.select().position(cogwheelPos),
              32.0F
      );
      scene.world().setKineticSpeed(
              util.select().position(pumpPos),
              -32.0F
      );
   }

   private static void fillSupplyTank(
           CreateSceneBuilder scene,
           BlockPos tankPos,
           Fluid fluid
   ) {
      scene.world().modifyBlockEntity(
              tankPos,
              FluidTankBlockEntity.class,
              tank -> tank.getTankInventory().setFluid(new FluidStack(fluid, 8000))
      );
   }

   private static void fillSpritzer(
           CreateSceneBuilder scene,
           BlockPos spritzerPos,
           Fluid fluid
   ) {
      scene.world().modifyBlockEntity(
              spritzerPos,
              PerforatedSpritzerBlockEntity.class,
              spritzer -> {
                 spritzer.getTankInventory().setFluid(new FluidStack(fluid, 3500));
                 if (spritzer.getFluidLevel() != null) {
                    spritzer.getFluidLevel().chase(1.0, 0.5, Chaser.EXP);
                 }
              }
      );
   }

   private static void createStationaryBabyZombie(
           CreateSceneBuilder scene,
           SceneBuildingUtil util,
           BlockPos targetPos
   ) {
      scene.world().createEntity(world -> {
         Zombie zombie = new Zombie(EntityType.ZOMBIE, world);
         Vec3 position = util.vector().centerOf(targetPos).add(0.0, 0.5, 0.0);

         zombie.setPos(position.x, position.y, position.z);
         zombie.setYRot(90.0F);
         zombie.setYHeadRot(90.0F);
         zombie.setYBodyRot(90.0F);
         zombie.setBaby(true);
         zombie.setNoAi(true);
         zombie.setSilent(true);
         zombie.setDeltaMovement(Vec3.ZERO);
         zombie.setNoGravity(false);
         zombie.xOld = position.x;
         zombie.yOld = position.y;
         zombie.zOld = position.z;
         zombie.xo = position.x;
         zombie.yo = position.y;
         zombie.zo = position.z;
         return zombie;
      });
   }

   private static void emitSprayPulse(
           CreateSceneBuilder scene,
           SceneBuildingUtil util,
           BlockPos spritzerPos,
           SimpleParticleType particle,
           Vec3 motion
   ) {
      for (int row = 0; row < 5; row++) {
         double z = 0.25 + row * 0.125;

         for (int column = 0; column < 4; column++) {
            double x = 0.25 + column * 0.1875;
            Vec3 particlePos = util.vector()
                    .topOf(spritzerPos)
                    .add(x - 0.5, -0.95, z - 0.5);

            scene.effects().emitParticles(
                    particlePos,
                    scene.effects().simpleParticleEmitter(particle, motion),
                    1.0F,
                    1
            );
         }
      }
   }

   private static void emitEntityFirePulse(
           CreateSceneBuilder scene,
           SceneBuildingUtil util,
           BlockPos targetPos,
           int pulse
   ) {
      Vec3 bodyBase = util.vector().centerOf(targetPos).add(0.0, 0.55, 0.0);
      double[][] anchors = {
              {-0.18, 0.10, -0.10},
              {0.16, 0.18, 0.10},
              {-0.10, 0.42, 0.12},
              {0.12, 0.58, -0.08},
              {0.00, 0.76, 0.02}
      };

      for (int index = 0; index < anchors.length; index++) {
         double[] anchor = anchors[index];
         Vec3 flamePos = bodyBase.add(anchor[0], anchor[1], anchor[2]);
         SimpleParticleType flame = (index + pulse) % 3 == 0
                 ? ParticleTypes.FLAME
                 : ParticleTypes.SMALL_FLAME;

         scene.effects().emitParticles(
                 flamePos,
                 scene.effects().simpleParticleEmitter(
                         flame,
                         new Vec3(0.0, 0.018, 0.0)
                 ),
                 1.0F,
                 1
         );
      }

      if (pulse % 2 == 0) {
         Vec3 smokePos = bodyBase.add(0.0, 0.92, 0.0);
         scene.effects().emitParticles(
                 smokePos,
                 scene.effects().simpleParticleEmitter(
                         ParticleTypes.SMOKE,
                         new Vec3(0.0, 0.025, 0.0)
                 ),
                 1.0F,
                 2
         );
      }
   }

   private static void emitEntityExtinguishBurst(
           CreateSceneBuilder scene,
           SceneBuildingUtil util,
           BlockPos targetPos
   ) {
      Vec3 bodyCenter = util.vector().centerOf(targetPos).add(0.0, 0.95, 0.0);
      double[][] splashOffsets = {
              {-0.20, 0.00, -0.10},
              {0.20, 0.05, 0.10},
              {-0.10, 0.28, 0.15},
              {0.12, 0.40, -0.12},
              {0.00, 0.62, 0.00}
      };

      for (double[] offset : splashOffsets) {
         Vec3 splashPos = bodyCenter.add(offset[0], offset[1], offset[2]);
         scene.effects().emitParticles(
                 splashPos,
                 scene.effects().simpleParticleEmitter(
                         ParticleTypes.SPLASH,
                         new Vec3(offset[0] * 0.08, 0.035, offset[2] * 0.08)
                 ),
                 1.0F,
                 2
         );
      }

      scene.effects().emitParticles(
              bodyCenter.add(0.0, 0.55, 0.0),
              scene.effects().simpleParticleEmitter(
                      ParticleTypes.SMOKE,
                      new Vec3(0.0, 0.02, 0.0)
              ),
              1.0F,
              4
      );
   }

}