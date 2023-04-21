package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.client.gui.BloodBarOverlay;
import com.unixkitty.vampire_blood.client.gui.ModDebugOverlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientEvents
{
    @Mod.EventBusSubscriber(modid = VampireBlood.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents
    {
        @SubscribeEvent
        public static void onKeyInput(final InputEvent.Key event)
        {
            KeyBindings.handleKeys(event);
        }

        @SubscribeEvent
        public static void onRenderGuiOverlay(final RenderGuiOverlayEvent.Pre event)
        {
            Minecraft mc = Minecraft.getInstance();

            if (event.getOverlay().id() == VanillaGuiOverlay.FOOD_LEVEL.id() && mc.player != null && VampirePlayerData.isVampire(mc.player) && mc.gameMode.hasExperience() && mc.player.isAlive())
            {
                event.setCanceled(true);
            }
        }

        /*@SubscribeEvent
        public static void onClientTick(final TickEvent.ClientTickEvent event)
        {
            if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null)
            {
                Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData -> vampirePlayerData.tickClient(Minecraft.getInstance().player));
            }
        }*/
    }

    @Mod.EventBusSubscriber(modid = VampireBlood.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents
    {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event)
        {
            if (Config.renderDebugOverlay)
            {
                MinecraftForge.EVENT_BUS.addListener(ModDebugOverlay::onRenderText);
            }
        }

        @SubscribeEvent
        public static void registerKeyMappings(final RegisterKeyMappingsEvent event)
        {
            KeyBindings.init(event);
        }

        @SubscribeEvent
        public static void onRegisterGuiOverlays(final RegisterGuiOverlaysEvent event)
        {
            event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "bloodbar", BloodBarOverlay.INSTANCE);
        }
    }
}
