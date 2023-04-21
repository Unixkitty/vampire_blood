package com.unixkitty.vampire_blood.capability;

import com.unixkitty.vampire_blood.Config;

public enum BloodRates
{
    IDLE,
    HEALING;

    public int get()
    {
        switch (this)
        {
            case IDLE ->
            {
                return 1;
            }
/*            case MOVING ->
            {
                return Config.bloodUsageRate.get() / 120;
            }
            case SPRINTING ->
            {
                return Config.bloodUsageRate.get() / 12;
            }*/
            case HEALING ->
            {
                return Config.bloodUsageRate.get() / 2;
            }
        }

        return 0;
    }
}
