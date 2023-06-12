package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles
{
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, VampireBlood.MODID);

    public static final RegistryObject<SimpleParticleType> CHARMED_PARTICLE = PARTICLES.register("enthralled", () -> new SimpleParticleType(true));
}
