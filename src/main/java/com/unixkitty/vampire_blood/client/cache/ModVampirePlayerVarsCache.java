package com.unixkitty.vampire_blood.client.cache;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.network.packet.PlayerVampireDataS2CPacket;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ModVampirePlayerVarsCache
{
    private static final int VAMPIRE_LEVEL = 0;
    private static final int BLOOD_TYPE = 1;
    private static final int ENTITY_OUTLINE_COLORS = 2;
    private static final int ENTITY_BLOOD_VALUES = 3;
    
    private final Int2ObjectOpenHashMap<Object> cache = new Int2ObjectOpenHashMap<>();

    //General
    public boolean feeding = false;
    public final ArrayList<VampireActiveAbility> activeAbilities = new ArrayList<>(); //TODO is this actually needed?

    //Blood data
    public int thirstLevel = 0;
    public int thirstExhaustion = 0;
    public float bloodlust = 0;
    public float bloodPurity = 0;

    @SuppressWarnings("unchecked")
    public void setEntityBloodValues(int entityId, int bloodPoints, int maxBloodPoints, BloodType bloodType)
    {
        ((Int2ObjectOpenHashMap<ClientCache.EntityBlood>)this.cache.computeIfAbsent(ENTITY_BLOOD_VALUES, k -> new Int2ObjectOpenHashMap<ClientCache.EntityBlood>())).put(entityId, new ClientCache.EntityBlood(bloodPoints, maxBloodPoints, bloodType));
    }

    @SuppressWarnings("unchecked")
    public ClientCache.EntityBlood getEntityBlood(int entityId)
    {
        if (this.cache.containsKey(ENTITY_BLOOD_VALUES))
        {
            return ((Int2ObjectOpenHashMap<ClientCache.EntityBlood>)this.cache.get(ENTITY_BLOOD_VALUES)).getOrDefault(entityId, null);
        }

        return null;
    }

    public void invalidateEntityBloodValues()
    {
        this.cache.remove(ENTITY_BLOOD_VALUES);
    }

    public boolean needsEntityOutlineColor(int entityId)
    {
        if (this.cache.containsKey(ENTITY_OUTLINE_COLORS))
        {
            return !((Int2IntOpenHashMap)this.cache.get(ENTITY_OUTLINE_COLORS)).containsKey(entityId);
        }
        else
        {
            return true;
        }
    }

    public void setEntityOutlineColor(int entityId, int color)
    {
        ((Int2IntOpenHashMap)this.cache.computeIfAbsent(ENTITY_OUTLINE_COLORS, k -> new Int2IntOpenHashMap())).put(entityId, color);
    }

    public int getEntityOutlineColor(int entityId)
    {
        if (this.cache.containsKey(ENTITY_OUTLINE_COLORS))
        {
            return ((Int2IntOpenHashMap)this.cache.get(ENTITY_OUTLINE_COLORS)).getOrDefault(entityId, -1);
        }

        return -1;
    }

    public void invalidateOutlineColors()
    {
        this.cache.remove(ENTITY_OUTLINE_COLORS);
    }

    public void setVampireLevel(@Nonnull VampirismLevel vampireLevel)
    {
        this.cache.put(VAMPIRE_LEVEL, vampireLevel);
    }

    public VampirismLevel getVampireLevel()
    {
        return (VampirismLevel) this.cache.getOrDefault(VAMPIRE_LEVEL, VampirismLevel.NOT_VAMPIRE);
    }

    public void setBloodType(@Nonnull BloodType bloodType)
    {
        this.cache.put(BLOOD_TYPE, bloodType);
    }

    public BloodType getBloodType()
    {
        return (BloodType) this.cache.getOrDefault(BLOOD_TYPE, BloodType.HUMAN);
    }

    public void handleVampireDataPacket(final PlayerVampireDataS2CPacket packet)
    {
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                setVampireLevel(vampirePlayerData.setClientVampireLevel(packet.vampireLevel));
                setBloodType(vampirePlayerData.setClientBloodType(packet.bloodType));

                this.thirstLevel = vampirePlayerData.setClientBlood(packet.thirstLevel);
                this.thirstExhaustion = vampirePlayerData.setClientExhaustion(packet.thirstExhaustion);
                this.bloodlust = vampirePlayerData.setClientBloodlust(packet.bloodlust);
                this.bloodPurity = packet.bloodPurity;
            });
        }
    }
}
