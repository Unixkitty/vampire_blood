package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
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

    default void stackBloodlossWeaknessEffect(@Nonnull LivingEntity victim)
    {
        if (victim.isAlive())
        {
            MobEffectInstance effectInstance = new MobEffectInstance(MobEffects.WEAKNESS, 200, 1, false, false, true);
            MobEffectInstance existingEffectInstance = victim.getEffect(effectInstance.getEffect());

            if (existingEffectInstance == null)
            {
                victim.addEffect(effectInstance);
            }
            else
            {
                effectInstance.duration += existingEffectInstance.duration;

                existingEffectInstance.update(effectInstance);
            }
        }
    }

    boolean tryGetCharmed(@Nonnull ServerPlayer player, VampirismLevel attackerLevel, @Nonnull LivingEntity target);

    boolean isCharmedBy(@Nonnull ServerPlayer player);

    int getCharmedByTicks(@Nonnull ServerPlayer player);

    boolean setCharmedBy(@Nonnull ServerPlayer player, @Nonnull LivingEntity target);

    void handleBeingCharmedTicks(@Nonnull LivingEntity entity);
}
