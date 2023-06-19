package com.unixkitty.vampire_blood.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class CharmedFeedbackParticle extends TextureSheetParticle
{

    public CharmedFeedbackParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet)
    {
        super(level, x, y - 0.5D, z, 0, 0, 0);

        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 0.86F;
        this.xd *= 0.01F;
        this.yd *= 0.01F;
        this.zd *= 0.01F;
        this.yd += 0.1D;
        this.quadSize *= 1.0F;
        this.lifetime = 16;
        this.hasPhysics = false;

        setSpriteFromAge(spriteSet);
    }

    @Nonnull
    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float pScaleFactor)
    {
        return this.quadSize * Mth.clamp(((float) this.age + pScaleFactor) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(@Nonnull SimpleParticleType particleType, @Nonnull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            return new CharmedFeedbackParticle(level, x, y, z, this.spriteSet);
        }
    }
}
