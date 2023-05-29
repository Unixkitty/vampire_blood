package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestFeedingC2SPacket;
import com.unixkitty.vampire_blood.network.packet.RequestStopFeedingC2SPacket;
import com.unixkitty.vampire_blood.network.packet.ToggleActiveAbilityC2SPacket;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public enum KeyAction
{
    FEED_START,
    FEED_STOP,
    NIGHT_VISION_TOGGLE(KeyBindings.NIGHT_VISION_KEY, VampireActiveAbility.NIGHT_VISION),
    BLOOD_VISION_TOGGLE(KeyBindings.BLOOD_VISION_KEY, VampireActiveAbility.BLOOD_VISION),
    SPEED_TOGGLE(KeyBindings.SPEED_KEY, VampireActiveAbility.SPEED),
    SENSES_TOGGLE(KeyBindings.SENSES_KEY, VampireActiveAbility.SENSES),
    CHARM(KeyBindings.CHARM_KEY);

    private static final Int2IntOpenHashMap timeStampMap = new Int2IntOpenHashMap();

    private final KeyMapping key;
    private final VampireActiveAbility ability;

    KeyAction()
    {
        this(null, null);
    }

    KeyAction(KeyMapping key)
    {
        this(key, null);
    }

    KeyAction(KeyMapping key, VampireActiveAbility ability)
    {
        this.key = key;
        this.ability = ability;
    }

    public void handleKey()
    {
        if (this.key != null && this.key.consumeClick())
        {
            handle(this);
        }
    }

    public void handle()
    {
        handle(this);
    }

    public static void handleKeys()
    {
        if (ClientCache.isVampire())
        {
            for (KeyAction action : values())
            {
                action.handleKey();
            }
        }
    }

    private static void handle(@Nonnull KeyAction action)
    {
        int delta = Minecraft.getInstance().player.tickCount - timeStampMap.getOrDefault(action.ordinal(), 0);

        if (delta >= 10 || delta < 0)
        {
            switch (action)
            {
                case FEED_START ->
                        ModNetworkDispatcher.sendToServer(new RequestFeedingC2SPacket(FeedingMouseOverHandler.getLastEntity().getId()));
                case FEED_STOP -> ModNetworkDispatcher.sendToServer(new RequestStopFeedingC2SPacket());
//                case CHARM -> ModNetworkDispatcher.sendToServer(new UseCharmAbilityC2SPacket());
                default -> ModNetworkDispatcher.sendToServer(new ToggleActiveAbilityC2SPacket(action.ability));
            }

            timeStampMap.put(action.ordinal(), Minecraft.getInstance().player.tickCount);
        }
    }
}
