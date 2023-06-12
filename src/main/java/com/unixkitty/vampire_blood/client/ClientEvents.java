package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingHandler;
import com.unixkitty.vampire_blood.client.gui.BloodBarOverlay;
import com.unixkitty.vampire_blood.client.gui.ModDebugOverlay;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.init.ModParticles;
import com.unixkitty.vampire_blood.particle.CharmedParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
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
            if (event.getOverlay().id() == VanillaGuiOverlay.FOOD_LEVEL.id() && Minecraft.getInstance().player != null && ClientCache.canFeed() && Minecraft.getInstance().player.isAlive() && Minecraft.getInstance().gameMode != null && Minecraft.getInstance().gameMode.hasExperience())
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
            if (event.side == LogicalSide.CLIENT && event.phase == TickEvent.Phase.END && ClientCache.isVampire())
            {
                if (Config.debug.get() && Config.renderDebugOverlay.get() && ModDebugOverlay.mainEnabled)
                {
                    ClientCache.getDebugVars().updateThirstExhaustionIncrementRate(event.player.tickCount);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingTick(final LivingEvent.LivingTickEvent event)
        {
            LivingEntity entity = event.getEntity();
            LocalPlayer player = Minecraft.getInstance().player;

            if (entity.level.isClientSide && player != null && player.isAlive() && entity.tickCount % 5 == 0 && ClientCache.isVampire() && ClientCache.getVampireVars().isEntityCharmed(entity.getId()) && player.isCloseEnough(event.getEntity(), ModEffects.SENSES_DISTANCE_LIMIT))
            {
                double d1 = entity.level.random.nextDouble() * 2.0D;
                double d2 = entity.level.random.nextDouble() * Math.PI;
                double d3 = Math.cos(d2) * d1;
                double d4 = 0.01D + entity.level.random.nextDouble() * 0.5D;
                double d5 = Math.sin(d2) * d1;

                entity.level.addParticle(ModParticles.CHARMED_PARTICLE.get(), entity.getX() + d3 * 0.1D, entity.getY() + (entity.getBbHeight() / 2), entity.getZ() + d5 * 0.1D, d3, d4, d5);
            }
        }

        @SubscribeEvent
        public static void onPostRenderLiving(final RenderLivingEvent.Post<?, ?> event)
        {
            BloodVisionUtil.render(event);
        }

        @SubscribeEvent
        public static void onClientLoggedOut(final ClientPlayerNetworkEvent.LoggingOut event)
        {
            ClientCache.reset();
        }
    }

    @Mod.EventBusSubscriber(modid = VampireBlood.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents
    {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event)
        {
            if (Config.debug.get() && Config.renderDebugOverlay.get())
            {
                MinecraftForge.EVENT_BUS.addListener(ModDebugOverlay::render);
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
            event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "blood", BloodBarOverlay.INSTANCE);
        }

        @SubscribeEvent
        public static void registerParticleFactories(final RegisterParticleProvidersEvent event)
        {
            event.register(ModParticles.CHARMED_PARTICLE.get(), CharmedParticle.Provider::new);
        }
    }
}
