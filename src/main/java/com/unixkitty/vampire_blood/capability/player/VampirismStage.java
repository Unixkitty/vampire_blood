package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.util.VampirismTier;
import net.minecraft.ChatFormatting;

public enum VampirismStage implements VampirismTier<VampirismStage>
{
    NOT_VAMPIRE(-1, 1, 1, 1, 0),
    IN_TRANSITION(0, 1, 2, 1, 0),
    FLEDGLING(1, 3, 3, 2, 1.75F),
    VAMPIRE(2, 4, 4, 3, 1.5F),
    MATURE(3, 5, 5, 4, 1.25F),
    ORIGINAL(999, 10, 6, 5, 1.0F);

    private final int id;
    private final double healthMultiplier;
    private final double strengthMultiplier;
    private final float speedBoostMultiplier;
    private final float bloodUsageMultiplier;

    VampirismStage(int id, double healthMultiplier, double strengthMultiplier, float speedBoostMultiplier, float bloodUsageMultiplier)
    {
        this.id = id;
        this.healthMultiplier = healthMultiplier;
        this.strengthMultiplier = strengthMultiplier;
        this.speedBoostMultiplier = speedBoostMultiplier;
        this.bloodUsageMultiplier = bloodUsageMultiplier;
    }

    @Override
    public int getId()
    {
        return id;
    }

    public float getSpeedBoostMultiplier()
    {
        return speedBoostMultiplier;
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
    public double getAttributeMultiplier(VampireAttributeModifiers.Modifier modifier)
    {
        return switch (modifier)
        {
            case HEALTH -> healthMultiplier;
            case STRENGTH -> strengthMultiplier;
            case BASE_SPEED -> this == IN_TRANSITION ? 1.25 : 1;
        };
    }

    @Override
    public ChatFormatting getChatFormatting()
    {
        return switch (this)
        {
            case NOT_VAMPIRE -> ChatFormatting.WHITE;
            case IN_TRANSITION -> ChatFormatting.GRAY;
            case FLEDGLING -> ChatFormatting.RED;
            case VAMPIRE -> ChatFormatting.LIGHT_PURPLE;
            case MATURE -> ChatFormatting.DARK_PURPLE;
            case ORIGINAL -> ChatFormatting.DARK_RED;
        };
    }
}
