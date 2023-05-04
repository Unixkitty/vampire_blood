package com.unixkitty.vampire_blood.client.gui;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestEntityBloodC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderHighlightEvent;

public class MouseOverHandler
{
    public static int lastEntityId = -1;
    public static BloodType bloodType = BloodType.NONE;
    public static int maxBloodPoints = 0;
    public static int bloodPoints = 0;

    private static int lastTick = 0;

    public static void handle(final RenderHighlightEvent.Entity event)
    {
        if (event.getTarget().getEntity() instanceof LivingEntity entity)
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
    }

    public static void reset()
    {
        lastEntityId = -1;
        bloodType = BloodType.NONE;
        maxBloodPoints = 0;
        bloodPoints = 0;
    }

    private static void requestUpdateOn(int id)
    {
        ModNetworkDispatcher.sendToServer(new RequestEntityBloodC2SPacket(id));
    }
}
