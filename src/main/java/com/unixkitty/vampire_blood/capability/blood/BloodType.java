package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.util.VampirismTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum BloodType implements VampirismTier
{
    NONE(0, 0, 0, 0, 0),
    FRAIL(1, 0.3333333333333333, 0.3333333333333333, 0.3333333333333333, 0.3333333333333333),
    CREATURE(2, 0.5, 0.5, 0.5, 1),
    HUMAN(3, 1, 1, 1, 2),
    VAMPIRE(4, 1.25, 1, 1.25, 0.75),
    PIGLIN(5, 0.75, 0.75, 0.75, 1.5);

    public static final String BLOODTYPE_NBT_NAME = "bloodType";

    private final int id;
    private final double healthMultiplier;
    private final double strengthMultiplier;
    private final double speedBoostModifier;
    private final double bloodSaturationModifier;

    BloodType(int id, double healthMultiplier, double strengthMultiplier, double speedBoostModifier, double bloodSaturationModifier)
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

    public double getBloodSaturationModifier()
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
    public int getColor()
    {
        return switch (this)
        {
            case NONE -> ChatFormatting.WHITE.getColor();
            case FRAIL -> ChatFormatting.GRAY.getColor();
            case CREATURE -> ChatFormatting.LIGHT_PURPLE.getColor();
            case HUMAN -> ChatFormatting.DARK_RED.getColor();
            case VAMPIRE -> ChatFormatting.DARK_PURPLE.getColor();
            case PIGLIN -> ChatFormatting.GOLD.getColor();
        };
    }

    @Override
    public Component getTranslation()
    {
        return this.getTranslation("blood_type");
    }
}
