package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.config.Config;

public enum BloodUsageRates
{
    IDLE,
    HEALING_SLOW,
    HEALING;

    public int get()
    {
        return switch (this)
        {
            case IDLE -> 1;
            case HEALING_SLOW -> Config.bloodUsageRate.get() / 4;
            case HEALING -> Config.bloodUsageRate.get() / 2;
        };
    }
}
