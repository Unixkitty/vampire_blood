package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerBloodData;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//Stores copies of some data in this class to go around having to call the capability for every little thing on the client
@OnlyIn(Dist.CLIENT)
public final class ClientVampirePlayerDataCache
{
    private ClientVampirePlayerDataCache() {}

    // General
    public static VampirismStage vampireLevel = VampirismStage.NOT_VAMPIRE;
    public static BloodType bloodType = BloodType.HUMAN;
    public static boolean feeding = false;

    public static boolean playerJustRespawned = false;

    //Blood data
    public static int thirstLevel;
    public static int thirstExhaustion;
    public static float bloodlust;
    public static float bloodPurity;

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
        public static int ticksFeeding;
        public static int ticksInSun;
        public static int thirstExhaustionIncrement;
        public static int thirstTickTimer;
        public static int noRegenTicks;
        public static int[] diet = new int[20];
    }
}
