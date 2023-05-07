package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum BloodType
{
    NONE(0, 0, 0, 0, 0, 0),
    FRAIL(1, 0.3333333333333333, 0.3333333333333333, 0.3333333333333333, 0.3333333333333333, 1.5),
    CREATURE(2, 0.5, 0.5, 0.5, 1, 1.25),
    HUMAN(3, 1, 1, 1, 2, 1),
    VAMPIRE(4, 1.25, 1, 1.25, 0.75, 1),
    PIGLIN(5, 0.75, 0.75, 0.75, 1.5, 1.25);

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

    public int getColor()
    {
        return switch (this)
        {
            case NONE -> ChatFormatting.WHITE.getColor();
            case FRAIL -> ChatFormatting.GRAY.getColor();
            case CREATURE -> ChatFormatting.GREEN.getColor();
            case HUMAN -> ChatFormatting.LIGHT_PURPLE.getColor();
            case VAMPIRE -> ChatFormatting.DARK_RED.getColor();
            case PIGLIN -> ChatFormatting.GOLD.getColor();
        };
    }

    public Component getTranslation()
    {
        return Component.translatable("vampire_blood.blood_type." + this.toString().toLowerCase());
    }
}
