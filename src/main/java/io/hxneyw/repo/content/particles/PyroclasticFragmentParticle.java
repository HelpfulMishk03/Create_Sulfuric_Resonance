package io.hxneyw.repo.content.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class PyroclasticFragmentParticle extends TextureSheetParticle {
   private final float spin;

   protected PyroclasticFragmentParticle(
      ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
   ) {
      super(level, x, y, z, xSpeed, ySpeed, zSpeed);
      this.xd = xSpeed;
      this.yd = ySpeed;
      this.zd = zSpeed;
      this.gravity = 0.72F;
      this.friction = 0.84F;
      this.hasPhysics = true;
      this.lifetime = 15 + this.random.nextInt(12);
      this.quadSize = 0.055F + this.random.nextFloat() * 0.055F;
      this.roll = this.random.nextFloat() * ((float)Math.PI * 2.0F);
      this.spin = (this.random.nextFloat() - 0.5F) * 0.38F;
   }

   @Override
   public void tick() {
      this.oRoll = this.roll;
      this.roll += this.spin;
      super.tick();
      if (this.age > this.lifetime * 0.62F) {
         this.alpha = Math.max(0.0F, (this.lifetime - this.age) / (this.lifetime * 0.38F));
      }
      this.quadSize *= 0.975F;
   }

   @Override
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

      @Override
      public Particle createParticle(
         @NotNull SimpleParticleType type,
         @NotNull ClientLevel level,
         double x,
         double y,
         double z,
         double xSpeed,
         double ySpeed,
         double zSpeed
      ) {
         PyroclasticFragmentParticle particle = new PyroclasticFragmentParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
         particle.pickSprite(this.sprites);
         return particle;
      }
   }
}
