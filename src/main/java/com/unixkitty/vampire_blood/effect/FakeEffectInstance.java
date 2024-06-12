package com.unixkitty.vampire_blood.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class FakeEffectInstance extends MobEffectInstance
{
    public FakeEffectInstance(MobEffect effect)
    {
        super(effect, Short.MAX_VALUE, 0, false, false, true);

        setCurativeItems(new ArrayList<>());
    }

    @Override
    public boolean isInfiniteDuration()
    {
        return true;
    }

    @Override
    public boolean tick(@Nonnull LivingEntity entity, @Nonnull Runnable onExpirationRunnable)
    {
        return true;
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
