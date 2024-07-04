package com.unixkitty.vampire_blood.capability.player;

public enum VampirismLevel implements VampirismTier<VampirismLevel>
{
    NOT_VAMPIRE(-1, 0),
    IN_TRANSITION(0, 0),
    FLEDGLING(1, 1.25F),
    VAMPIRE(2, 1.0F),
    MATURE(3, 0.75F),
    ORIGINAL(999, 0.5F);

    private final int id;
    private final float bloodUsageMultiplier;

    VampirismLevel(int id, float bloodUsageMultiplier)
    {
        this.id = id;
        this.bloodUsageMultiplier = bloodUsageMultiplier;
    }

    @Override
    public int getId()
    {
        return id;
    }

    public float getBloodUsageMultiplier()
    {
        return bloodUsageMultiplier;
    }

    @Override
    public float getBloodlustMultiplier(boolean bloodPointGained)
    {
        return switch (this)
        {
            case NOT_VAMPIRE, IN_TRANSITION -> 0.0F;
            case FLEDGLING -> bloodPointGained ? 1.75F : 3.5F;
            case VAMPIRE -> bloodPointGained ? 3F : 2.75F;
            case MATURE -> bloodPointGained ? 3.75F : 1.75F;
            case ORIGINAL -> bloodPointGained ? 5F : 1.25F;
        };
    }

    @Override
    public double getAttributeMultiplier(VampireAttributeModifier modifier)
    {
        return switch (modifier)
        {
            case HEALTH, STRENGTH, BASE_SPEED, ATTACK_SPEED -> this.ordinal();
            case STEP_HEIGHT -> 1.0D;
        };
    }
}
