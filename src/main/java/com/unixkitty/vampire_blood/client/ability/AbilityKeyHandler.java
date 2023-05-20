package com.unixkitty.vampire_blood.client.ability;

import com.unixkitty.vampire_blood.client.KeyAction;
import com.unixkitty.vampire_blood.client.KeyBindings;
import com.unixkitty.vampire_blood.client.cache.ClientVampirePlayerDataCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
public class AbilityKeyHandler
{
    public static void handleKeys(final InputEvent.Key event)
    {
        if (KeyBindings.NIGHT_VISION_KEY.consumeClick() && ClientVampirePlayerDataCache.isVampire())
        {
            KeyAction.NIGHT_VISION_TOGGLE.handle();
        }
    }
}
