package com.unixkitty.vampire_blood.capability;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;

public enum VampireBloodType
{
    FRAIL(0.25, 0.25, 0.25, 0.25, 1.5),
    CREATURE(0.5, 0.5, 0.5, 0.5, 1.25),
    HUMAN(1, 1, 1, 1, 1),
    VAMPIRE(1.25, 1, 1, 0.75, 1);

    private final double healthMultiplier;
    private final double strengthMultiplier;
    private final double speedBoostModifier;
    private final double bloodSaturationModifier;
    private final double drainVictimChanceModifier;

    VampireBloodType(double healthMultiplier, double strengthMultiplier, double speedBoostModifier, double bloodSaturationModifier, double drainVictimChanceModifier)
    {
        this.healthMultiplier = healthMultiplier;
        this.strengthMultiplier = strengthMultiplier;
        this.speedBoostModifier = speedBoostModifier;
        this.bloodSaturationModifier = bloodSaturationModifier;
        this.drainVictimChanceModifier = drainVictimChanceModifier;
    }

    public double getHealthMultiplier()
    {
        return healthMultiplier;
    }

    public double getStrengthMultiplier()
    {
        return strengthMultiplier;
    }

    public double getSpeedBoostModifier()
    {
        return speedBoostModifier;
    }

    public double getBloodSaturationModifier()
    {
        return bloodSaturationModifier;
    }

    public double getDrainVictimChanceModifier()
    {
        return drainVictimChanceModifier;
    }

    public double getAttributeMultiplier(VampireAttributeModifiers.Modifier modifier)
    {
        switch (modifier)
        {
            case HEALTH ->
            {
                return healthMultiplier;
            }
            case STRENGTH ->
            {
                return strengthMultiplier;
            }
        }

        return 1;
    }

    public static VampireBloodType fromId(int id)
    {
        for (VampireBloodType type : values())
        {
            if (type.ordinal() == id) return type;
        }

        return null;
    }
}
