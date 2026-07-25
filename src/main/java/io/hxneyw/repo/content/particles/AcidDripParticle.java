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
public class AcidDripParticle extends TextureSheetParticle {
   protected AcidDripParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      super(level, x, y, z, xSpeed, ySpeed, zSpeed);
      this.lifetime = 60;
      this.gravity = 0.4F;
      this.hasPhysics = true;
      this.xd = 0.0;
      this.yd = ySpeed;
      this.zd = 0.0;
      this.rCol = 0.85F;
      this.gCol = 1.0F;
      this.bCol = 0.0F;
      this.quadSize = 0.05F + this.random.nextFloat() * 0.02F;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.yd = this.yd - 0.04 * this.gravity;
         this.move(this.xd, this.yd, this.zd);
         if (this.age > this.lifetime - 10) {
            this.alpha = (this.lifetime - this.age) / 10.0F;
         }

         if (this.onGround) {
            this.remove();
         }

         if (Math.abs(this.yd) < 0.001 && this.age > 5) {
            this.remove();
         }
      }
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
         AcidDripParticle particle = new AcidDripParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
         particle.pickSprite(this.sprites);
         return particle;
      }
   }
}
