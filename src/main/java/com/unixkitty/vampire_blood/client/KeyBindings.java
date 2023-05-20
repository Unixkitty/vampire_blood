package com.unixkitty.vampire_blood.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.client.ability.AbilityKeyHandler;
import com.unixkitty.vampire_blood.client.feeding.FeedingHandler;
import com.unixkitty.vampire_blood.client.gui.ModDebugOverlay;
import com.unixkitty.vampire_blood.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeyBindings
{
    private static List<KeyMapping> keyMappings = new ArrayList<>();

    public static final KeyMapping FEED_KEY = key("feed", InputConstants.KEY_V);
    public static final KeyMapping NIGHT_VISION_KEY = key("night_vision", InputConstants.KEY_N);

    public static void init(final RegisterKeyMappingsEvent event)
    {
        for (KeyMapping key : keyMappings)
        {
            event.register(key);
        }

        keyMappings = null;
    }

    public static void handleKeys(final InputEvent.Key event)
    {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().mouseHandler.isMouseGrabbed() && Minecraft.getInstance().isWindowActive())
        {
            FeedingHandler.handleKeys(event);

            AbilityKeyHandler.handleKeys(event);
        }

        //TODO remove debug hud toggle
        //F4 + V toggles debug HUD
        //Sneak + F4 + V toggles diet HUD (only renders if main debug HUD is rendering)
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F4) && FEED_KEY.isDown())
        {
            if (!Config.renderDebugOverlay.get())
            {
                Config.renderDebugOverlay.set(true);
                Config.renderDebugOverlay.save();

                ModDebugOverlay.register();
            }

            if (Minecraft.getInstance().options.keyShift.isDown())
            {
                ModDebugOverlay.nextSecondaryElement();
            }
            else
            {
                ModDebugOverlay.mainEnabled = !ModDebugOverlay.mainEnabled;
            }
        }

        //TODO remove debug
//        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F4) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_T))
//        {
//            if (Minecraft.getInstance().player != null)
//            {
//                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Generating test list..."));
//                TestListGenerator.generate();
//                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Test list generated."));
//            }
//        }
    }

    private static KeyMapping key(String name, int keyCode)
    {
        KeyMapping key = new KeyMapping("key." + VampireBlood.MODID + "." + name, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, keyCode, "key.category." + VampireBlood.MODID + "." + "main");

        keyMappings.add(key);

        return key;
    }
}
