package com.unixkitty.vampire_blood.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.DrinkBloodC2SPacket;
import com.unixkitty.vampire_blood.network.packet.StopDrinkBloodC2SPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings
{
    public static final String KEY_CATEGORY_MAIN = keyCategory("main");
    public static final String KEY_FEED = keyName("feed");

    private static final KeyMapping FEED_KEY = new KeyMapping(KEY_FEED, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY_MAIN);

    private static boolean succ = false;

    public static void init(final RegisterKeyMappingsEvent event)
    {
        event.register(FEED_KEY);
    }

    public static void handleKeys(final InputEvent event)
    {
/*        Player player = Minecraft.getInstance().player;

        if (player != null)
        {
            if (FEED_KEY.consumeClick() && player.isCreative() && player.isSpectator())
            {
                player.sendSystemMessage(Component.literal("Pressed key: " + FEED_KEY.getName()));

                ModNetworkMessages.sendToServer(new DrinkBloodC2SPacket());
            }
        }*/

        if (FEED_KEY.isDown())
        {
            if (!succ)
            {
                Player player = Minecraft.getInstance().player;

                if (player == null) return;

                succ = true;

                HitResult mouseOver = Minecraft.getInstance().hitResult;

//                if (mouseOver != null && !player.isSpectator() && !player.isCreative() && player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent() && player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel().getId() > VampirePlayerData.Stage.NOT_VAMPIRE.getId()).orElse(false))
                if (mouseOver != null && !player.isSpectator() && !player.isCreative() && player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
                {
                    //TODO actually use entities
                    if (mouseOver instanceof EntityHitResult)
                    {
                        //TODO LivingEntity
//                        ModNetworkDispatcher.sendToServer(new DrinkBloodC2SPacket(((EntityHitResult) mouseOver).getEntity().getId()));
                        if (Config.isDebug) player.sendSystemMessage(Component.literal("Drink blood key is down"));
                        ModNetworkDispatcher.sendToServer(new DrinkBloodC2SPacket());
                    }
//                    else if (mouseOver instanceof BlockHitResult)
//                    {
//                        BlockPos pos = ((BlockHitResult) mouseOver).getBlockPos();
//                        ModNetworkDispatcher.sendToServer(new DrinkBloodC2SPacket(pos));
//                    }
                }
            }
        }
        else
        {
            if (succ)
            {
                succ = false;
                if (Config.isDebug) Minecraft.getInstance().player.sendSystemMessage(Component.literal("Drink blood key is off"));
                ModNetworkDispatcher.sendToServer(new StopDrinkBloodC2SPacket());
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
