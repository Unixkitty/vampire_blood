package com.unixkitty.vampire_blood.client.feeding;

import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestEntityBloodC2SPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;

@OnlyIn(Dist.CLIENT)
public class FeedingHandler
{
    public static void handleMouseOver(final RenderHighlightEvent.Entity event)
    {
        FeedingMouseOverHandler.handle(event);
    }

    public static void handleKeys(final InputEvent.Key event)
    {
        FeedingKeyHandler.handle(event);
    }

    public static boolean canFeed()
    {
        return ClientCache.canFeed() && FeedingMouseOverHandler.isCloseEnough();
    }

    public static boolean isFeeding()
    {
        return canFeed() && ClientCache.getVampireVars().feeding;
    }

    static void requestUpdateOn(int id)
    {
        ModNetworkDispatcher.sendToServer(new RequestEntityBloodC2SPacket(id));
    }
}
