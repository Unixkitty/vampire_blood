package com.unixkitty.vampire_blood.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class CharmedParticle extends TextureSheetParticle
{
    protected CharmedParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet)
    {
        super(level, x, y, z, 0.5D - level.random.nextDouble(), ySpeed, 0.5D - level.random.nextDouble());

        this.friction = 0.96F;
        this.gravity = -0.1F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.yd *= 0.2F;

        if (xSpeed == 0.0D && zSpeed == 0.0D)
        {
            this.xd *= 0.1F;
            this.zd *= 0.1F;
        }

        this.quadSize *= 0.75F;
        this.lifetime = (int) (8.0D / (level.random.nextDouble() * 0.8D + 0.2D));
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);

        if (this.isCloseToScopingPlayer())
        {
            this.setAlpha(0.0F);
        }
    }

    private boolean isCloseToScopingPlayer()
    {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localplayer = minecraft.player;

        return localplayer != null && localplayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0D && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping();
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.isCloseToScopingPlayer())
        {
            this.setAlpha(0.0F);
        }
        else
        {
            this.setAlpha(Mth.lerp(0.05F, this.alpha, 1.0F));
        }

    }

    @Nonnull
    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
            return new CharmedParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }
}
