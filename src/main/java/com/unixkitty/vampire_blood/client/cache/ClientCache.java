package com.unixkitty.vampire_blood.client.cache;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerBloodData;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.config.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class ClientCache
{
    @Nullable
    private static ModVampirePlayerVarsCache vampirePlayerVars;
    @Nullable
    private static ModDebugVarsCache debugVars;

    public static void reset()
    {
        vampirePlayerVars = null;
        debugVars = null;
    }

    public static ModVampirePlayerVarsCache getVampireVars()
    {
        return vampirePlayerVars == null ? vampirePlayerVars = new ModVampirePlayerVarsCache() : vampirePlayerVars;
    }

    @Nullable
    public static ModDebugVarsCache getDebugVars()
    {
        return debugVars == null ? Config.debug.get() ? debugVars = new ModDebugVarsCache() : null : debugVars;
    }

    //========================================

    public static boolean canFeed()
    {
        return getVampireVars().getVampireLevel().getId() > VampirismLevel.NOT_VAMPIRE.getId();
    }

    public static boolean isVampire()
    {
        return getVampireVars().getVampireLevel().getId() > VampirismLevel.IN_TRANSITION.getId();
    }

    public static boolean isHungry()
    {
        return getVampireVars().thirstLevel <= VampirePlayerBloodData.MAX_THIRST / 6;
    }

    public record EntityBlood(int bloodPoints, int maxBloodPoints, BloodType bloodType){}
}
