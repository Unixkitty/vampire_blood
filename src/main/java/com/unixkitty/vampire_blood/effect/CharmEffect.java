package com.unixkitty.vampire_blood.effect;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class CharmEffect extends MobEffect
{
    public CharmEffect()
    {
        super(MobEffectCategory.HARMFUL, ChatFormatting.LIGHT_PURPLE.getColor());
    }

    @Override
    public void applyEffectTick(@Nonnull LivingEntity livingEntity, int amplifier)
    {

    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier)
    {
        return false;
    }
}
