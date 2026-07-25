package io.hxneyw.repo.ponder;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.content.registry.ModParticles;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
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
import net.minecraft.world.level.block.state.BlockState;
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
         .showText(70)
         .text("It cannot spray fluid on its own")
         .colored(PonderPalette.MEDIUM)
         .pointAt(util.vector().topOf(spritzerPos))
         .placeNearTarget();
      scene.idle(80);
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
         .showText(80)
         .text("It requires pressure from a pump once its tank is full")
         .colored(PonderPalette.OUTPUT)
         .pointAt(util.vector().topOf(spritzerPos))
         .placeNearTarget();
      scene.idle(90);
      scene.overlay()
         .showText(70)
         .text("When the pump pushes fluid into a full Spritzer...")
         .attachKeyFrame()
         .colored(PonderPalette.GREEN)
         .pointAt(util.vector().centerOf(spritzerPos))
         .placeNearTarget();
      scene.idle(80);

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
         .showText(60)
         .text("...it sprays the fluid downward!")
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
      scene.world().setBlocks(util.select().position(farmlandPos), (BlockState)Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0), false);
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

      for (int i = 0; i < 40; i++) {
         double xOffset = (Math.random() - 0.5) * 0.6;
         double zOffset = (Math.random() - 0.5) * 0.6;
         Vec3 sprayPos = util.vector().centerOf(spritzerPos).add(xOffset, -0.3, zOffset);
         scene.effects().emitParticles(sprayPos, scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_WATER, sprayPos), 0.3F, 2);
         scene.idle(3);
      }

      scene.idle(10);
      scene.addKeyframe();
      scene.world().modifyBlock(farmlandPos, state -> (BlockState)Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7), false);
      scene.idle(10);
      scene.overlay()
         .showText(80)
         .text("...it can also water crops!")
         .colored(PonderPalette.GREEN)
         .pointAt(util.vector().blockSurface(farmlandPos, Direction.UP))
         .placeNearTarget();
      scene.idle(90);
      BlockPos cropPos = farmlandPos.above();
      scene.world().setBlock(cropPos, (BlockState)Blocks.CARROTS.defaultBlockState().setValue(CarrotBlock.AGE, 0), false);
      scene.world().showSection(util.select().position(cropPos), Direction.DOWN);
      scene.idle(30);

      for (int i = 0; i < 20; i++) {
         double xOffset = (Math.random() - 0.5) * 0.6;
         double zOffset = (Math.random() - 0.5) * 0.6;
         Vec3 sprayPos = util.vector().centerOf(spritzerPos).add(xOffset, -0.3, zOffset);
         scene.effects().emitParticles(sprayPos, scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_WATER, sprayPos), 0.3F, 2);
         scene.idle(3);
      }

      scene.world().modifyBlock(cropPos, state -> (BlockState)state.setValue(CarrotBlock.AGE, 2), false);
      scene.idle(20);

      for (int i = 0; i < 20; i++) {
         double xOffset = (Math.random() - 0.5) * 0.6;
         double zOffset = (Math.random() - 0.5) * 0.6;
         Vec3 sprayPos = util.vector().centerOf(spritzerPos).add(xOffset, -0.3, zOffset);
         scene.effects().emitParticles(sprayPos, scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_WATER, sprayPos), 0.3F, 2);
         scene.idle(3);
      }

      scene.world().modifyBlock(cropPos, state -> (BlockState)state.setValue(CarrotBlock.AGE, 4), false);
      scene.idle(20);

      for (int i = 0; i < 20; i++) {
         double xOffset = (Math.random() - 0.5) * 0.6;
         double zOffset = (Math.random() - 0.5) * 0.6;
         Vec3 sprayPos = util.vector().centerOf(spritzerPos).add(xOffset, -0.3, zOffset);
         scene.effects().emitParticles(sprayPos, scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_WATER, sprayPos), 0.3F, 2);
         scene.idle(3);
      }

      scene.world().modifyBlock(cropPos, state -> (BlockState)state.setValue(CarrotBlock.AGE, 6), false);
      scene.idle(20);
      scene.overlay()
         .showText(80)
         .text("Making their growth speed increase slightly only when farmland beneath them is fully hydrated")
         .colored(PonderPalette.GREEN)
         .pointAt(util.vector().blockSurface(farmlandPos, Direction.UP))
         .placeNearTarget();
      scene.idle(90);
      scene.markAsFinished();
   }

   public static void mobAutomation(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("perforated_spritzer.mob_automation", "Mob Automation with the Spritzer");
      BlockPos spritzerPos = util.grid().at(2, 2, 2);
      BlockPos motorPos = util.grid().at(2, 2, 1);
      BlockPos pumpPos = util.grid().at(2, 3, 2);
      BlockPos cogwheelPos = util.grid().at(2, 3, 1);
      BlockPos tankPos = util.grid().at(2, 4, 2);
      BlockPos zombiePos = util.grid().at(2, 0, 2);
      BlockPos fanMotorPos = util.grid().at(0, 2, 2);
      BlockPos fanPos = util.grid().at(1, 2, 2);
      BlockPos funnelPos = util.grid().at(3, 0, 2);
      BlockPos chestPos = util.grid().at(4, 0, 2);
      scene.configureBasePlate(0, 0, 5);
      scene.showBasePlate();
      scene.idle(10);
      scene.world().showSection(util.select().position(tankPos), Direction.DOWN);
      scene.idle(10);
      scene.world().showSection(util.select().position(pumpPos), Direction.DOWN);
      scene.idle(5);
      scene.world().showSection(util.select().position(cogwheelPos), Direction.DOWN);
      scene.idle(5);
      scene.world().showSection(util.select().position(spritzerPos), Direction.DOWN);
      scene.idle(5);
      scene.world().showSection(util.select().position(motorPos), Direction.DOWN);
      scene.idle(15);
      scene.world()
         .modifyBlockEntity(
            tankPos, FluidTankBlockEntity.class, be -> be.getTankInventory().setFluid(new FluidStack((Fluid)AllModFluids.SULFURIC_ACID.get(), 8000))
         );
      scene.world().modifyBlockEntity(spritzerPos, PerforatedSpritzerBlockEntity.class, be -> {
         be.getTankInventory().setFluid(new FluidStack((Fluid)AllModFluids.SULFURIC_ACID.get(), 3500));
         if (be.getFluidLevel() != null) {
            be.getFluidLevel().chase(1.0, 0.5, Chaser.EXP);
         }
      });
      scene.idle(10);
      scene.world().showSection(util.select().position(chestPos), Direction.DOWN);
      scene.idle(5);
      scene.world().showSection(util.select().position(funnelPos), Direction.DOWN);
      scene.idle(10);
      scene.world().showSection(util.select().position(fanMotorPos), Direction.DOWN);
      scene.idle(5);
      scene.world().showSection(util.select().position(fanPos), Direction.DOWN);
      scene.idle(15);
      scene.world().createEntity(w -> {
         Zombie zombie = new Zombie(EntityType.ZOMBIE, w);
         Vec3 pos = util.vector().centerOf(zombiePos).add(0.0, 0.5, 0.0);
         zombie.setPos(pos.x, pos.y, pos.z);
         zombie.setYRot(90.0F);
         zombie.setYHeadRot(90.0F);
         zombie.setYBodyRot(90.0F);
         zombie.setBaby(true);
         zombie.setNoAi(true);
         zombie.setSilent(true);
         zombie.setDeltaMovement(Vec3.ZERO);
         zombie.setNoGravity(false);
         zombie.xOld = pos.x;
         zombie.yOld = pos.y;
         zombie.zOld = pos.z;
         zombie.xo = pos.x;
         zombie.yo = pos.y;
         zombie.zo = pos.z;
         return zombie;
      });
      scene.idle(20);
      scene.overlay()
         .showText(240)
         .text(
            "The Spritzer, when filled with sulfuric acid will cause the corrosive acid burn effect and effectively hurt mobs, to kill and automate their loot in a creative new way."
         )
         .attachKeyFrame()
         .colored(PonderPalette.RED)
         .pointAt(util.vector().centerOf(spritzerPos))
         .placeNearTarget();
      scene.idle(250);
      scene.world().setKineticSpeed(util.select().position(motorPos), 32.0F);
      scene.world().setKineticSpeed(util.select().position(cogwheelPos), 32.0F);
      scene.world().setKineticSpeed(util.select().position(pumpPos), -32.0F);
      scene.world().propagatePipeChange(pumpPos);
      scene.idle(30);

      for (int spray = 0; spray < 10; spray++) {
         double[][] holePositions = new double[][]{
            {0.25, 0.25},
            {0.4375, 0.25},
            {0.625, 0.25},
            {0.8125, 0.25},
            {0.25, 0.375},
            {0.4375, 0.375},
            {0.625, 0.375},
            {0.8125, 0.375},
            {0.25, 0.5},
            {0.4375, 0.5},
            {0.625, 0.5},
            {0.8125, 0.5},
            {0.25, 0.625},
            {0.4375, 0.625},
            {0.625, 0.625},
            {0.8125, 0.625},
            {0.25, 0.75},
            {0.4375, 0.75},
            {0.625, 0.75},
            {0.8125, 0.75}
         };

         for (double[] hole : holePositions) {
            Vec3 sprayPos = util.vector().topOf(spritzerPos).add(hole[0] - 0.5, -0.95, hole[1] - 0.5);
            scene.effects()
               .emitParticles(
                  sprayPos, scene.effects().simpleParticleEmitter((SimpleParticleType)ModParticles.ACID_DRIP.get(), new Vec3(0.0, -0.1, 0.0)), 1.0F, 1
               );
         }

         if (spray % 2 == 0) {
            scene.world().modifyEntities(Zombie.class, zombie -> {
               zombie.hurtDuration = 10;
               zombie.hurtTime = 10;
            });
         }

         if (spray == 8) {
            scene.world().modifyEntities(Zombie.class, zombie -> zombie.remove(RemovalReason.KILLED));
            Vec3 dropPos = util.vector().centerOf(zombiePos).add(0.0, 0.5, 0.0);
            scene.world().createItemEntity(dropPos, util.vector().of(0.0, 0.0, 0.0), new ItemStack(Items.ROTTEN_FLESH, 2));
         }

         scene.idle(15);
      }

      scene.idle(20);
      scene.world().setKineticSpeed(util.select().position(fanMotorPos), -32.0F);
      scene.world().setKineticSpeed(util.select().position(fanPos), -32.0F);
      scene.idle(10);
      scene.overlay()
         .showText(100)
         .text("Items are blown into the collection system")
         .colored(PonderPalette.GREEN)
         .pointAt(util.vector().topOf(fanPos))
         .placeNearTarget();
      scene.idle(110);
      scene.world().modifyEntities(ItemEntity.class, item -> item.setDeltaMovement(-0.3, 0.05, 0.0));
      scene.idle(60);
      scene.idle(6);
      scene.world().modifyEntities(ItemEntity.class, item -> item.remove(RemovalReason.DISCARDED));
      scene.idle(14);
      scene.overlay()
         .showText(100)
         .text("Items are collected in the chest!")
         .colored(PonderPalette.GREEN)
         .pointAt(util.vector().centerOf(chestPos))
         .placeNearTarget();
      scene.idle(110);
      scene.addKeyframe();
      scene.world().modifyBlockEntity(tankPos, FluidTankBlockEntity.class, be -> be.getTankInventory().setFluid(new FluidStack(Fluids.LAVA, 8000)));
      scene.world().modifyBlockEntity(spritzerPos, PerforatedSpritzerBlockEntity.class, be -> {
         be.getTankInventory().setFluid(new FluidStack(Fluids.LAVA, 3500));
         if (be.getFluidLevel() != null) {
            be.getFluidLevel().chase(1.0, 0.5, Chaser.EXP);
         }
      });
      scene.idle(20);
      scene.world().createEntity(w -> {
         Zombie zombie = new Zombie(EntityType.ZOMBIE, w);
         Vec3 pos = util.vector().centerOf(zombiePos).add(0.0, 0.5, 0.0);
         zombie.setPos(pos.x, pos.y, pos.z);
         zombie.setYRot(90.0F);
         zombie.setYHeadRot(90.0F);
         zombie.setYBodyRot(90.0F);
         zombie.setBaby(true);
         zombie.setNoAi(true);
         zombie.setSilent(true);
         zombie.setDeltaMovement(Vec3.ZERO);
         zombie.setNoGravity(false);
         zombie.xOld = pos.x;
         zombie.yOld = pos.y;
         zombie.zOld = pos.z;
         zombie.xo = pos.x;
         zombie.yo = pos.y;
         zombie.zo = pos.z;
         return zombie;
      });
      scene.idle(10);
      scene.overlay()
         .showText(100)
         .text("The Spritzer also sprays lava, damaging entities by setting them on fire")
         .colored(PonderPalette.OUTPUT)
         .pointAt(util.vector().blockSurface(spritzerPos, Direction.DOWN))
         .placeNearTarget();
      scene.idle(110);

      for (int spray = 0; spray < 12; spray++) {
         for (int i = 0; i < 10; i++) {
            double xOffset = (Math.random() - 0.5) * 0.6;
            double zOffset = (Math.random() - 0.5) * 0.6;
            Vec3 sprayPos = util.vector().centerOf(spritzerPos).add(xOffset, -0.3, zOffset);
            scene.effects().emitParticles(sprayPos, scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_LAVA, sprayPos), 0.8F, 3);
         }

         scene.world().modifyEntities(Zombie.class, zombie -> {
            zombie.setRemainingFireTicks(40);
            zombie.hurtDuration = 10;
            zombie.hurtTime = 10;
         });
         if (spray == 10) {
            scene.world().modifyEntities(Zombie.class, zombie -> zombie.remove(RemovalReason.KILLED));
            Vec3 cookedDropPos = util.vector().centerOf(zombiePos).add(0.0, 0.5, 0.0);
            scene.world().createItemEntity(cookedDropPos, util.vector().of(0.0, 0.0, 0.0), new ItemStack(Items.ROTTEN_FLESH, 1));
         }

         scene.idle(15);
      }

      scene.idle(20);
      scene.world().modifyEntities(ItemEntity.class, item -> item.setDeltaMovement(-0.3, 0.05, 0.0));
      scene.idle(60);
      scene.overlay()
         .showText(100)
         .text("Loot collected successfully!")
         .colored(PonderPalette.GREEN)
         .pointAt(util.vector().centerOf(chestPos))
         .placeNearTarget();
      scene.idle(110);
      scene.idle(6);
      scene.world().modifyEntities(ItemEntity.class, item -> item.remove(RemovalReason.DISCARDED));
      scene.idle(14);
      scene.markAsFinished();
   }
}
