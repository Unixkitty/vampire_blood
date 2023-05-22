package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import net.minecraft.client.Minecraft;
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
                }
            }
        }
    }

    /*public static void handleVampireData(VampirismLevel vampireLevel, BloodType bloodType, int thirstLevel, int thirstExhaustion, float bloodlust, float bloodPurity)
    {
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                ModCache.getVampireVars().getVampireLevel() = vampirePlayerData.setClientVampireLevel(vampireLevel);
                ModCache.getVampireVars().getBloodType() = vampirePlayerData.setClientBloodType(bloodType);

                ModCache.getVampireVars().thirstLevel = vampirePlayerData.setClientBlood(thirstLevel);
                ModCache.getVampireVars().thirstExhaustion = vampirePlayerData.setClientExhaustion(thirstExhaustion);
                ModCache.getVampireVars().bloodlust = vampirePlayerData.setClientBloodlust(bloodlust);
                ModCache.getVampireVars().bloodPurity = bloodPurity;
            });
        }
    }*/

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
