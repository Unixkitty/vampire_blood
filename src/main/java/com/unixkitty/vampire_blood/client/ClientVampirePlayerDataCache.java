package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.VampireBloodType;
import com.unixkitty.vampire_blood.capability.VampirismStage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//Stores copies of some data in this class to go around having to call the capability for every little thing
@OnlyIn(Dist.CLIENT)
public final class ClientVampirePlayerDataCache
{
    private ClientVampirePlayerDataCache() {}

    /*
        General
     */
    public static VampirismStage vampireLevel = VampirismStage.NOT_VAMPIRE;
    public static VampireBloodType bloodType = VampireBloodType.HUMAN;
    public static boolean isFeeding = false;
    public static int ticksFeeding = 0;

    public static boolean playerJustRespawned = false;

    /*
        Blood data
     */
    public static int thirstLevel = 0;

    public static boolean isVampire()
    {
        return vampireLevel.getId() > VampirismStage.IN_TRANSITION.getId();
    }

    public static final class Debug
    {
        public static int ticksInSun = 0;
        public static int noRegenTicks = 0;
        public static int thirstExhaustion = 0;
        public static int thirstExhaustionIncrement = 0;
        public static int thirstTickTimer = 0;
    }
}
