package com.unixkitty.vampire_blood.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.TestListGenerator;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.client.feeding.FeedingHandler;
import com.unixkitty.vampire_blood.client.gui.ModDebugOverlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings
{
    public static final String KEY_CATEGORY_MAIN = keyCategory("main");
    public static final String KEY_FEED = keyName("feed");

    public static final KeyMapping FEED_KEY = new KeyMapping(KEY_FEED, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY_MAIN);

    public static void init(final RegisterKeyMappingsEvent event)
    {
        event.register(FEED_KEY);
    }

    public static void handleKeys(final InputEvent.Key event)
    {
        if (Minecraft.getInstance().player != null)
        {
            FeedingHandler.handleKeys(event);
        }

        //TODO remove debug hud toggle
        //F4 + V toggles debug HUD
        //Sneak + F4 + V toggles diet HUD (only renders if main debug HUD is rendering)
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F4) && FEED_KEY.isDown())
        {
            if (Minecraft.getInstance().options.keyShift.isDown())
            {
                ModDebugOverlay.keyDietEnabled = !ModDebugOverlay.keyDietEnabled;
            }
            else
            {
                if (Config.renderDebugOverlay.get())
                {
                    ModDebugOverlay.keyEnabled = !ModDebugOverlay.keyEnabled;
                }
                else
                {
                    Config.renderDebugOverlay.set(true);
                    Config.renderDebugOverlay.save();
                    ModDebugOverlay.keyEnabled = true;

                    ModDebugOverlay.register();
                }
            }
        }

        //TODO remove debug
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F4) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_T))
        {
            if (Minecraft.getInstance().player != null)
            {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Generating test list..."));
                TestListGenerator.generate();
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Test list generated."));
            }
        }
    }

    private static String keyCategory(String name)
    {
        return "key.category." + VampireBlood.MODID + "." + name;
    }

    private static String keyName(String name)
    {
        return "key." + VampireBlood.MODID + "." + name;
    }
}
