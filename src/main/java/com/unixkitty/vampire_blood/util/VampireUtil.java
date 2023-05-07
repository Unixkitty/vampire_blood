package com.unixkitty.vampire_blood.util;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class VampireUtil
{
    @SuppressWarnings("MalformedFormatString")
    public static String formatDecimal(Number number, int decimalPoints)
    {
        return String.format("%." + decimalPoints + "f", number);
    }

    public static boolean isUndead(Player player)
    {
        return player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel().getId() > VampirismStage.NOT_VAMPIRE.getId()).orElse(false);
    }

    public static boolean isVampire(Player player)
    {
        return player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId()).orElse(false);
    }

    public static boolean isTransitioning(Player player)
    {
        return player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel() == VampirismStage.IN_TRANSITION).orElse(false);
    }

    public static float getHealthRegenRate(Player player)
    {
        return (float) ((player.getMaxHealth() / player.getAttributeBaseValue(Attributes.MAX_HEALTH) / (20.0F / Config.naturalHealingRate.get())) * Config.naturalHealingMultiplier.get());
    }

    public static int healthToBlood(float health, BloodType bloodType)
    {
        return (int) Math.ceil(health * bloodType.getBloodSaturationModifier());
    }
}
