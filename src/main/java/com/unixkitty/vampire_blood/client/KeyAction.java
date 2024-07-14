package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestFeedingC2SPacket;
import com.unixkitty.vampire_blood.network.packet.RequestStopFeedingC2SPacket;
import com.unixkitty.vampire_blood.network.packet.ToggleActiveAbilityC2SPacket;
import com.unixkitty.vampire_blood.network.packet.UseCharmAbilityC2SPacket;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public enum KeyAction
{
    FEED_START,
    FEED_STOP,
    NIGHT_VISION_TOGGLE("mob_effect/night_vision.png", KeyBindings.NIGHT_VISION_KEY, VampireActiveAbility.NIGHT_VISION),
    BLOOD_VISION_TOGGLE("mob_effect/blood_vision.png", KeyBindings.BLOOD_VISION_KEY, VampireActiveAbility.BLOOD_VISION),
    SPEED_TOGGLE("mob_effect/enhanced_speed.png", KeyBindings.SPEED_KEY, VampireActiveAbility.SPEED),
    SENSES_TOGGLE("mob_effect/enhanced_senses.png", KeyBindings.SENSES_KEY, VampireActiveAbility.SENSES),
    CHARM("gui/charm.png", KeyBindings.CHARM_KEY);

    private static final Int2IntOpenHashMap timeStampMap = new Int2IntOpenHashMap();

    @Nullable
    public final ResourceLocation texture;
    private final KeyMapping key;
    private final VampireActiveAbility ability;

    KeyAction()
    {
        this(null, null);
    }

    KeyAction(String texturePath, KeyMapping key)
    {
        this(texturePath, key, null);
    }

    KeyAction(String texturePath, KeyMapping key, VampireActiveAbility ability)
    {
        if (texturePath == null)
        {
            this.texture = null;
        }
        else
        {
            this.texture = new ResourceLocation(ability == VampireActiveAbility.NIGHT_VISION ? "minecraft" : VampireBlood.MODID, "textures/" + texturePath);
        }

        this.key = key;
        this.ability = ability;
    }

    public KeyMapping getKey()
    {
        return this.key;
    }

    public void handleKey()
    {
        if (this.key != null && !this.key.isUnbound() && this.key.consumeClick())
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
        final LocalPlayer player = Minecraft.getInstance().player;

        if (player != null)
        {
            int delta = player.tickCount - timeStampMap.getOrDefault(action.ordinal(), 0);

            if (delta >= 10 || delta < 0)
            {
                switch (action)
                {
                    case FEED_START ->
                            ModNetworkDispatcher.sendToServer(new RequestFeedingC2SPacket(FeedingMouseOverHandler.getLastEntity().getId()));
                    case FEED_STOP -> ModNetworkDispatcher.sendToServer(new RequestStopFeedingC2SPacket());
                    case CHARM ->
                    {
                        /*if (player.isShiftKeyDown())
                        {

                        }
                        else*/
                        if (FeedingMouseOverHandler.isCloseEnough()) //Default charm/uncharm action
                        {
                            ModNetworkDispatcher.sendToServer(new UseCharmAbilityC2SPacket(FeedingMouseOverHandler.getLastEntity().getId()));
                        }
                    }
                    default -> ModNetworkDispatcher.sendToServer(new ToggleActiveAbilityC2SPacket(action.ability));
                }

                timeStampMap.put(action.ordinal(), player.tickCount);
            }
        }
    }
}
