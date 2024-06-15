package com.unixkitty.vampire_blood.capability.player;

import net.minecraft.ChatFormatting;

public enum VampirismLevel implements VampirismTier<VampirismLevel>
{
    NOT_VAMPIRE(-1, 1, 1, 1, 0, ChatFormatting.WHITE),
    IN_TRANSITION(0, 1.5, 1.5, 1.5, 0, ChatFormatting.GRAY),
    FLEDGLING(1, 2.5, 2.5, 2, 1.25F, ChatFormatting.RED),
    VAMPIRE(2, 4, 4, 3, 1.0F, ChatFormatting.LIGHT_PURPLE),
    MATURE(3, 5, 5, 4, 0.75F, ChatFormatting.DARK_PURPLE),
    ORIGINAL(999, 5, 6, 5, 0.5F, ChatFormatting.DARK_RED);

    private final int id;
    private final double healthMultiplier;
    private final double strengthMultiplier;
    private final double speedBoostMultiplier;
    private final float bloodUsageMultiplier;
    private final ChatFormatting chatFormatting;

    VampirismLevel(int id, double healthMultiplier, double strengthMultiplier, double speedBoostMultiplier, float bloodUsageMultiplier, ChatFormatting formatting)
    {
        this.id = id;
        this.healthMultiplier = healthMultiplier;
        this.strengthMultiplier = strengthMultiplier;
        this.speedBoostMultiplier = speedBoostMultiplier;
        this.bloodUsageMultiplier = bloodUsageMultiplier;
        this.chatFormatting = formatting;
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
            case HEALTH -> healthMultiplier;
            case STRENGTH -> strengthMultiplier;
            case BASE_SPEED, ATTACK_SPEED -> speedBoostMultiplier;
        };
    }

    @Override
    public ChatFormatting getChatFormatting()
    {
        return this.chatFormatting;
    }
}
