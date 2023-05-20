package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.effect.FakeEffectInstance;
import com.unixkitty.vampire_blood.init.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public enum VampireActiveAbility
{
    NIGHT_VISION(MobEffects.NIGHT_VISION),
    SPEED(ModEffects.ENHANCED_SPEED.get()),
    SENSES(ModEffects.ENHANCED_SENSES.get()),
    BLOOD_VISION(ModEffects.BLOOD_VISION.get());

    private static final String ABILITIES_NBT_NAME = "activeAbilities";

    private final MobEffect effect;

    VampireActiveAbility(MobEffect effect)
    {
        this.effect = effect;
    }

    public void refresh(Player player)
    {
        if (player.level.isClientSide())
        {
            switch (this)
            {
                case NIGHT_VISION ->
                {
                    if (!(player.getEffect(this.effect) instanceof FakeEffectInstance))
                    {
                        player.forceAddEffect(new FakeEffectInstance(this.effect), null);
                    }
                }
                case BLOOD_VISION, SENSES, SPEED ->
                {
                    if (!player.hasEffect(this.effect))
                    {
                        player.forceAddEffect(new FakeEffectInstance(this.effect), null);
                    }
                }
            }
        }
    }

    public void stop(Player player)
    {
        if (player.level.isClientSide())
        {
            if (player.getEffect(this.effect) instanceof FakeEffectInstance)
            {
                player.removeEffect(this.effect);
            }
        }
    }

    public String getSimpleName()
    {
        return this.name().toLowerCase();
    }

    public static VampireActiveAbility fromOrdinal(int id)
    {
        for (VampireActiveAbility ability : values())
        {
            if (ability.ordinal() == id) return ability;
        }

        return null;
    }

    public static void saveNBT(Set<VampireActiveAbility> activeAbilities, CompoundTag tag)
    {
        CompoundTag subTag = new CompoundTag();

        for (VampireActiveAbility ability : values())
        {
            subTag.putBoolean(ability.getSimpleName(), activeAbilities.contains(ability));
        }

        tag.put(ABILITIES_NBT_NAME, subTag);
    }

    public static void loadNBT(Set<VampireActiveAbility> activeAbilities, CompoundTag tag)
    {
        CompoundTag subTag = tag.getCompound(ABILITIES_NBT_NAME);

        if (!subTag.isEmpty())
        {
            for (VampireActiveAbility ability : values())
            {
                if (subTag.getBoolean(ability.getSimpleName()))
                {
                    activeAbilities.add(ability);
                }
            }
        }
    }
}
