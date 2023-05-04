package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;

public enum BloodType
{
    NONE(0, 0, 0, 0, 0, 0),
    FRAIL(1, 0.3333333333333333, 0.25, 0.25, 0.25, 1.5),
    CREATURE(2, 0.5, 0.5, 0.5, 0.5, 1.25),
    HUMAN(3, 1, 1, 1, 1, 1),
    VAMPIRE(4, 1.25, 1, 1.25, 0.25, 1),
    PIGLIN(5, 0.75, 0.75, 0.75, 0.75, 1.25);

    public static final String BLOODTYPE_NBT_NAME = "bloodType";

    private final int id;
    private final double healthMultiplier;
    private final double strengthMultiplier;
    private final double speedBoostModifier;
    private final double bloodSaturationModifier;
    private final double drainVictimChanceModifier;

    BloodType(int id, double healthMultiplier, double strengthMultiplier, double speedBoostModifier, double bloodSaturationModifier, double drainVictimChanceModifier)
    {
        this.id = id;
        this.healthMultiplier = healthMultiplier;
        this.strengthMultiplier = strengthMultiplier;
        this.speedBoostModifier = speedBoostModifier;
        this.bloodSaturationModifier = bloodSaturationModifier;
        this.drainVictimChanceModifier = drainVictimChanceModifier;
    }

    public int getId()
    {
        return id;
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
            if (type.id == id) return type;
        }

        return null;
    }
}
