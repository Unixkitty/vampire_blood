package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.init.ModDamageTypes;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public interface IBloodVessel
{
    String CHARMED_BY_NBT_NAME = "charmedBy";
    String KNOWN_VAMPIRE_PLAYERS_NBT_NAME = "knownVampirePlayers";
    String FOOD_ITEM_COOLDOWN_NBT_NAME = "foodItemCooldown";

    boolean isEdible();

    int getBloodPoints();

    int getMaxBloodPoints();

    BloodType getBloodType();

    void dieFromBloodLoss(@Nonnull LivingEntity victim, @Nonnull LivingEntity attacker);

    void drinkFromHealth(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim, @Nonnull BloodType bloodType);

    boolean decreaseBlood(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim);

    boolean hasNoFoodItemCooldown();

    void addFoodItemCooldown(LivingEntity entity, ItemStack stack);

    int getFoodItemCooldown();

    default void handleBloodlossEffects(@Nonnull LivingEntity victim, @Nonnull LivingEntity attacker)
    {
        if (victim.isAlive())
        {
            if (victim.getMobType() == MobType.UNDEAD && !(victim instanceof Player)) return;

            MobEffectInstance weaknessEffectInstance = stackEffect(victim, MobEffects.WEAKNESS);

            stackEffect(victim, MobEffects.DIG_SLOWDOWN);

            if (weaknessEffectInstance.duration >= 1920)
            {
                int chance = Math.min(weaknessEffectInstance.duration / 160, 100);

                victim.hurt(ModDamageTypes.source(ModDamageTypes.BLOOD_LOSS, victim.level(), attacker), 1F);

                if (victim.hasEffect(MobEffects.DAMAGE_BOOST))
                {
                    chance /= 2;
                }

                if (victim.hasEffect(MobEffects.DAMAGE_RESISTANCE))
                {
                    chance /= 2;
                }

                VampireUtil.chanceEffect(victim, MobEffects.CONFUSION, 120, 1, chance * 2);
                VampireUtil.runWithChance(chance, victim.getRandom(), () -> this.dieFromBloodLoss(victim, attacker));
            }
        }
    }

    private MobEffectInstance stackEffect(@Nonnull LivingEntity victim, @Nonnull MobEffect effect)
    {
        MobEffectInstance effectInstance = new MobEffectInstance(effect, 120, 1, false, false, true);
        MobEffectInstance existingEffectInstance = victim.getEffect(effect);

        if (existingEffectInstance == null)
        {
            victim.addEffect(effectInstance);
            existingEffectInstance = effectInstance;
        }
        else
        {
            effectInstance.duration += existingEffectInstance.duration;

            existingEffectInstance.update(effectInstance);
        }

        return existingEffectInstance;
    }

    boolean tryGetCharmed(@Nonnull ServerPlayer player, VampirismLevel attackerLevel, @Nonnull LivingEntity target);

    boolean isCharmedBy(@Nonnull ServerPlayer player);

    int getCharmedByTicks(@Nonnull ServerPlayer player);

    boolean setCharmedBy(@Nonnull ServerPlayer player, @Nonnull LivingEntity target);

    void handleBeingCharmedTicks(@Nonnull LivingEntity entity);
}
