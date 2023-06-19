package com.unixkitty.vampire_blood.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.client.feeding.FeedingHandler;
import com.unixkitty.vampire_blood.client.gui.ModDebugOverlay;
import com.unixkitty.vampire_blood.client.gui.abilitywheel.AbilityWheelHandler;
import com.unixkitty.vampire_blood.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class KeyBindings
{
    private static List<KeyMapping> keyMappings = new ArrayList<>();

    public static final KeyMapping FEED_KEY = register("feed", InputConstants.KEY_V);
    public static final KeyMapping NIGHT_VISION_KEY = register("night_vision", InputConstants.KEY_N);
    public static final KeyMapping BLOOD_VISION_KEY = register("blood_vision", InputConstants.KEY_B);
    public static final KeyMapping SPEED_KEY = register("enhanced_speed", InputConstants.KEY_X);
    public static final KeyMapping SENSES_KEY = register("enhanced_senses", InputConstants.KEY_H);
    public static final KeyMapping CHARM_KEY = register("charm", InputConstants.KEY_G);
    public static final KeyMapping ABILITY_WHEEL_KEY = register("ability_wheel", InputConstants.KEY_C, KeyConflictContext.UNIVERSAL);

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
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.isWindowActive())
        {
            if (minecraft.player != null && minecraft.player.isAlive() && !minecraft.player.isSpectator())
            {
                if (minecraft.mouseHandler.isMouseGrabbed())
                {
                    FeedingHandler.handleKeys(event, minecraft);

                    KeyAction.handleKeys();
                }

                AbilityWheelHandler.handleKeys(minecraft, event);
            }
        }

        //F4 + V toggles debug HUD
        //Sneak + F4 + V toggles diet HUD (only renders if main debug HUD is rendering)
        if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F4) && FEED_KEY.isDown())
        {
            if (Config.debug.get() && Config.renderDebugOverlay.get())
            {
                if (minecraft.options.keyShift.isDown())
                {
                    ModDebugOverlay.nextSecondaryElement();
                }
                else
                {
                    ModDebugOverlay.mainEnabled = !ModDebugOverlay.mainEnabled;
                }
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

    private static KeyMapping register(String name, int keyCode)
    {
        return register(name, keyCode, KeyConflictContext.IN_GAME);
    }

    private static KeyMapping register(String name, int keyCode, KeyConflictContext context)
    {
        KeyMapping key = new KeyMapping("key." + VampireBlood.MODID + "." + name, context, InputConstants.Type.KEYSYM, keyCode, "key.category." + VampireBlood.MODID + "." + "main");

        keyMappings.add(key);

        return key;
    }
}
