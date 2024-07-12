package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.capability.player.VampireAttributeModifier;
import com.unixkitty.vampire_blood.capability.player.VampirismTier;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

import java.util.function.Supplier;

public enum BloodType implements VampirismTier<BloodType>
{
    NONE(0, 0, 0, 0, 0),
    FRAIL(1, 0.5, 0.5, 0, 0.33F),
    CREATURE(2, 0.75, 0.75, 0.25, 1F),
    HUMAN(3, 1, 1, 1, 2F),
    VAMPIRE(4, 0.85, 0.85, 1.25, 1F),
    PIGLIN(5, 0.85, 0.85, 0.75, 1.5F);

    public static final String BLOODTYPE_NBT_NAME = "bloodType";

    private final int id;
    private final double healthMultiplier;
    private final double strengthMultiplier;
    private final double speedBoostModifier;
    private final float bloodSaturationModifier;
    private final Supplier<Rarity> itemRarity; //For unknown reasons the game crashes on startup with missing "forge:step_addition" attribute if this is not a supplier?

    BloodType(int id, double healthMultiplier, double strengthMultiplier, double speedBoostModifier, float bloodSaturationModifier)
    {
        this.id = id;
        this.healthMultiplier = healthMultiplier;
        this.strengthMultiplier = strengthMultiplier;
        this.speedBoostModifier = speedBoostModifier;
        this.bloodSaturationModifier = bloodSaturationModifier;
        this.itemRarity = () -> Rarity.create(this.name() + "_blood", switch (this)
        {
            case NONE -> ChatFormatting.BLACK;
            case FRAIL -> ChatFormatting.GRAY;
            case CREATURE -> ChatFormatting.GREEN;
            case HUMAN -> ChatFormatting.DARK_RED;
            case VAMPIRE -> ChatFormatting.DARK_PURPLE;
            case PIGLIN -> ChatFormatting.GOLD;
        });
    }

    public Rarity getItemRarity()
    {
        return this.itemRarity.get();
    }

    public float getBloodSaturationModifier()
    {
        return bloodSaturationModifier;
    }

    @Override
    public int getId()
    {
        return id;
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
            case HEALTH -> this.healthMultiplier;
            case STRENGTH -> this.strengthMultiplier;
            case BASE_SPEED, ATTACK_SPEED -> this.speedBoostModifier;
            case STEP_HEIGHT -> this == FRAIL ? 0D : 1D;
        };
    }
}
