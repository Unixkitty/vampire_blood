package com.unixkitty.vampire_blood.util;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.blood.IBloodVessel;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampireAttributeModifier;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.PlayerAvoidHurtAnimS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

public class VampireUtil
{
    public static final double FEEDING_DISTANCE = 2.0D;

    public static <E extends Enum<E>> String getEnumName(@Nonnull E e)
    {
        return e.name().toLowerCase();
    }

    @SuppressWarnings("MalformedFormatString")
    public static <T extends Number> String formatDecimal(@Nonnull T number)
    {
        String result = String.format("%.2f", number);

        return result.endsWith(".00") ? result.split("\\.")[0] : result;
    }

    public static String formatPercent100(double number)
    {
        return formatDecimal(number > 1 ? number : number * 100) + "%";
    }

    public static boolean isUndead(@Nonnull ServerPlayer player)
    {
        return player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel() != VampirismLevel.NOT_VAMPIRE && vampirePlayerData.getVampireLevel() != VampirismLevel.ORIGINAL).orElse(false);
    }

    public static boolean isVampire(@Nonnull ServerPlayer player)
    {
        return player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel().getId() > VampirismLevel.IN_TRANSITION.getId()).orElse(false);
    }

    public static float getHealthRegenRate(@Nonnull Player player)
    {
        return (float) ((player.getMaxHealth() / player.getAttributeBaseValue(Attributes.MAX_HEALTH) / (20.0F / Config.naturalHealingRate.get())) * Config.naturalHealingMultiplier.get());
    }

    public static int healthToBlood(float health, @Nonnull BloodType bloodType)
    {
        return (int) Math.ceil(health * bloodType.getBloodSaturationModifier());
    }

    public static int clampInt(int value, int max)
    {
        return clampInt(0, value, max);
    }

    public static int clampInt(int min, int value, int max)
    {
        return Math.max(min, Math.min(value, max));
    }

    public static float clampFloat(float value, float max)
    {
        return clampFloat(0F, value, max);
    }

    public static float clampFloat(float min, float value, float max)
    {
        return Math.max(min, Math.min(value, max));
    }

    public static void preventMovement(@Nonnull LivingEntity entity)
    {
        VampireUtil.applyEffect(entity, MobEffects.MOVEMENT_SLOWDOWN, 20, 9);
        VampireUtil.applyEffect(entity, MobEffects.DIG_SLOWDOWN, 400, 2);
    }

    public static boolean canReachEntity(@Nonnull Player player, @Nonnull LivingEntity target)
    {
        Vec3 eyePos2 = player.getEyePosition();

        return target.getBoundingBox().clip(eyePos2, eyePos2.add(player.getLookAngle().scale(FEEDING_DISTANCE + 0.1D))).isPresent();
    }

    public static void updateAttributes(ServerPlayer player, VampirismLevel vampirismLevel, BloodType bloodType, float bloodPurity, final Set<VampireActiveAbility> activeAbilities)
    {
        for (VampireAttributeModifier modifier : VampireAttributeModifier.values())
        {
            float lastHealth = modifier == VampireAttributeModifier.HEALTH ? player.getHealth() : -1;

            //1. Remove existing modifier
            AttributeInstance attribute = player.getAttribute(modifier.getBaseAttribute());

            if (attribute != null)
            {
                AttributeModifier existingModifier = attribute.getModifier(modifier.getUUID());

                if (existingModifier != null)
                {
                    attribute.removeModifier(existingModifier);

                    if (modifier == VampireAttributeModifier.BASE_SPEED)
                    {
                        player.removeEffect(MobEffects.JUMP);
                    }
                }

                //2. Calculate actual value to use
                double modifierValue = modifier.getValue(attribute.getBaseValue(), vampirismLevel, bloodType, bloodPurity, activeAbilities);

                //3. Add modifier to player
                if (modifierValue != -1)
                {
                    //Clamp health at 0 to prevent going negative
                    if (modifier == VampireAttributeModifier.HEALTH)
                    {
                        modifierValue = Math.max(0, modifierValue);
                    }

                    attribute.addPermanentModifier(new AttributeModifier(modifier.getUUID(), modifier.getName(), modifierValue, modifier.getModifierOperation()));

                    if (modifier == VampireAttributeModifier.BASE_SPEED)
                    {
                        VampireUtil.applyEffect(player, MobEffects.JUMP, -1, Mth.floor(modifierValue));
                    }
                }

                if (lastHealth != -1)
                {
                    float health = Math.min(lastHealth, player.getMaxHealth());

                    if (health < player.getHealth())
                    {
                        ModNetworkDispatcher.sendToClient(new PlayerAvoidHurtAnimS2CPacket(health), player);
                    }

                    player.setHealth(health);
                }
            }
        }
    }

    public static Optional<Vec3> getFeedingBloodParticlePosition(Player vampirePlayer, LivingEntity feedingTarget)
    {
        return feedingTarget.getBoundingBox().clip(vampirePlayer.getEyePosition(), feedingTarget.getPosition(1.0F).add(0, feedingTarget.getBbHeight() / 2, 0));
    }

    public static boolean isEntityCharmedBy(LivingEntity entity, ServerPlayer player)
    {
        return entity.getCapability(BloodProvider.BLOOD_STORAGE).map(bloodEntityStorage -> bloodEntityStorage.isCharmedBy(player)).orElse(false);
    }

    public static boolean isArmour(final ItemStack itemStack)
    {
        return itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getDefense() > 0;
    }

    public static void applyEffect(LivingEntity livingEntity, MobEffect effect, int duration, int effectPower)
    {
        livingEntity.addEffect(new MobEffectInstance(effect, duration, effectPower, false, false, true));
    }

    public static void chanceEffect(LivingEntity livingEntity, MobEffect effect, int duration, int effectPower, int chance)
    {
        runWithChance(chance, livingEntity.getRandom(), () -> applyEffect(livingEntity, effect, duration, effectPower));
    }

    public static void chanceSound(ServerPlayer player, float volume, float pitch, int chance)
    {
        runWithChance(chance, player.getRandom(), () -> player.playNotifySound(SoundEvents.AMBIENT_CAVE.get(), player.getSoundSource(), volume, pitch));
    }

    public static void runWithChance(int chance, RandomSource randomSource, Runnable runnable)
    {
        if (!(chance < 100 && randomSource.nextInt(101) > chance))
        {
            runnable.run();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static @Nonnull IBloodVessel getEntityBloodVessel(@Nonnull LivingEntity targetEntity)
    {
        IBloodVessel bloodVessel = targetEntity instanceof Player targetPlayer ? targetPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).orElse(null) : targetEntity.getCapability(BloodProvider.BLOOD_STORAGE).orElse(null);

        //noinspection ConstantValue
        if (bloodVessel == null)
        {
            throw new IllegalStateException(targetEntity.getDisplayName().getString() + " (" + targetEntity.getStringUUID() + ") had no attached capability!");
        }

        return bloodVessel;
    }
}
