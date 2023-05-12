package com.unixkitty.vampire_blood.util;

import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import net.minecraft.network.chat.Component;

public interface VampirismTier
{
    int getId();

    double getAttributeMultiplier(VampireAttributeModifiers.Modifier modifier);

    float getBloodlustMultiplier(boolean bloodPointGained);

    int getColor();

    Component getTranslation();

    default Component getTranslation(String name)
    {
        return Component.translatable("vampire_blood." + name + "." + this.toString().toLowerCase());
    }

    static <E extends Enum<E> & VampirismTier> E fromId(Class<E> clazz, int id)
    {
        for (E stage : clazz.getEnumConstants())
        {
            if (stage.getId() == id) return stage;
        }

        return null;
    }
}
