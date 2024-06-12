package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModDamageTypes
{
    public static final ResourceKey<DamageType> SUN_DAMAGE = createKey("sunlight");
    public static final ResourceKey<DamageType> BLOOD_LOSS = createKey("bloodloss");

    private static ResourceKey<DamageType> createKey(String name)
    {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(VampireBlood.MODID, name));
    }

    public static DamageSource source(ResourceKey<DamageType> damageType, Level level)
    {
        return source(damageType, level, null);
    }

    public static DamageSource source(ResourceKey<DamageType> damageType, Level level, @Nullable Entity attacker)
    {
        return new DamageSource(level.registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(damageType), attacker)
        {
            @Override
            public boolean scalesWithDifficulty()
            {
                return false;
            }

            @Nonnull
            @Override
            public Component getLocalizedDeathMessage(@Nonnull LivingEntity victim)
            {
                if (this.is(SUN_DAMAGE))
                {
                    return Component.translatable("vampire_blood.death.attack.sunlight_" + victim.getRandom().nextIntBetweenInclusive(1, 9), victim.getDisplayName());
                }
                else if (this.is(BLOOD_LOSS))
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
        };
    }
}
