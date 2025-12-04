package io.hxneyw.repo.content.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EmberCatalystItem extends Item {
    public EmberCatalystItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(@NotNull ItemStack stack, ItemEntity entity) {
        Level level = entity.level();

        // Spawn subtle warm glow particles (client side only)
        if (level.isClientSide && level.random.nextFloat() < 0.15f) { // 15% chance
            // Use FLAME particles (orange) for warm glow
            level.addParticle(ParticleTypes.FLAME,
                    entity.getX() + (level.random.nextDouble() - 0.5) * 0.15,
                    entity.getY() + level.random.nextDouble() * 0.2,
                    entity.getZ() + (level.random.nextDouble() - 0.5) * 0.15,
                    0, 0.01, 0); // Very slow rise
        }

        return false;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false; // No enchantment glint
    }
}