package com.unixkitty.vampire_blood.client.feeding;

import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestEntityBloodC2SPacket;
import com.unixkitty.vampire_blood.network.packet.RequestFeedingC2SPacket;
import com.unixkitty.vampire_blood.network.packet.RequestStopFeedingC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;

@OnlyIn(Dist.CLIENT)
public class FeedingHandler
{
    private static int lastSentStartRequest = 0;
    private static int lastSentStopRequest = 0;

    public static void handleMouseOver(final RenderHighlightEvent.Entity event)
    {
        FeedingMouseOverHandler.handle(event);
    }

    public static void handleKeys(final InputEvent.Key event)
    {
        FeedingKeyHandler.handle(event);
    }

    static void requestUpdateOn(int id)
    {
        ModNetworkDispatcher.sendToServer(new RequestEntityBloodC2SPacket(id));
    }

    static void startFeeding()
    {
        int delta = Minecraft.getInstance().player.tickCount - lastSentStartRequest;

        if (delta >= 10 || delta < 0)
        {
            ModNetworkDispatcher.sendToServer(new RequestFeedingC2SPacket(FeedingMouseOverHandler.getLastHighlightedEntity()));

            lastSentStartRequest = Minecraft.getInstance().player.tickCount;
        }
    }

    static void stopFeeding()
    {
        int delta = Minecraft.getInstance().player.tickCount - lastSentStopRequest;

        if (delta >= 10 || delta < 0)
        {
            ModNetworkDispatcher.sendToServer(new RequestStopFeedingC2SPacket());

            lastSentStopRequest = Minecraft.getInstance().player.tickCount;
        }
    }
}
