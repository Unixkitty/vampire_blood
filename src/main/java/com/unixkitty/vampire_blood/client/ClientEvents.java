package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.VampireUtil;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.gui.BloodBarOverlay;
import com.unixkitty.vampire_blood.client.gui.ModDebugOverlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
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

            if (event.getOverlay().id() == VanillaGuiOverlay.FOOD_LEVEL.id() && mc.player != null && VampireUtil.isUndead(mc.player) && mc.gameMode.hasExperience() && mc.player.isAlive())
            {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onClientPlayerClone(ClientPlayerNetworkEvent.Clone event)
        {
            if (ClientVampirePlayerDataCache.playerJustRespawned)
            {
                event.getNewPlayer().getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                {
                    vampirePlayerData.setVampireLevel(ClientVampirePlayerDataCache.vampireLevel.getId());
                    vampirePlayerData.setBloodType(ClientVampirePlayerDataCache.bloodType.ordinal());
                });

                ClientVampirePlayerDataCache.playerJustRespawned = false;
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
            if (Config.renderDebugOverlay.get())
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
