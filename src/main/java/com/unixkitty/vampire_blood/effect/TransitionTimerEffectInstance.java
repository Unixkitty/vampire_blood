package com.unixkitty.vampire_blood.effect;

import com.unixkitty.vampire_blood.init.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class TransitionTimerEffectInstance extends MobEffectInstance
{
    public TransitionTimerEffectInstance(int timer)
    {
        super(ModEffects.VAMPIRE_IN_TRANSITION.get(), timer, 0, false, false, true);

        setCurativeItems(new ArrayList<>());
    }

    @Override
    public boolean update(@Nonnull MobEffectInstance other)
    {
        return false;
    }

    @Override
    public void applyEffect(@Nonnull LivingEntity entity)
    {
    }

    @Override
    public boolean equals(Object other)
    {
        return other == this;
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag)
    {
        return tag;
    }
}
