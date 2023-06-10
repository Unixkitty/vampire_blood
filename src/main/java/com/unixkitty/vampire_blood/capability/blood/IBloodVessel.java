package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.init.ModDamageSources;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IBloodVessel
{
    String CHARMED_BY_NBT_NAME = "charmedBy";

    boolean isEdible();

    int getBloodPoints();

    int getMaxBloodPoints();

    BloodType getBloodType();

    default void dieFromBloodLoss(@Nonnull LivingEntity victim, @Nonnull LivingEntity attacker)
    {
        victim.setLastHurtByMob(attacker);
        victim.hurt(ModDamageSources.BLOOD_LOSS, Float.MAX_VALUE);
    }

    default void drinkFromHealth(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim, @Nonnull BloodType bloodType)
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

    boolean decreaseBlood(@Nonnull LivingEntity attacker, @Nonnull LivingEntity victim);

    default void tryGetCharmed(ServerPlayer player, VampirismLevel attackerLevel)
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

    boolean isCharmedBy(ServerPlayer player);

    void setCharmedBy(ServerPlayer player);

    void handleBeingCharmedTicks(LivingEntity entity);

    default void saveCharmedByMap(CompoundTag tag, final Object2IntOpenHashMap<UUID> charmedByMap)
    {
        if (charmedByMap != null)
        {
            CompoundTag subTag = new CompoundTag();

            charmedByMap.forEach((uuid, integer) -> subTag.putInt(uuid.toString(), integer));

            tag.put(CHARMED_BY_NBT_NAME, subTag);
        }
    }

    default Object2IntOpenHashMap<UUID> loadCharmedByMap(CompoundTag tag)
    {
        CompoundTag subTag = tag.getCompound(CHARMED_BY_NBT_NAME);

        Object2IntOpenHashMap<UUID> charmedByMap = null;

        if (subTag.size() > 0)
        {
            charmedByMap = new Object2IntOpenHashMap<>();

            for (String key : subTag.getAllKeys())
            {
                charmedByMap.put(UUID.fromString(key), subTag.getInt(key));
            }
        }

        return charmedByMap;
    }
}
