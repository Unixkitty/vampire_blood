package com.unixkitty.vampire_blood.capability.player;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.network.chat.Component;

public interface VampirismTier<E extends Enum<E> & VampirismTier<E>>
{
    int getId();

    double getAttributeMultiplier(VampireAttributeModifier modifier);

    float getBloodlustMultiplier(boolean bloodPointGained);

    @SuppressWarnings("unchecked")
    default Component getTranslation()
    {
        return Component.translatable(VampireBlood.MODID + "." + getName((Class<? extends VampirismTier<?>>) this.getClass()) + "." + VampireUtil.getEnumName((E) this));
    }

    static String getName(Class<? extends VampirismTier<?>> tierClass)
    {
        if (tierClass.equals(VampirismLevel.class))
        {
            return "level";
        }
        else if (tierClass.equals(BloodType.class))
        {
            return "blood_type";
        }
        else
        {
            return "vampirism_tier";
        }
    }

    static <E extends Enum<E> & VampirismTier<E>> E fromId(Class<E> clazz, int id)
    {
        for (E stage : clazz.getEnumConstants())
        {
            if (stage.getId() == id) return stage;
        }

        return null;
    }
}
