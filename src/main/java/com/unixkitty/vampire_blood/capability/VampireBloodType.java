package com.unixkitty.vampire_blood.capability;

public enum VampireBloodType
{
    NONE,
    FRAIL,
    WEAK,
    NORMAL,
    VAMPIRE;

    public static VampireBloodType fromId(int id)
    {
        for (VampireBloodType type : values())
        {
            if (type.ordinal() == id) return type;
        }

        return NONE;
    }
}
