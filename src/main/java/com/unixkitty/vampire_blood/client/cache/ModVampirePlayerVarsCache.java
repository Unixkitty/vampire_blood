package com.unixkitty.vampire_blood.client.cache;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.network.packet.PlayerVampireDataS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ModVampirePlayerVarsCache
{
    private final Map<Key, Object> cache = new ConcurrentHashMap<>();

    //General
    public boolean feeding = false;
    public final ArrayList<VampireActiveAbility> activeAbilities = new ArrayList<>();

    //Blood data
    public int thirstLevel = 0;
    public int thirstExhaustion = 0;
    public float bloodlust = 0;
    public float bloodPurity = 0;

    public void setVampireLevel(VampirismLevel vampireLevel)
    {
        this.cache.put(Key.VAMPIRE_LEVEL, vampireLevel);
    }

    public VampirismLevel getVampireLevel()
    {
        return (VampirismLevel) this.cache.getOrDefault(Key.VAMPIRE_LEVEL, VampirismLevel.NOT_VAMPIRE);
    }

    public void setBloodType(BloodType bloodType)
    {
        this.cache.put(Key.BLOOD_TYPE, bloodType);
    }

    public BloodType getBloodType()
    {
        return (BloodType) this.cache.getOrDefault(Key.BLOOD_TYPE, BloodType.HUMAN);
    }

    public void handleVampireDataPacket(final PlayerVampireDataS2CPacket packet)
    {
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                this.cache.put(Key.VAMPIRE_LEVEL, vampirePlayerData.setClientVampireLevel(packet.vampireLevel));
                this.cache.put(Key.BLOOD_TYPE, vampirePlayerData.setClientBloodType(packet.bloodType));

                this.thirstLevel = vampirePlayerData.setClientBlood(packet.thirstLevel);
                this.thirstExhaustion = vampirePlayerData.setClientExhaustion(packet.thirstExhaustion);
                this.bloodlust = vampirePlayerData.setClientBloodlust(packet.bloodlust);
                this.bloodPurity = packet.bloodPurity;
            });
        }
    }

    private enum Key
    {
        VAMPIRE_LEVEL,
        BLOOD_TYPE
    }
}
