package com.unixkitty.vampire_blood.util;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.EntityBloodInfoS2CPacket;
import com.unixkitty.vampire_blood.network.packet.PlayerFeedingStatusS2CPacket;
import net.minecraft.server.level.ServerPlayer;
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

    public static void sendPlayerEntityBlood(ServerPlayer player, BloodType bloodType, int bloodPoints, int maxBloodPoints)
    {
        ModNetworkDispatcher.sendToClient(new EntityBloodInfoS2CPacket(bloodType.getId(), bloodPoints, maxBloodPoints), player);
    }

    public static void notifyPlayerFeeding(ServerPlayer player, boolean value)
    {
        ModNetworkDispatcher.sendToClient(new PlayerFeedingStatusS2CPacket(value), player);
    }
}
