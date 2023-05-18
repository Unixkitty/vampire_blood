package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.init.ModRegistry;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public interface IBloodVessel
{
    boolean isEdible();

    int getBloodPoints();

    int getMaxBloodPoints();

    BloodType getBloodType();

    default void drinkFromHealth(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim, @Nonnull BloodType bloodType)
    {
        float resultingHealth = victim.getHealth() - (1F / bloodType.getBloodSaturationModifier());

        if (resultingHealth > 0)
        {
            victim.setHealth(resultingHealth);
        }
        else
        {
            victim.setLastHurtByMob(attacker);
            victim.hurt(ModRegistry.BLOOD_LOSS, Float.MAX_VALUE);
        }
    }

    boolean decreaseBlood(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim);
}
