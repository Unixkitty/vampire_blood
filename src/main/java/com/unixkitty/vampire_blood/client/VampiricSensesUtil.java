package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.RequestEntityOutlineColorC2SPacket;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class VampiricSensesUtil
{
    private static final Int2IntOpenHashMap entityGlowRequestTimestampMap = new Int2IntOpenHashMap();

    public static boolean shouldEntityGlow(@Nonnull LivingEntity entity)
    {
        Player player = Minecraft.getInstance().player;

        if (player != null && (entity instanceof Player && !entity.isSpectator() || entity instanceof PathfinderMob) && player.hasEffect(ModEffects.ENHANCED_SENSES.get()) && !(entity.isInFluidType() && !entity.getEyeInFluidType().isAir()) && entity.distanceTo(player) < ModEffects.SENSES_DISTANCE_LIMIT)
        {
            if (ClientCache.needsEntityOutlineColor(entity))
            {
                int delta = player.tickCount - entityGlowRequestTimestampMap.getOrDefault(entity.getId(), 0);

                if (delta >= 10 || delta < 0)
                {
                    ModNetworkDispatcher.sendToServer(new RequestEntityOutlineColorC2SPacket(entity.getId()));

                    entityGlowRequestTimestampMap.put(entity.getId(), player.tickCount);
                }
            }

            return true;
        }

        return false;
    }

    public static int getEntityGlowColor(@Nonnull Entity entity)
    {
        if (entity instanceof Player && ((Player) entity).isCreative())
        {
            return Color.HSBtoRGB((entity.tickCount % 100) / 100F, 1.0F, 1.0F);
        }
        else if (entity instanceof LivingEntity)
        {
            return ClientCache.getVampireVars().getEntityOutlineColor(entity.getId());
        }

        return -1;
    }
}
