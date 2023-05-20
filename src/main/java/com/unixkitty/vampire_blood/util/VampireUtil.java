package com.unixkitty.vampire_blood.util;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class VampireUtil
{
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
        return player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel().getId() > VampirismStage.NOT_VAMPIRE.getId()).orElse(false);
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
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 9, false, false, true));
    }

    public static boolean isLookingAtEntity(@Nonnull Player player, @Nonnull LivingEntity target)
    {
        Vec3 eyePos2 = player.getEyePosition();

        return target.getBoundingBox().clip(eyePos2, eyePos2.add(player.getLookAngle().scale(1.1D))).isPresent();
    }
}
