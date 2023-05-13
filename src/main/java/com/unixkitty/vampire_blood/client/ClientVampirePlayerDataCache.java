package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerBloodData;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//Stores copies of some data in this class to go around having to call the capability for every little thing
@OnlyIn(Dist.CLIENT)
public final class ClientVampirePlayerDataCache
{

    private ClientVampirePlayerDataCache() {}

    // General
    public static VampirismStage vampireLevel = VampirismStage.NOT_VAMPIRE;
    public static BloodType bloodType = BloodType.HUMAN;
    public static boolean feeding = false;
    public static int ticksFeeding = 0;

    public static boolean playerJustRespawned = false;

    //Blood data
    public static int thirstLevel = 0;
    public static int thirstExhaustion = 0;
    public static float bloodlust = 0;

    public static boolean canFeed()
    {
        return vampireLevel.getId() > VampirismStage.NOT_VAMPIRE.getId();
    }

    public static boolean isVampire()
    {
        return vampireLevel.getId() > VampirismStage.IN_TRANSITION.getId();
    }

    public static boolean isHungry()
    {
        return thirstLevel <= VampirePlayerBloodData.MAX_THIRST / 6;
    }

    public static final class Debug
    {
        public static int ticksInSun = 0;
        public static int thirstExhaustionIncrement = 0;
        public static int thirstTickTimer = 0;
        public static int noRegenTicks = 0;
        public static BloodType lastConsumedBloodType = BloodType.NONE;
        public static int consecutiveBloodPoints = 0;
    }
}
