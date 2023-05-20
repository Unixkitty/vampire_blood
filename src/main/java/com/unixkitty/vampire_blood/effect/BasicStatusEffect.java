package com.unixkitty.vampire_blood.effect;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class BasicStatusEffect extends MobEffect
{
    public BasicStatusEffect(int color)
    {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    public BasicStatusEffect()
    {
        super(MobEffectCategory.BENEFICIAL, ChatFormatting.WHITE.getColor());
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
