package io.hxneyw.repo.content.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CombustionPurpleFlameParticle extends RisingParticle {
   protected CombustionPurpleFlameParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      super(level, x, y, z, xSpeed, ySpeed, zSpeed);
      this.lifetime = 20;
      this.gravity = -0.01F;
      this.hasPhysics = false;
      this.quadSize = this.quadSize * (0.5F + this.random.nextFloat() * 0.5F);
   }

   public void tick() {
      super.tick();
      if (this.age > this.lifetime / 2) {
         this.setAlpha(1.0F - ((float)this.age - this.lifetime / 2) / this.lifetime);
      }

      this.quadSize *= 0.96F;
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(
         @NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
      ) {
         CombustionPurpleFlameParticle particle = new CombustionPurpleFlameParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
         particle.pickSprite(this.sprites);
         return particle;
      }
   }
}
