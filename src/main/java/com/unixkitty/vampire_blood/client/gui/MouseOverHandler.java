package com.unixkitty.vampire_blood.client.gui;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestEntityBloodC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;

@OnlyIn(Dist.CLIENT)
public class MouseOverHandler
{
    public static int lastEntityId = -1;
    public static BloodType bloodType = BloodType.NONE;
    public static int maxBloodPoints = 0;
    public static int bloodPoints = 0;

    private static int lastTick = 0;
    private static boolean hasData = false;

    public static void handle(final RenderHighlightEvent.Entity event)
    {
        if (event.getTarget().getEntity() instanceof PathfinderMob entity && entity.isAlive())
        {
            int currentTick = Minecraft.getInstance().gui.getGuiTicks();

            if (lastEntityId != entity.getId())
            {
                lastEntityId = entity.getId();

                requestUpdateOn(entity.getId());
            }
            else if (lastTick != currentTick && currentTick % 20 == 0)
            {
                requestUpdateOn(entity.getId());
            }

            lastTick = currentTick;
        }
        //TODO players
        /*else if (event.getTarget().getEntity() instanceof Player player)
        {

        }*/
    }

    public static void reset()
    {
        lastEntityId = -1;
        bloodType = BloodType.NONE;
        maxBloodPoints = 0;
        bloodPoints = 0;

        hasData = false;
    }

    public static void setHasData()
    {
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

    private static void requestUpdateOn(int id)
    {
        ModNetworkDispatcher.sendToServer(new RequestEntityBloodC2SPacket(id));
    }
}
