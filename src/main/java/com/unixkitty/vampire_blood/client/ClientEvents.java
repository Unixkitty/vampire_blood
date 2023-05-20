package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.client.cache.ClientVampirePlayerDataCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingHandler;
import com.unixkitty.vampire_blood.client.gui.BloodBarOverlay;
import com.unixkitty.vampire_blood.client.gui.ModDebugOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientEvents
{
    public static final int MARGIN_PX = 5;
    public static final ResourceLocation ICONS_PNG = new ResourceLocation(VampireBlood.MODID, "textures/gui/icons.png");

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
            if (event.getOverlay().id() == VanillaGuiOverlay.FOOD_LEVEL.id() && Minecraft.getInstance().player != null && ClientVampirePlayerDataCache.canFeed() && Minecraft.getInstance().player.isAlive() && Minecraft.getInstance().gameMode != null && Minecraft.getInstance().gameMode.hasExperience())
            {
                event.setCanceled(true);
            }
        }

        //TODO test respawn/player cloning
//        @SubscribeEvent
//        public static void onClientPlayerClone(final ClientPlayerNetworkEvent.Clone event)
//        {
//            if (ClientVampirePlayerDataCache.playerJustRespawned)
//            {
//                event.getNewPlayer().getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
//                {
//                    vampirePlayerData.setVampireLevel(ClientVampirePlayerDataCache.vampireLevel.getId());
//                    vampirePlayerData.setBloodType(ClientVampirePlayerDataCache.bloodType.getId());
//                });
//
//                ClientVampirePlayerDataCache.playerJustRespawned = false;
//            }
//        }

        @SubscribeEvent
        public static void onEntityMouseOver(final RenderHighlightEvent.Entity event)
        {
            FeedingHandler.handleMouseOver(event);
        }

        @SubscribeEvent
        public static void onClientTick(final TickEvent.PlayerTickEvent event)
        {
            if (event.side == LogicalSide.CLIENT && event.player instanceof LocalPlayer player)
            {
                switch (event.phase)
                {
                    case START ->
                    {
                        /*if (player.tickCount % 10 == 0)
                        {
                            for (VampireActiveAbilities ability : VampireActiveAbilities.values())
                            {
                                if (ClientVampirePlayerDataCache.activeAbilities.contains(ability) && ClientVampirePlayerDataCache.isVampire())
                                {
                                    ability.refresh(player);
                                }
                                else if (!ClientVampirePlayerDataCache.isVampire() || !ClientVampirePlayerDataCache.activeAbilities.contains(ability))
                                {
                                    ability.stop(player);
                                }
                            }
                        }*/
                    }
                    case END ->
                    {
                        if (ModDebugOverlay.isMainOverlayEnabled() && ClientVampirePlayerDataCache.isVampire())
                        {
                            ClientVampirePlayerDataCache.Debug.updateThirstExhaustionIncrementRate(player.tickCount);
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onClientLoggedIn(final ClientPlayerNetworkEvent.LoggingIn event)
        {
            //TODO cache reinit
        }

        @SubscribeEvent
        public static void onClientLoggedOut(final ClientPlayerNetworkEvent.LoggingOut event)
        {
            //TODO cache reset
        }
    }

    @Mod.EventBusSubscriber(modid = VampireBlood.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents
    {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event)
        {
            ModDebugOverlay.register();
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
