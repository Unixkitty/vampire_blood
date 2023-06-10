package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModDamageSources;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class AbstractBloodVessel implements IBloodVessel
{
    protected Object2IntOpenHashMap<UUID> charmedByMap = null;

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
        }
    }

    @Override
    public boolean isCharmedBy(ServerPlayer player)
    {
        return this.charmedByMap != null && this.charmedByMap.containsKey(player.getUUID());
    }

    @Override
    public void setCharmedBy(ServerPlayer player)
    {
        if (this.charmedByMap == null)
        {
            this.charmedByMap = new Object2IntOpenHashMap<>();
        }

        this.charmedByMap.put(player.getUUID(), (int) Config.charmedEffectDuration.get());
    }

    @Override
    public void tryGetCharmed(ServerPlayer player, VampirismLevel attackerLevel)
    {
        switch (getBloodType())
        {
            case VAMPIRE ->
            {
                if (attackerLevel == VampirismLevel.ORIGINAL)
                {
                    setCharmedBy(player);
                }
            }
            case CREATURE, HUMAN, PIGLIN -> setCharmedBy(player);
        }
    }

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
}
