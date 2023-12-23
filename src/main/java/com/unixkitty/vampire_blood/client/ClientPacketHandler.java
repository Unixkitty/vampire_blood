package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.player.VampirismTier;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import com.unixkitty.vampire_blood.init.ModParticles;
import com.unixkitty.vampire_blood.network.packet.EntityBloodInfoS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler
{
    public static void handleSyncAbilities(int[] abilities)
    {
        final List<VampireActiveAbility> lastList = List.copyOf(ClientCache.getVampireVars().activeAbilities);

        ClientCache.getVampireVars().activeAbilities.clear();

        for (int id : abilities)
        {
            ClientCache.getVampireVars().activeAbilities.add(VampireActiveAbility.fromOrdinal(id));
        }

        if (Minecraft.getInstance().player != null)
        {
            for (VampireActiveAbility ability : VampireActiveAbility.values())
            {
                if (ClientCache.getVampireVars().activeAbilities.contains(ability))
                {
                    ability.refresh(Minecraft.getInstance().player);
                }
                else if (lastList.contains(ability))
                {
                    ability.stop(Minecraft.getInstance().player);

                    if (ability == VampireActiveAbility.SENSES)
                    {
                        ClientCache.getVampireVars().invalidateOutlineColors();
                    }
                    else if (ability == VampireActiveAbility.BLOOD_VISION)
                    {
                        ClientCache.getVampireVars().invalidateEntityBloodValues();
                    }
                }
            }
        }
    }

    public static void handleDebugData(int ticksInSun, int noRegenTicks, int thirstExhaustionIncrement, int thirstTickTimer, int[] diet)
    {
        ClientCache.getDebugVars().ticksInSun = ticksInSun;
        ClientCache.getDebugVars().noRegenTicks = noRegenTicks;

        ClientCache.getDebugVars().thirstExhaustionIncrement = thirstExhaustionIncrement;
        ClientCache.getDebugVars().thirstTickTimer = thirstTickTimer;

        ArrayUtils.reverse(diet);
        System.arraycopy(diet, 0, ClientCache.getDebugVars().diet, 0, diet.length);
    }

    public static void handleFeedingStatus(boolean feeding)
    {
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                    ClientCache.getVampireVars().feeding = vampirePlayerData.setFeeding(feeding));
        }
    }

    public static void handleEntityBloodInfo(EntityBloodInfoS2CPacket packet)
    {
        if (packet.lookingDirectly)
        {
            FeedingMouseOverHandler.setData(packet.bloodType, packet.bloodPoints, packet.maxBloodPoints, packet.charmedTicks);
        }
        else
        {
            ClientCache.getVampireVars().setEntityBloodValues(packet.entityId, packet.bloodPoints, packet.maxBloodPoints, packet.bloodType);
            ClientCache.getVampireVars().setEntityCharmed(packet.entityId, !(packet.charmedTicks == -2 || packet.charmedTicks == 0));
        }
    }

    public static void handleAvoidHurtAnim(float health)
    {
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.setHealth(health);
        }
    }

    public static void handleEntityOutlineColor(int entityId, int color)
    {
        ClientCache.getVampireVars().setEntityOutlineColor(entityId, color);
    }

    public static void handleEntityCharmedStatus(int entityId, boolean charmed)
    {
        ClientCache.getVampireVars().setEntityCharmed(entityId, charmed);
    }

    public static void handleBloodParticles(Vec3 position)
    {
        ClientEvents.spawnBloodParticles(position, false);
    }

    public static void handleSuccessfulCharm(int entityId)
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null && minecraft.cameraEntity == minecraft.player)
        {
            Entity entity = minecraft.player.level.getEntity(entityId);

            if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive())
            {
                spawnParticlesAroundEntity(entity, ModParticles.CHARMED_FEEDBACK_PARTICLE.get());
            }
        }
    }

    public static void spawnParticlesAroundEntity(Entity entity, ParticleOptions particleOptions)
    {
        for (int i = 0; i < 5; ++i)
        {
            entity.level.addParticle(
                    particleOptions,
                    entity.getRandomX(1.0D),
                    entity.getRandomY() + 1.0D,
                    entity.getRandomZ(1.0D),
                    gauss(entity.level.random),
                    gauss(entity.level.random),
                    gauss(entity.level.random)
            );
        }
    }

    public static void handleVampireVarsResponse(int[] playerEntityIds, int[] playerVampireLevels)
    {
        if (playerVampireLevels.length < playerEntityIds.length)
        {
            VampireBlood.LOG.error("Error syncing other player vampire vars back to client");

            return;
        }

        if (playerEntityIds.length > 0)
        {
            ClientLevel clientLevel = Minecraft.getInstance().level;

            if (clientLevel != null)
            {
                for (int i = 0; i < playerEntityIds.length; i++)
                {
                    Entity entity = clientLevel.getEntity(playerEntityIds[i]);

                    if (entity instanceof Player remotePlayer)
                    {
                        final int vampireLevel = playerVampireLevels[i];

                        remotePlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData -> vampirePlayerData.setClientVampireLevel(VampirismTier.fromId(VampirismLevel.class, vampireLevel)));
                    }
                }
            }
        }
    }

    private static double gauss(RandomSource random)
    {
        return random.nextGaussian() * 0.02D;
    }
}
