package com.unixkitty.vampire_blood.init;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class ModDamageSources
{
    public static final DamageSource SUN_DAMAGE = new ModDamageSource("sunlight").bypassArmor().bypassMagic();
    public static final DamageSource BLOOD_LOSS = new ModDamageSource("bloodloss").bypassArmor().bypassMagic();

    private static class ModDamageSource extends DamageSource
    {
        public ModDamageSource(String name)
        {
            super(name);
        }

        @Nonnull
        @Override
        public Component getLocalizedDeathMessage(@Nonnull LivingEntity victim)
        {
            if (this == SUN_DAMAGE)
            {
                return Component.translatable("vampire_blood.death.attack.sunlight_" + victim.getRandom().nextIntBetweenInclusive(1, 9), victim.getDisplayName());
            }
            else if (this == BLOOD_LOSS)
            {
                LivingEntity attacker = victim.getKillCredit();
                final boolean suicide = attacker == victim || attacker == null;
                String message = "vampire_blood.death." + (suicide ? "self" : "attack") + ".bloodloss_";

                if (suicide)
                {
                    return Component.translatable(message + victim.getRandom().nextIntBetweenInclusive(1, 4), victim.getDisplayName());
                }
                else
                {
                    return Component.translatable(message + victim.getRandom().nextIntBetweenInclusive(1, 6), victim.getDisplayName(), attacker.getDisplayName());
                }
            }

            return super.getLocalizedDeathMessage(victim);
        }
    }
}
