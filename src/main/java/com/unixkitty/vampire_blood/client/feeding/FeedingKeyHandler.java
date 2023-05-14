package com.unixkitty.vampire_blood.client.feeding;

import com.mojang.blaze3d.platform.InputConstants;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import com.unixkitty.vampire_blood.client.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
public class FeedingKeyHandler
{
    static void handle(final InputEvent.Key event)
    {
        final boolean feedKeyTouched = event.getKey() == KeyBindings.FEED_KEY.getKey().getValue();
        final boolean movementKeysTouched = Minecraft.getInstance().options.keyUp.isDown() || Minecraft.getInstance().options.keyDown.isDown() || Minecraft.getInstance().options.keyLeft.isDown() || Minecraft.getInstance().options.keyRight.isDown() || Minecraft.getInstance().options.keyJump.isDown() || Minecraft.getInstance().options.keyShift.isDown();

        //Just pressed the key, if not feeding need to request server to start feeding
        if (feedKeyTouched && event.getAction() == InputConstants.PRESS)
        {
            if (!ClientVampirePlayerDataCache.feeding && ClientVampirePlayerDataCache.canFeed() && FeedingMouseOverHandler.isLookingAtEdible())
            {
                FeedingHandler.startFeeding();
            }
        }
        //Just released the key or if any movement keys pressed and the key is not being held, if feeding need to request server to stop feeding
        else if ((feedKeyTouched && event.getAction() == InputConstants.RELEASE) || (movementKeysTouched))
        {
            //Do not stop feeding if holding the key
            if (!(feedKeyTouched && event.getAction() == InputConstants.REPEAT) && ClientVampirePlayerDataCache.feeding)
            {
                FeedingHandler.stopFeeding();
            }
        }

        //No movement during feeding
        if (ClientVampirePlayerDataCache.feeding && ClientVampirePlayerDataCache.canFeed() && FeedingMouseOverHandler.isLookingAtEdible() && movementKeysTouched)
        {
            Minecraft.getInstance().options.keyUp.setDown(false);
            Minecraft.getInstance().options.keyDown.setDown(false);
            Minecraft.getInstance().options.keyLeft.setDown(false);
            Minecraft.getInstance().options.keyRight.setDown(false);
            Minecraft.getInstance().options.keyJump.setDown(false);
            Minecraft.getInstance().options.keyShift.setDown(false);
        }
    }
}
