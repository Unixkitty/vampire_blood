package com.unixkitty.vampire_blood.client.feeding;

import com.mojang.blaze3d.platform.InputConstants;
import com.unixkitty.vampire_blood.client.KeyAction;
import com.unixkitty.vampire_blood.client.KeyBindings;
import com.unixkitty.vampire_blood.client.cache.ClientVampirePlayerDataCache;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
public class FeedingKeyHandler
{
    private static int lastFeedKeyAction = InputConstants.RELEASE;

    static void handle(final InputEvent.Key event)
    {
        final boolean feedKeyTouched = event.getKey() == KeyBindings.FEED_KEY.getKey().getValue();
        final boolean movementKeysTouched = Minecraft.getInstance().options.keyUp.isDown() || Minecraft.getInstance().options.keyDown.isDown() || Minecraft.getInstance().options.keyLeft.isDown() || Minecraft.getInstance().options.keyRight.isDown() || Minecraft.getInstance().options.keyJump.isDown() || Minecraft.getInstance().options.keyShift.isDown() || Minecraft.getInstance().options.keyAttack.isDown() || Minecraft.getInstance().options.keyUse.isDown();

        if (feedKeyTouched)
        {
            lastFeedKeyAction = event.getAction();
        }

        //Just pressed the key, if not feeding need to request server to start feeding
        if (feedKeyTouched && lastFeedKeyAction == InputConstants.PRESS)
        {
            if (!ClientVampirePlayerDataCache.feeding && FeedingHandler.canFeed())
            {
                KeyAction.FEED_START.handle();
            }
        }
        else if (ClientVampirePlayerDataCache.feeding)
        {
            //Do not stop feeding if holding the key
            if (lastFeedKeyAction != InputConstants.REPEAT && (movementKeysTouched || lastFeedKeyAction == InputConstants.RELEASE))
            {
                KeyAction.FEED_STOP.handle();
            }
        }

        //No movement during feeding
        if (FeedingHandler.isFeeding() && movementKeysTouched)
        {
            Minecraft.getInstance().options.keyUp.setDown(false);
            Minecraft.getInstance().options.keyDown.setDown(false);
            Minecraft.getInstance().options.keyLeft.setDown(false);
            Minecraft.getInstance().options.keyRight.setDown(false);
            Minecraft.getInstance().options.keyJump.setDown(false);
            Minecraft.getInstance().options.keyShift.setDown(false);
            Minecraft.getInstance().options.keyAttack.setDown(false);
            Minecraft.getInstance().options.keyUse.setDown(false);
        }
    }
}
