package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.util.VampirismTier;
import net.minecraft.ChatFormatting;

public enum BloodType implements VampirismTier<BloodType>
{
    NONE(0, 0, 0, 0, 0),
    FRAIL(1, 0.3333333333333333, 0.3333333333333333, 0.3333333333333333, 0.33F),
    CREATURE(2, 0.5, 0.5, 0.5, 1F),
    HUMAN(3, 1, 1, 1, 2F),
    VAMPIRE(4, 1.25, 1, 1.25, 1F),
    PIGLIN(5, 0.75, 0.75, 0.75, 1.5F);

    public static final String BLOODTYPE_NBT_NAME = "bloodType";

    private final int id;
    private final double healthMultiplier;
    private final double strengthMultiplier;
    private final double speedBoostModifier;
    private final float bloodSaturationModifier;

    BloodType(int id, double healthMultiplier, double strengthMultiplier, double speedBoostModifier, float bloodSaturationModifier)
    {
        this.id = id;
        this.healthMultiplier = healthMultiplier;
        this.strengthMultiplier = strengthMultiplier;
        this.speedBoostModifier = speedBoostModifier;
        this.bloodSaturationModifier = bloodSaturationModifier;
    }

    @Override
    public int getId()
    {
        return id;
    }

    public double getSpeedBoostModifier()
    {
        return speedBoostModifier;
    }

    public float getBloodSaturationModifier()
    {
        return bloodSaturationModifier;
    }

    @Override
    public float getBloodlustMultiplier(boolean bloodPointGained)
    {
        return switch (this)
        {
            case NONE -> 0.0F;
            case FRAIL -> bloodPointGained ? 1F : 3.75F;
            case CREATURE -> bloodPointGained ? 2F : 2.5F;
            case HUMAN -> bloodPointGained ? 3.75F : 1.5F;
            case VAMPIRE -> bloodPointGained ? 3F : 1.25F;
            case PIGLIN -> bloodPointGained ? 2F : 2.5F;
        };
    }

    @Override
    public double getAttributeMultiplier(VampireAttributeModifiers.Modifier modifier)
    {
        return switch (modifier)
        {
            case HEALTH -> healthMultiplier;
            case STRENGTH -> strengthMultiplier;
            case BASE_SPEED -> 1;
        };
    }

    @Override
    public ChatFormatting getChatFormatting()
    {
        return switch (this)
        {
            case NONE -> ChatFormatting.WHITE;
            case FRAIL -> ChatFormatting.GRAY;
            case CREATURE -> ChatFormatting.LIGHT_PURPLE;
            case HUMAN -> ChatFormatting.DARK_RED;
            case VAMPIRE -> ChatFormatting.DARK_PURPLE;
            case PIGLIN -> ChatFormatting.GOLD;
        };
    }
}
