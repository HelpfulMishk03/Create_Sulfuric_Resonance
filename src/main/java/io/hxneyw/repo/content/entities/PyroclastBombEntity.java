package io.hxneyw.repo.content.entities;

import io.hxneyw.repo.content.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

// Pyroclastic Powder Entity - A throwable explosive projectile
// Extends ThrowableItemProjectile to inherit flying physics, collision detection, and rendering
public class PyroclastBombEntity extends ThrowableItemProjectile {

    // Constructor #1: Used when loading entity from saved game data
    // Called when Minecraft needs to recreate the entity (e.g., loading a saved world with projectile mid-flight)
    public PyroclastBombEntity(EntityType<? extends PyroclastBombEntity> type, Level level) {
        super(type, level); // Call parent class constructor to set up basic entity properties
    }

    // Constructor #2: Used when a player throws the powder
    // Called by PyroclastBombItem when player right-clicks
    public PyroclastBombEntity(Level level, LivingEntity shooter) {
        super(ModEntities.PYROCLAST_BOMB.get(), shooter, level); // Tell parent who threw it and set entity type
    }

    // Defines which item to render while the projectile is flying through the air
    // You see a small spinning item - this method determines what that item looks like
    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.PYROCLAST_BOMB.get(); // Show our custom powder item while flying
    }

    // Called when the projectile hits something (block, entity, or ground)
    // This is where we add our custom explosion and particle effects
    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result); // Let parent class do basic hit handling first

        // SERVER-SIDE ONLY: Handle game logic (explosion, block destruction, damage, AND particles)
        // The server is the "source of truth" - it decides what actually happens in the game
        if (!this.level().isClientSide) {
            // Create SMALL explosion at impact point
            this.level().explode(
                    this,                              // Source entity - who/what caused this explosion
                    this.getX(),                       // X coordinate - exact position in world
                    this.getY(),                       // Y coordinate - vertical position
                    this.getZ(),                       // Z coordinate - depth position
                    1.50F,                              // Explosion radius (0.8 = very small, barely breaks blocks)
                    false,                             // Should create fire? false = no fire spread
                    Level.ExplosionInteraction.BLOCK   // BLOCK = destroys blocks and drops items
            );

            // Damage nearby mobs with reduced damage (1.5 hearts = 3.0F damage)
            this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5))
                    .forEach(entity -> {
                        if (entity != this.getOwner()) { // Don't damage the thrower
                            entity.hurt(this.damageSources().thrown(this, this.getOwner()), 4.75F); // 2.2ish hearts
                        }
                    });

            // SERVER-SIDE: Spawn flame particles (will sync to all clients automatically)
            // ServerLevel has methods to send particles to all nearby clients
            if (this.level() instanceof ServerLevel serverLevel) {
                // Create 15 flame particles in a burst effect
                for (int i = 0; i < 18; i++) {
                    serverLevel.sendParticles(
                            ParticleTypes.FLAME,                        // Type of particle (orange flame)
                            this.getX(),                                // Spawn X position (where explosion happened)
                            this.getY(),                                // Spawn Y position
                            this.getZ(),                                // Spawn Z position
                            1,                                          // Number of particles (1 per call)
                            (this.random.nextDouble() - 0.5) * 0.5,    // X offset/velocity
                            this.random.nextDouble() * 0.5,             // Y offset/velocity (rises upward)
                            (this.random.nextDouble() - 0.5) * 0.5,    // Z offset/velocity
                            0.1                                         // Speed multiplier
                    );
                }
            }

            this.discard(); // Remove this entity from the world (it exploded, so it shouldn't exist anymore)
        }
    }

    // Called every game tick (20 times per second) while the projectile exists
    // Used to add continuous effects while the powder is flying through the air
    @Override
    public void tick() {
        super.tick(); // Let parent handle movement physics, gravity, etc.

        // Add trailing smoke particles while flying (50% chance each tick)
        // CLIENT-SIDE ONLY: Visual effect
        if (this.level().isClientSide && this.random.nextFloat() < 0.5f) {
            this.level().addParticle(
                    ParticleTypes.SMOKE,  // Gray smoke particle
                    this.getX(),          // Current X position
                    this.getY(),          // Current Y position
                    this.getZ(),          // Current Z position
                    0, 0, 0               // No velocity - smoke stays where spawned (creates trail)
            );
        }
    }
}