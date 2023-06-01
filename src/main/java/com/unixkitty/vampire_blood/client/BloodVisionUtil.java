package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestEntityBloodC2SPacket;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;

@OnlyIn(Dist.CLIENT)
public final class BloodVisionUtil
{
    private static final Int2IntOpenHashMap entityBloodRequestTimestampMap = new Int2IntOpenHashMap();
    private static boolean currentlyNotRendering = true;

    public static void render(final RenderLivingEvent.Post<?, ?> event)
    {
        if (currentlyNotRendering)
        {
            final Player player = Minecraft.getInstance().player;
            final LivingEntity entity = event.getEntity();

            if (player != null && player.hasEffect(ModEffects.BLOOD_VISION.get()) && entity != player && entity.isAlive() && !(entity.isInFluidType() && !entity.getEyeInFluidType().isAir()) && entity.distanceTo(player) < ModEffects.SENSES_DISTANCE_LIMIT)
            {
                ClientCache.EntityBlood entityBlood = ClientCache.getVampireVars().getEntityBlood(entity.getId());

                if (entityBlood != null && entityBlood.bloodType() != BloodType.NONE && entityBlood.maxBloodPoints() > 0 && entityBlood.bloodPoints() > 0)
                {
                    int color = entityBlood.bloodType().getColor();
//                    int alpha = (int) (((double) entityBlood.bloodPoints() / entityBlood.maxBloodPoints()) * 255); //This doesn't do anything at the moment

                    currentlyNotRendering = false;

                    OutlineBufferSource buffer = Minecraft.getInstance().renderBuffers().outlineBufferSource();

                    buffer.setColor(color >> 16 & 255, color >> 8 & 255, color & 255, 255);

                    LivingEntityRenderer<LivingEntity, ?> entityrenderer = (LivingEntityRenderer<LivingEntity, ?>) event.getRenderer();

                    entityrenderer.render(entity, Mth.lerp(event.getPartialTick(), entity.yRotO, entity.getYRot()), event.getPartialTick(), event.getPoseStack(), buffer, entityrenderer.getPackedLightCoords(entity, event.getPartialTick()));

                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

                    currentlyNotRendering = true;
                }

                int delta = player.tickCount - entityBloodRequestTimestampMap.getOrDefault(entity.getId(), 0);

                if (delta >= 10 || delta < 0)
                {
                    ModNetworkDispatcher.sendToServer(new RequestEntityBloodC2SPacket(entity.getId(), false));

                    entityBloodRequestTimestampMap.put(entity.getId(), player.tickCount);
                }
            }
        }
    }
}
