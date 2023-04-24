package com.unixkitty.vampire_blood.client;

import com.unixkitty.vampire_blood.capability.VampireBloodType;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
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
    public static VampirePlayerData.Stage vampireLevel = VampirePlayerData.Stage.NOT_VAMPIRE;
    public static VampireBloodType bloodType = VampireBloodType.NONE;
    public static int ticksInSun = 0;
    public static boolean isFeeding = false;
    public static int ticksFeeding = 0;

    /*
        Blood data
     */
    public static int thirstLevel = 0;

    public static boolean isVampire()
    {
        return vampireLevel != VampirePlayerData.Stage.NOT_VAMPIRE && vampireLevel != VampirePlayerData.Stage.IN_TRANSITION;
    }

    public static final class Debug
    {
        public static int thirstExhaustion = 0;
        public static int thirstExhaustionIncrement = 0;
        public static int thirstTickTimer = 0;
    }
}
