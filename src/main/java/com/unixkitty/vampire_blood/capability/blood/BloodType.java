package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.player.VampireAttributeModifier;
import com.unixkitty.vampire_blood.capability.player.VampirismTier;
import net.minecraft.ChatFormatting;

public enum BloodType implements VampirismTier<BloodType>
{
    NONE(0, 0, 0, 0, 0, ChatFormatting.BLACK),
    FRAIL(1, 0.3333333333333333, 0.3333333333333333, 0.3333333333333333, 0.33F, ChatFormatting.GRAY),
    CREATURE(2, 0.5, 0.5, 0.5, 1F, ChatFormatting.GREEN),
    HUMAN(3, 1, 1, 1, 2F, ChatFormatting.DARK_RED),
    VAMPIRE(4, 0.75, 0.75, 1.25, 1F, ChatFormatting.DARK_PURPLE),
    PIGLIN(5, 0.75, 0.75, 0.75, 1.5F, ChatFormatting.GOLD);

    public static final String BLOODTYPE_NBT_NAME = "bloodType";

    private final int id;
    private final double healthMultiplier;
    private final double strengthMultiplier;
    private final double speedBoostModifier;
    private final float bloodSaturationModifier;
    private final ChatFormatting chatFormatting;

    BloodType(int id, double healthMultiplier, double strengthMultiplier, double speedBoostModifier, float bloodSaturationModifier, ChatFormatting formatting)
    {
        this.id = id;
        this.healthMultiplier = healthMultiplier;
        this.strengthMultiplier = strengthMultiplier;
        this.speedBoostModifier = speedBoostModifier;
        this.bloodSaturationModifier = bloodSaturationModifier;
        this.chatFormatting = formatting;
    }

    @Override
    public int getId()
    {
        return id;
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
            case CREATURE, PIGLIN -> bloodPointGained ? 2F : 2.5F;
            case HUMAN -> bloodPointGained ? 3.75F : 1.5F;
            case VAMPIRE -> bloodPointGained ? 3F : 1.25F;
        };
    }

    @Override
    public double getAttributeMultiplier(VampireAttributeModifier modifier)
    {
        return switch (modifier)
        {
            case HEALTH -> healthMultiplier;
            case STRENGTH -> strengthMultiplier;
            case BASE_SPEED, ATTACK_SPEED -> speedBoostModifier;
            case STEP_HEIGHT -> 1D;
        };
    }

    @Override
    public ChatFormatting getChatFormatting()
    {
        return this.chatFormatting;
    }

    public int getColor()
    {
        return switch (this)
        {
            case CREATURE -> 6336304;
            case HUMAN -> 9437216;
            case VAMPIRE -> 6226059;
            default -> this.chatFormatting.getColor();
        };
    }
}
