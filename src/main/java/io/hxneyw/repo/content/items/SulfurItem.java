package io.hxneyw.repo.content.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SulfurItem extends Item {
    public SulfurItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(@NotNull ItemStack stack, ItemEntity entity) {
        Level level = entity.level();

        // SERVER ONLY: Clear fire (affects game state)
        if (entity.isOnFire() && !level.isClientSide) {
            entity.clearFire();
            entity.setRemainingFireTicks(0);
        }

        boolean inDanger = entity.isInLava() ||
                level.getBlockState(entity.blockPosition()).getBlock() == net.minecraft.world.level.block.Blocks.FIRE ||
                level.getBlockState(entity.blockPosition()).getBlock() == net.minecraft.world.level.block.Blocks.SOUL_FIRE;

        // CLIENT ONLY: Spawn particles (visual only)
        if (inDanger && level.isClientSide) {
            if (level.random.nextFloat() < 0.3f) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        entity.getX() + (level.random.nextDouble() - 0.5) * 0.2,
                        entity.getY() + level.random.nextDouble() * 0.3,
                        entity.getZ() + (level.random.nextDouble() - 0.5) * 0.2,
                        0, 0.02, 0);
            }
        }

        // SERVER ONLY: Apply damage (affects game state)
        if (inDanger && !level.isClientSide && level.getGameTime() % 20 == 0) {
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                            entity.getBoundingBox().inflate(3))
                    .forEach(livingEntity -> livingEntity.hurt(level.damageSources().magic(), 1.0F));
        }

        return false;
    }
}