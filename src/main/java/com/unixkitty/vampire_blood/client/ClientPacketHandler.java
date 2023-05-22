package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.cache.ClientVampirePlayerDataCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler
{
    public static void handleSyncAbilities(int[] abilities)
    {
        List<VampireActiveAbility> previousList = new ArrayList<>(ClientVampirePlayerDataCache.activeAbilities);

        ClientVampirePlayerDataCache.activeAbilities.clear();

        for (int id : abilities)
        {
            ClientVampirePlayerDataCache.activeAbilities.add(VampireActiveAbility.fromOrdinal(id));
        }

        if (Minecraft.getInstance().player != null)
        {
            for (VampireActiveAbility ability : VampireActiveAbility.values())
            {
                if (ClientVampirePlayerDataCache.activeAbilities.contains(ability))
                {
                    ability.refresh(Minecraft.getInstance().player);
                }
                else if (previousList.contains(ability))
                {
                    ability.stop(Minecraft.getInstance().player);
                }
            }
        }
    }

    public static void handleVampireData(VampirismStage vampireLevel, BloodType bloodType, int thirstLevel, int thirstExhaustion, float bloodlust, float bloodPurity)
    {
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                ClientVampirePlayerDataCache.vampireLevel = vampirePlayerData.setClientVampireLevel(vampireLevel);
                ClientVampirePlayerDataCache.bloodType = vampirePlayerData.setClientBloodType(bloodType);

                ClientVampirePlayerDataCache.thirstLevel = vampirePlayerData.setClientBlood(thirstLevel);
                ClientVampirePlayerDataCache.thirstExhaustion = vampirePlayerData.setClientExhaustion(thirstExhaustion);
                ClientVampirePlayerDataCache.bloodlust = vampirePlayerData.setClientBloodlust(bloodlust);
                ClientVampirePlayerDataCache.bloodPurity = bloodPurity;
            });
        }
    }

    public static void handleDebugData(int ticksFeeding, int ticksInSun, int noRegenTicks, int thirstExhaustionIncrement, int thirstTickTimer, int[] diet)
    {
        ClientVampirePlayerDataCache.Debug.ticksFeeding = ticksFeeding;
        ClientVampirePlayerDataCache.Debug.ticksInSun = ticksInSun;
        ClientVampirePlayerDataCache.Debug.noRegenTicks = noRegenTicks;

        ClientVampirePlayerDataCache.Debug.thirstExhaustionIncrement = thirstExhaustionIncrement;
        ClientVampirePlayerDataCache.Debug.thirstTickTimer = thirstTickTimer;

        ArrayUtils.reverse(diet);
        System.arraycopy(diet, 0, ClientVampirePlayerDataCache.Debug.diet, 0, diet.length);
    }

    public static void handleFeedingStatus(boolean feeding)
    {
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                    ClientVampirePlayerDataCache.feeding = vampirePlayerData.setFeeding(feeding));
        }
    }

    public static void handleEntityBloodInfo(BloodType bloodType, int bloodPoints, int maxBloodPoints)
    {
        FeedingMouseOverHandler.setData(bloodType, bloodPoints, maxBloodPoints);
    }

    public static void handleAvoidHurtAnim(float health)
    {
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.setHealth(health);
        }
    }
}
