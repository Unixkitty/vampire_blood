package com.unixkitty.vampire_blood.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingHandler;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import com.unixkitty.vampire_blood.client.gui.BloodBarOverlay;
import com.unixkitty.vampire_blood.client.gui.ModDebugOverlay;
import com.unixkitty.vampire_blood.client.gui.abilitywheel.AbilityWheelHandler;
import com.unixkitty.vampire_blood.config.ArmourUVCoverageManager;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.fluid.BloodFluidType;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.init.ModParticles;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.BiteAttackC2SPacket;
import com.unixkitty.vampire_blood.network.packet.RequestOtherPlayerVampireVarsC2SPacket;
import com.unixkitty.vampire_blood.particle.BloodDripParticle;
import com.unixkitty.vampire_blood.particle.CharmedFeedbackParticle;
import com.unixkitty.vampire_blood.particle.CharmedParticle;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Vector3f;

@SuppressWarnings("unused")
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
        public static void onInteraction(final InputEvent.InteractionKeyMappingTriggered event)
        {
            if (FeedingHandler.isFeeding() && (event.isAttack() || event.isUseItem()))
            {
                VampireBlood.LOG.debug("Clicked attack or use when feeding");

                event.setSwingHand(false);
                event.setCanceled(true);

                if (event.isAttack())
                {
                    ModNetworkDispatcher.sendToServer(new BiteAttackC2SPacket(FeedingMouseOverHandler.getLastEntity().getId()));
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onItemTooltip(final ItemTooltipEvent event)
        {
            Player player = event.getEntity();

            if (event.getEntity() != null
                    && ArmourUVCoverageManager.hasEntries()
                    && event.getItemStack().getItem() instanceof ArmorItem item
                    && ClientCache.isVampire())
            {
                float coverage = ArmourUVCoverageManager.getCoverage(item);

                if (coverage > ArmourUVCoverageManager.ZERO_COVERAGE)
                {
                    event.getToolTip().add(2, Component.translatable("text.vampire_blood.sun_coverage_tooltip", String.format("%.0f%%", coverage * 100)).withStyle(ChatFormatting.YELLOW));
                }
            }
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

            if (entity.level().isClientSide && player != null && player.isAlive() && entity.tickCount % 5 == 0 && ClientCache.isVampire() && ClientCache.getVampireVars().isEntityCharmed(entity.getId()) && player.isCloseEnough(event.getEntity(), ModEffects.SENSES_DEFAULT_DISTANCE))
            {
                entity.level().addParticle(ModParticles.CHARMED.get(), entity.getRandomX(entity.getBbWidth()), entity.getRandomY() + 0.5D, entity.getRandomZ(entity.getBbWidth()), 0, 0, 0);
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
                    ModNetworkDispatcher.sendToServer(new RequestOtherPlayerVampireVarsC2SPacket(event.player.level().players().stream().filter(player -> !player.isSpectator()).mapToInt(Player::getId).toArray()));
                }
            }
        }

        @SubscribeEvent
        public static void onPostRenderLiving(final RenderLivingEvent.Post<?, ?> event)
        {
            BloodVisionUtil.render(event);
        }

        @SubscribeEvent
        public static void onRenderFog(final ViewportEvent.RenderFog event)
        {
            if (event.isCanceled()) return;

            Entity cameraEntity = Minecraft.getInstance().getCameraEntity();

            if (cameraEntity != null)
            {
                FluidState fluidstate = cameraEntity.level().getFluidState(event.getCamera().getBlockPosition());

                if (!fluidstate.isEmpty() && fluidstate.getType().getFluidType() instanceof BloodFluidType)
                {
                    event.setCanceled(true);

                    event.setFogShape(FogShape.SPHERE);
                    event.setNearPlaneDistance(0.25F);
                    event.setFarPlaneDistance(1.0F);
                }
            }
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
            event.registerSpriteSet(ModParticles.CHARMED.get(), CharmedParticle.Provider::new);
            event.registerSpriteSet(ModParticles.CHARMED_FEEDBACK.get(), CharmedFeedbackParticle.Provider::new);
            event.registerSpriteSet(ModParticles.DRIPPING_BLOOD.get(), BloodDripParticle.Provider::new);
        }

        @SubscribeEvent
        public static void onModelRegister(ModelEvent.RegisterAdditional event)
        {
            event.register(CustomRenderer.HORNS);
            event.register(CustomRenderer.TAIL_MAIN);
            event.register(CustomRenderer.TAIL_SITTING);
            event.register(CustomRenderer.TAIL_SPEED);
            event.register(CustomRenderer.WINGS);
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
