package com.unixkitty.vampire_blood.capability;

public enum VampirismStage
{
    NOT_VAMPIRE(-1, 1, 1, 1, 0, 0),
    IN_TRANSITION(0, 1, 1, 1, 0, 0.5F),
    FLEDGLING(1, 3, 2, 2, 2, 0.25F),
    VAMPIRE(2, 4, 3, 3, 1.5F, 0.125F),
    MATURE(3, 5, 4, 4, 1, 0),
    ORIGINAL(999, 10, 5, 4, 0.5F, 0);

    final int id;
    final float healthMultiplier;
    final float attackMultiplier;
    final float speedBoostMultiplier;
    final float bloodUsageMultiplier;
    final float drainVictimBaseChance;

    VampirismStage(int id, float healthMultiplier, float attackMultiplier, float speedBoostMultiplier, float bloodUsageMultiplier, float drainVictimBaseChance)
    {
        this.id = id;
        this.healthMultiplier = healthMultiplier;
        this.attackMultiplier = attackMultiplier;
        this.speedBoostMultiplier = speedBoostMultiplier;
        this.bloodUsageMultiplier = bloodUsageMultiplier;
        this.drainVictimBaseChance = drainVictimBaseChance;
    }

    public int getId()
    {
        return id;
    }

    public static VampirismStage fromId(int id)
    {
        for (VampirismStage stage : values())
        {
            if (stage.id == id) return stage;
        }

        return NOT_VAMPIRE;
    }
}
