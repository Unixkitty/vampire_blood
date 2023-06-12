package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModDamageSources;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import com.unixkitty.vampire_blood.network.packet.EntityCharmedStatusS2CPacket;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class BloodVessel implements IBloodVessel
{
    protected Object2IntOpenHashMap<UUID> charmedByMap = null;

    public void saveNBTData(CompoundTag tag)
    {
        if (this.charmedByMap != null)
        {
            CompoundTag subTag = new CompoundTag();

            this.charmedByMap.forEach((uuid, integer) -> subTag.putInt(uuid.toString(), integer));

            tag.put(CHARMED_BY_NBT_NAME, subTag);
        }
    }

    public void loadNBTData(CompoundTag tag)
    {
        CompoundTag subTagCharmedBy = tag.getCompound(CHARMED_BY_NBT_NAME);

        if (subTagCharmedBy.size() > 0)
        {
            this.charmedByMap = new Object2IntOpenHashMap<>();

            for (String key : subTagCharmedBy.getAllKeys())
            {
                this.charmedByMap.put(UUID.fromString(key), subTagCharmedBy.getInt(key));
            }
        }
    }

    protected void handleCharmedTicks(LivingEntity entity)
    {
        for (UUID key : this.charmedByMap.keySet())
        {
            int value = this.charmedByMap.getInt(key);

            if (value > 0)
            {
                if (value >= 20)
                {
                    this.charmedByMap.addTo(key, -20);
                }
                else
                {
                    this.charmedByMap.addTo(key, -value);
                }
            }

            if (value == 0)
            {
                this.charmedByMap.removeInt(key);
            }

            ServerPlayer player = (ServerPlayer) entity.level.getPlayerByUUID(key);

            //Check if player is actually logged on and if they're nearby before sending packet
            if (player != null
                    && entity.level.getNearbyPlayers(TargetingConditions.forNonCombat().selector(target -> target.equals(player)), entity, entity.getBoundingBox().inflate(ModEffects.SENSES_DISTANCE_LIMIT)).contains(player)
                    && player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel().getId() > VampirismLevel.IN_TRANSITION.getId()).orElse(false))
            {
                ModNetworkDispatcher.sendToClient(new EntityCharmedStatusS2CPacket(entity.getId(), value != 0), player);
            }
        }
    }

    @Override
    public void dieFromBloodLoss(@NotNull LivingEntity victim, @NotNull LivingEntity attacker)
    {
        victim.setLastHurtByMob(attacker);
        victim.hurt(ModDamageSources.BLOOD_LOSS, Float.MAX_VALUE);
    }

    @Override
    public void drinkFromHealth(@NotNull LivingEntity attacker, @NotNull LivingEntity victim, @NotNull BloodType bloodType)
    {
        float resultingHealth = victim.getHealth() - (1F / bloodType.getBloodSaturationModifier());

        if (resultingHealth > 0)
        {
            victim.setHealth(resultingHealth);
        }
        else
        {
            dieFromBloodLoss(victim, attacker);
        }
    }

    @Override
    public void handleBeingCharmedTicks(@Nonnull LivingEntity entity)
    {
        if (entity.tickCount % 20 == 0 && this.charmedByMap != null && !this.charmedByMap.isEmpty())
        {
            handleCharmedTicks(entity);
        }
    }

    @Override
    public boolean isCharmedBy(ServerPlayer player)
    {
        return this.charmedByMap != null && this.charmedByMap.containsKey(player.getUUID());
    }

    @Override
    public int getCharmedByTicks(ServerPlayer player)
    {
        return this.charmedByMap == null ? -2 : this.charmedByMap.getOrDefault(player.getUUID(), -2);
    }

    @Override
    public boolean setCharmedBy(ServerPlayer player)
    {
        if (this.charmedByMap == null)
        {
            this.charmedByMap = new Object2IntOpenHashMap<>();
        }

        UUID uuid = player.getUUID();

        if (this.charmedByMap.containsKey(uuid))
        {
            this.charmedByMap.removeInt(uuid);
        }
        else
        {
            this.charmedByMap.put(uuid, (int) Config.charmEffectDuration.get());
        }

        return true;
    }

    @Override
    public boolean tryGetCharmed(ServerPlayer player, VampirismLevel attackerLevel)
    {
        return switch (getBloodType())
        {
            case VAMPIRE -> attackerLevel == VampirismLevel.ORIGINAL && setCharmedBy(player);
            case CREATURE, HUMAN, PIGLIN -> setCharmedBy(player);
            default -> false;
        };
    }
}
