package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.player.VampireActiveAbilities;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestFeedingC2SPacket;
import com.unixkitty.vampire_blood.network.packet.RequestStopFeedingC2SPacket;
import com.unixkitty.vampire_blood.network.packet.UseAbilityC2SPacket;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public enum KeyAction
{
    FEED_START,
    FEED_STOP,
    NIGHT_VISION_TOGGLE(VampireActiveAbilities.NIGHT_VISION);

    private static final Map<KeyAction, Integer> timeStampMap = new HashMap<>();

    private final VampireActiveAbilities ability;

    KeyAction()
    {
        this.ability = null;
    }

    KeyAction(VampireActiveAbilities ability)
    {
        this.ability = ability;
    }

    public void handle()
    {
        handle(this);
    }

    private static void handle(@Nonnull KeyAction action)
    {
        int delta = Minecraft.getInstance().player.tickCount - timeStampMap.getOrDefault(action, 0);

        if (delta >= 10 || delta < 0)
        {
            switch (action)
            {
                case FEED_START ->
                        ModNetworkDispatcher.sendToServer(new RequestFeedingC2SPacket(FeedingMouseOverHandler.getLastEntity().getId()));
                case FEED_STOP -> ModNetworkDispatcher.sendToServer(new RequestStopFeedingC2SPacket());
                default -> ModNetworkDispatcher.sendToServer(new UseAbilityC2SPacket(action.ability));
            }

            timeStampMap.put(action, Minecraft.getInstance().player.tickCount);
        }
    }
}
