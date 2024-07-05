package com.unixkitty.vampire_blood.particle;

import com.unixkitty.vampire_blood.init.ModFluids;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class BloodDripParticle extends DripParticle
{
    protected BloodDripParticle(ClientLevel pLevel, double pX, double pY, double pZ, Fluid pType, SpriteSet spriteSet)
    {
        super(pLevel, pX, pY, pZ, pType);

        setSpriteFromAge(spriteSet);
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
            return new BloodDripParticle(level, x, y, z, ModFluids.HUMAN_BLOOD.source.get(), this.spriteSet);
        }
    }
}
