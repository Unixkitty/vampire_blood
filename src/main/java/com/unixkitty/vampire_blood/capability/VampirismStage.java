package com.unixkitty.vampire_blood.capability;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;

public enum VampirismStage
{
    NOT_VAMPIRE(-1, 1, 1, 1, 0, 0),
    IN_TRANSITION(0, 1, 1, 1, 0, 0.5F),
    FLEDGLING(1, 3, 2, 2, 2, 0.25F),
    VAMPIRE(2, 4, 3, 3, 1.5F, 0.125F),
    MATURE(3, 5, 4, 4, 1, 0),
    ORIGINAL(999, 10, 5, 4, 0.5F, 0);

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

    public static VampirismStage fromId(int id)
    {
        for (VampirismStage stage : values())
        {
            if (stage.id == id) return stage;
        }

        return null;
    }
}
