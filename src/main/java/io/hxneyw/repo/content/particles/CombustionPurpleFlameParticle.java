package io.hxneyw.repo.content.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CombustionPurpleFlameParticle extends RisingParticle {

    protected CombustionPurpleFlameParticle(ClientLevel level, double x, double y, double z,
                                            double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.lifetime = 20; // How long the particle lives (in ticks)
        this.gravity = -0.01F; // Negative = rises upward
        this.hasPhysics = false; // No collision

        // Randomize initial size
        this.quadSize *= 0.5F + this.random.nextFloat() * 0.5F;
    }

    @Override
    public void tick() {
        super.tick();

        // Fade out as particle ages
        if (this.age > this.lifetime / 2) {
            this.setAlpha(1.0F - ((float) this.age - (float) (this.lifetime / 2)) / (float) this.lifetime);
        }

        // Shrink over time
        this.quadSize *= 0.96F;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            CombustionPurpleFlameParticle particle = new CombustionPurpleFlameParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}