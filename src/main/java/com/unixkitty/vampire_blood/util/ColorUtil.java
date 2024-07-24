package com.unixkitty.vampire_blood.util;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.EntityOutlineColorS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;

import javax.annotation.Nonnull;

public final class ColorUtil
{
    public static final int RED_WINE = 9437216;
    public static final int HEALTHY_RED = 16716563;
    public static final int DARK_SLATE_BLUE = 4408944;
    public static final int SASSY_VIOLET = 6226059;
    public static final int LETTUCE_GREEN = 6336304;

    public static int getForBloodType(BloodType bloodType)
    {
        return switch (bloodType)
        {
            case NONE -> 0;
            case FRAIL -> ChatFormatting.GRAY.getColor();
            case CREATURE -> ColorUtil.LETTUCE_GREEN;
            case HUMAN -> ColorUtil.RED_WINE;
            case VAMPIRE -> ColorUtil.SASSY_VIOLET;
            case PIGLIN -> ChatFormatting.GOLD.getColor();
        };
    }

    //Must send a default color that is not white to avoid confusion with the vanilla glow effect
    public static void computeEntityOutlineColorFor(@Nonnull ServerPlayer player, @Nonnull LivingEntity entity)
    {
        int color = 0;

        if (entity instanceof ServerPlayer targetPlayer)
        {
            color = targetPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData ->
                    switch (vampirePlayerData.getVampireLevel())
                    {
                        case NOT_VAMPIRE -> RED_WINE;
                        case IN_TRANSITION -> DARK_SLATE_BLUE;
                        case ORIGINAL -> 10551359; //colour: hottest pink
                        default -> SASSY_VIOLET;
                    }).orElse(targetPlayer.isCreative() ? -1 : RED_WINE);
        }
        else if (entity instanceof PathfinderMob || entity instanceof AmbientCreature)
        {
            color = entity.getCapability(BloodProvider.BLOOD_STORAGE).map(bloodEntityStorage ->
            {
                if (bloodEntityStorage.getBloodType() == BloodType.NONE)
                {
                    if (entity.getMobType() == MobType.UNDEAD)
                    {
                        return DARK_SLATE_BLUE;
                    }
                    else if (entity.getMobType() == MobType.WATER)
                    {
                        return 430476; //colour: warm ocean
                    }
                    else if (entity.getMobType() == MobType.ARTHROPOD)
                    {
                        return 4734002; //colour: taupe
                    }
                    else
                    {
                        return entity instanceof Animal ? LETTUCE_GREEN : 0;
                    }
                }
                else
                {
                    return getForBloodType(bloodEntityStorage.getBloodType());
                }
            }).orElse(0);
        }

        if (color != -1)
        {
            ModNetworkDispatcher.sendToClient(new EntityOutlineColorS2CPacket(entity, color), player);
        }
    }
}
