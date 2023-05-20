package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.capability.player.effect.VampireNightVision;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public enum VampireActiveAbilities
{
    NIGHT_VISION,
    SPEED,
    SENSES,
    TRANSFIX,
    BLOOD_VISION;

    private static final String ABILITIES_NBT_NAME = "activeAbilities";

    public void refresh(Player player)
    {
        if (this == NIGHT_VISION)
        {
            if (player.level.isClientSide())
            {
                if (!(player.getEffect(MobEffects.NIGHT_VISION) instanceof VampireNightVision))
                {
                    player.addEffect(new VampireNightVision());
                }
            }
        }
    }

    public void stop(Player player)
    {
        if (player.level.isClientSide())
        {
            if (this == NIGHT_VISION)
            {
                if (player.getEffect(MobEffects.NIGHT_VISION) instanceof VampireNightVision)
                {
                    player.removeEffect(MobEffects.NIGHT_VISION);
                }
            }
        }
    }

    public String getSimpleName()
    {
        return this.name().toLowerCase();
    }

    public static VampireActiveAbilities fromOrdinal(int id)
    {
        for (VampireActiveAbilities ability : values())
        {
            if (ability.ordinal() == id) return ability;
        }

        return null;
    }

    public static void saveNBT(Set<VampireActiveAbilities> activeAbilities, CompoundTag tag)
    {
        CompoundTag subTag = new CompoundTag();

        for (VampireActiveAbilities ability : values())
        {
            subTag.putBoolean(ability.getSimpleName(), activeAbilities.contains(ability));
        }

        tag.put(ABILITIES_NBT_NAME, subTag);
    }

    public static void loadNBT(Set<VampireActiveAbilities> activeAbilities, CompoundTag tag)
    {
        CompoundTag subTag = tag.getCompound(ABILITIES_NBT_NAME);

        if (!subTag.isEmpty())
        {
            for (VampireActiveAbilities ability : values())
            {
                if (subTag.getBoolean(ability.getSimpleName()))
                {
                    activeAbilities.add(ability);
                }
            }
        }
    }
}
