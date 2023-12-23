package com.unixkitty.vampire_blood.client;

import com.mojang.math.Vector3f;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingHandler;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import com.unixkitty.vampire_blood.client.gui.BloodBarOverlay;
import com.unixkitty.vampire_blood.client.gui.ModDebugOverlay;
import com.unixkitty.vampire_blood.client.gui.abilitywheel.AbilityWheelHandler;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.init.ModParticles;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestOtherPlayerVampireVarsC2SPacket;
import com.unixkitty.vampire_blood.particle.CharmedFeedbackParticle;
import com.unixkitty.vampire_blood.particle.CharmedParticle;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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

    //forSelf we only spawn one particle, otherwise multiple
    public static void spawnBloodParticles(Vec3 position, boolean forSelf)
    {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null)
        {
            double radius = forSelf ? 0.1D : 0.2D;
            double offsetX;
            double offsetY;
            double offsetZ;

            for (int i = 0; i < (forSelf ? 1 : 4); i++)
            {
                offsetX = level.random.nextDouble() * radius - radius / 2;
                offsetY = level.random.nextDouble() * radius - radius / 2;
                offsetZ = level.random.nextDouble() * radius - radius / 2;

                level.addParticle(new DustParticleOptions(new Vector3f(0.7333F, 0.0392F, 0.1176F), 0.75F), position.x + offsetX, position.y + offsetY, position.z + offsetZ, 0, 0, 0);
            }
        }
    }

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
            Minecraft minecraft = Minecraft.getInstance();

            if (event.getOverlay().id() == VanillaGuiOverlay.FOOD_LEVEL.id() && minecraft.player != null && ClientCache.canFeed() && minecraft.player.isAlive() && minecraft.gameMode != null && minecraft.gameMode.hasExperience())
            {
                event.setCanceled(true);
            }
            else if (event.getOverlay().id() == VanillaGuiOverlay.CROSSHAIR.id() && minecraft.screen instanceof AbilityWheelHandler.AbilityWheelScreen)
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
        public static void onClientPlayerTick(final TickEvent.PlayerTickEvent event)
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
                entity.level.addParticle(ModParticles.CHARMED_PARTICLE.get(), entity.getRandomX(entity.getBbWidth()), entity.getRandomY() + 0.5D, entity.getRandomZ(entity.getBbWidth()), 0, 0, 0);
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(final TickEvent.PlayerTickEvent event)
        {
            if (event.side == LogicalSide.CLIENT && event.phase == TickEvent.Phase.END)
            {
                //Render blood particles when feeding
                if (event.player.isAlive() && ClientCache.canFeed() && ClientCache.getVampireVars().feeding && event.player.tickCount % 5 == 0)
                {
                    LivingEntity entity = FeedingMouseOverHandler.getLastEntity();

                    if (entity != null)
                    {
                        VampireUtil.getFeedingBloodParticlePosition(event.player, entity).ifPresent(vec3 -> spawnBloodParticles(vec3, true));
                    }
                }

                //Request info about nearby players
                if (event.player.tickCount % 20 == 0)
                {
                    ModNetworkDispatcher.sendToServer(new RequestOtherPlayerVampireVarsC2SPacket(event.player.level.players().stream().filter(player -> !player.isSpectator()).mapToInt(Player::getId).toArray()));

//                    VampireBlood.LOG.warn("Unixkitty vampire level is {}", event.player.level.getPla);
                }
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
            event.register(ModParticles.CHARMED_FEEDBACK_PARTICLE.get(), CharmedFeedbackParticle.Provider::new);
        }

        @SubscribeEvent
        public static void onModelRegister(ModelEvent.RegisterAdditional event)
        {
            event.register(CustomRenderer.HORNS);
            event.register(CustomRenderer.TAIL_MAIN);
            event.register(CustomRenderer.TAIL_SITTING);
            event.register(CustomRenderer.TAIL_SPEED);
        }

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event)
        {
            addPlayerLayer(event, "default");
            addPlayerLayer(event, "slim");
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static void addPlayerLayer(EntityRenderersEvent.AddLayers event, String skin)
        {
            EntityRenderer<? extends Player> renderer = event.getSkin(skin);

            if (renderer instanceof LivingEntityRenderer livingRenderer)
            {
                livingRenderer.addLayer(new CustomRenderer.CosmeticLayer<>(livingRenderer));
            }
        }
    }
}
