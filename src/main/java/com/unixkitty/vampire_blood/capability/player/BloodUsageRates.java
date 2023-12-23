package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.config.Config;

public enum BloodUsageRates
{
    IDLE,
    HEALING;

    int get()
    {
        return switch (this)
        {
            case HEALING -> (Config.bloodUsageRate.get() * 2) * Config.naturalHealingRate.get();
            case IDLE -> 1;
        };
    }

    static int getForAbility(VampireActiveAbility ability)
    {
        return switch (ability)
        {
            case SPEED -> Config.bloodUsageRate.get();
            case SENSES -> Config.bloodUsageRate.get() / 18;
            default -> 0;
        };
    }
}
