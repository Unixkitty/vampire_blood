package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;

public enum BloodType
{
    FRAIL(0.3333333333333333, 0.25, 0.25, 0.25, 1.5),
    CREATURE(0.5, 0.5, 0.5, 0.5, 1.25),
    PIGLIN(0.75, 0.75, 0.75, 0.75, 1.25),
    HUMAN(1, 1, 1, 1, 1),
    VAMPIRE(1.25, 1, 1.25, 0.25, 1),
    NONE(0, 0, 0, 0, 0);

    final double healthMultiplier;
    final double strengthMultiplier;
    final double speedBoostModifier;
    final double bloodSaturationModifier;
    final double drainVictimChanceModifier;

    BloodType(double healthMultiplier, double strengthMultiplier, double speedBoostModifier, double bloodSaturationModifier, double drainVictimChanceModifier)
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
        return switch (modifier)
        {
            case HEALTH -> healthMultiplier;
            case STRENGTH -> strengthMultiplier;
            case BASE_SPEED -> 1;
        };
    }

    public static BloodType fromId(int id)
    {
        for (BloodType type : values())
        {
            if (type.ordinal() == id) return type;
        }

        return null;
    }
}
