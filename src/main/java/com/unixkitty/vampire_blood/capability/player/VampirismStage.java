package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;

public enum VampirismStage
{
    NOT_VAMPIRE(-1, 1, 1, 1, 0, 0),
    IN_TRANSITION(0, 1, 2, 1, 0, 0.5F),
    FLEDGLING(1, 3, 3, 2, 1.75F, 0.25F),
    VAMPIRE(2, 4, 4, 3, 1.5F, 0.125F),
    MATURE(3, 5, 5, 4, 1.25F, 0),
    ORIGINAL(999, 10, 6, 5, 1.0F, 0);

    final int id;
    final double healthMultiplier;
    final double strengthMultiplier;
    final float speedBoostMultiplier;
    final float bloodUsageMultiplier;
    final float drainVictimBaseChance;

    VampirismStage(int id, double healthMultiplier, double strengthMultiplier, float speedBoostMultiplier, float bloodUsageMultiplier, float drainVictimBaseChance)
    {
        this.id = id;
        this.healthMultiplier = healthMultiplier;
        this.strengthMultiplier = strengthMultiplier;
        this.speedBoostMultiplier = speedBoostMultiplier;
        this.bloodUsageMultiplier = bloodUsageMultiplier;
        this.drainVictimBaseChance = drainVictimBaseChance;
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

    public float getSpeedBoostMultiplier()
    {
        return speedBoostMultiplier;
    }

    public float getBloodUsageMultiplier()
    {
        return bloodUsageMultiplier;
    }

    public float getDrainVictimBaseChance()
    {
        return drainVictimBaseChance;
    }

    public double getAttributeMultiplier(VampireAttributeModifiers.Modifier modifier)
    {
        return switch (modifier)
        {
            case HEALTH -> healthMultiplier;
            case STRENGTH -> strengthMultiplier;
            case BASE_SPEED -> this == IN_TRANSITION ? 1.25 : 1;
        };
    }

    public static VampirismStage fromId(int id)
    {
        for (VampirismStage stage : values())
        {
            if (stage.id == id) return stage;
        }

        return null;
    }
}
