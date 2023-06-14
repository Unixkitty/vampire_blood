package com.unixkitty.vampire_blood.client.feeding;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;

@OnlyIn(Dist.CLIENT)
public class FeedingMouseOverHandler
{
    public static BloodType bloodType = BloodType.NONE;
    public static int maxBloodPoints = 0;
    public static int bloodPoints = 0;
    public static int charmedSeconds = -2;

    private static LivingEntity lastEntity = null;
    private static boolean closeEnough = false;
    private static int lastTick = 0;
    private static boolean hasData = false;

    static void handle(final RenderHighlightEvent.Entity event)
    {
        if (ClientCache.canFeed() && event.getTarget().getEntity() instanceof LivingEntity entity) //LivingEntity because we want info about players as well
        {
            int currentTick = Minecraft.getInstance().gui.getGuiTicks();

            if (entity.isAlive())
            {
                closeEnough = Minecraft.getInstance().player.isCloseEnough(entity, VampireUtil.FEEDING_DISTANCE);

                if (lastEntity != entity)
                {
                    lastEntity = entity;

                    FeedingHandler.requestUpdateOn(entity.getId());
                }
                else if (lastTick != currentTick && currentTick % 20 == 0)
                {
                    FeedingHandler.requestUpdateOn(entity.getId());
                }
            }
            else if (lastEntity == entity)
            {
                reset();
            }

            lastTick = currentTick;
        }
    }

    public static LivingEntity getLastEntity()
    {
        return lastEntity;
    }

    public static void reset()
    {
        lastEntity = null;
        bloodType = BloodType.NONE;
        maxBloodPoints = 0;
        bloodPoints = 0;
        charmedSeconds = -2;

        hasData = false;
        closeEnough = false;
    }

    public static void setData(BloodType pBloodType, int pBloodPoints, int pMaxBloodPoints, int pCharmedTicks)
    {
        bloodType = pBloodType;
        bloodPoints = pBloodPoints;
        maxBloodPoints = pMaxBloodPoints;
        charmedSeconds = pCharmedTicks > 0 ? pCharmedTicks / 20 : pCharmedTicks;

        hasData = true;
    }

    public static boolean hasData()
    {
        return hasData;
    }

    public static boolean isLookingAtEdible()
    {
        return hasData && maxBloodPoints > 0 && bloodType != BloodType.NONE;
    }

    public static boolean isCloseEnough()
    {
        return isLookingAtEdible() && closeEnough;
    }
}
