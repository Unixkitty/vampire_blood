package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.client.gui.MouseOverHandler;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestFeedingC2SPacket;
import com.unixkitty.vampire_blood.network.packet.RequestStopFeedingC2SPacket;

public class FeedingHandler
{
    private static boolean feedKeyWasDown = false;
    private static boolean wantsToFeed = false;
    private static boolean wantsToStopFeeding = false;
    private static int lastWantsToFeedTimer = 0;
    private static int lastWantsToStopFeedingTimer = 0;

    public static void handle(boolean feedKeyDown, boolean movementKeyPressed)
    {
        if (feedKeyDown)
        {
            if (!wantsToFeed)
            {
                wantsToFeed = true;

                //TODO actually use entities
                if (ClientVampirePlayerDataCache.canFeed() && MouseOverHandler.isLookingAtEdible())
                {
                    ModNetworkDispatcher.sendToServer(new RequestFeedingC2SPacket(MouseOverHandler.lastEntityId));
                }
            }
        }
        else
        {
            if (wantsToFeed)
            {
                wantsToFeed = false;
                ModNetworkDispatcher.sendToServer(new RequestStopFeedingC2SPacket());
            }
        }

        /*if (((feedKeyWasDown && !feedKeyDown) || movementKeyPressed) && ClientVampirePlayerDataCache.isFeeding)
        {
            wantsToStopFeeding = true;
        }
        else if (!feedKeyWasDown && feedKeyDown && ClientVampirePlayerDataCache.canFeed() && MouseOverHandler.isLookingAtEdible())
        {
            wantsToFeed = true;
        }

        feedKeyWasDown = feedKeyDown;

        if (wantsToStopFeeding)
        {
            if (lastWantsToStopFeedingTimer <= 0)
            {
                ModNetworkDispatcher.sendToServer(new RequestStopFeedingC2SPacket());

                wantsToStopFeeding = false;
                lastWantsToStopFeedingTimer = 10;
            }
            else
            {
                --lastWantsToStopFeedingTimer;
            }
        }
        else if (wantsToFeed)
        {
            if (lastWantsToFeedTimer <= 0)
            {
                ModNetworkDispatcher.sendToServer(new RequestFeedingC2SPacket(MouseOverHandler.lastEntityId));

                wantsToFeed = false;
                lastWantsToFeedTimer = 10;
            }
            else
            {
                --lastWantsToFeedTimer;
            }
        }*/
    }
}
