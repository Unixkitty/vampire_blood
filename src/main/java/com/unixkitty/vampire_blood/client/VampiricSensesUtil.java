package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.init.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class VampiricSensesUtil
{
    public static boolean shouldEntityGlow(@Nonnull Entity entity, @Nullable LocalPlayer player)
    {
        return (entity instanceof Player || entity instanceof PathfinderMob) && player != null && player.hasEffect(ModEffects.ENHANCED_SENSES.get()) && !(entity.isInFluidType() && !entity.getEyeInFluidType().isAir()) && entity.distanceTo(player) < 30F;
    }

    //TODO we don't want much logic here because this is called every render frame, need caching
    public static int getEntityGlowColor(@Nonnull Entity entity)
    {
        if (entity instanceof LivingEntity livingEntity && livingEntity.level.isClientSide() && Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(ModEffects.ENHANCED_SENSES.get()))
        {
            if (livingEntity instanceof Player)
            {
//                if (livingEntity.getMobType() == MobType.UNDEAD)
//                {
                    return ChatFormatting.DARK_PURPLE.getColor();
//                }
//                else
//                {
//                    return BloodType.HUMAN.getColor();
//                }
            }
            else if (livingEntity instanceof PathfinderMob)
            {
                if ((livingEntity.getMobType() == MobType.UNDEFINED && (livingEntity instanceof Villager || livingEntity instanceof Witch)) || (livingEntity.getMobType() == MobType.ILLAGER && livingEntity instanceof AbstractIllager))
                {
                    return BloodType.HUMAN.getColor();
                }
                else if (livingEntity.getMobType() == MobType.ARTHROPOD)
                {
                    return ChatFormatting.BLACK.getColor();
                }
                else if (livingEntity.getMobType() == MobType.WATER)
                {
                    return ChatFormatting.BLUE.getColor();
                }
                else if (livingEntity.getMobType() == MobType.UNDEAD)
                {
                    return BloodType.FRAIL.getColor();
                }
            }
        }

        return -1;
    }
}
