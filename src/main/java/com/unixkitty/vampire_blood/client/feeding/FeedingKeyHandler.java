package com.unixkitty.vampire_blood.client.feeding;

import com.mojang.blaze3d.platform.InputConstants;
import com.unixkitty.vampire_blood.client.KeyAction;
import com.unixkitty.vampire_blood.client.KeyBindings;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
public class FeedingKeyHandler
{
    private static int lastFeedKeyAction = InputConstants.RELEASE;

    static void handle(final InputEvent.Key event, Minecraft minecraft)
    {
        final boolean feedKeyTouched = event.getKey() == KeyBindings.FEED_KEY.getKey().getValue();
        final boolean movementKeysTouched = minecraft.options.keyUp.isDown() || minecraft.options.keyDown.isDown() || minecraft.options.keyLeft.isDown() || minecraft.options.keyRight.isDown() || minecraft.options.keyJump.isDown() || minecraft.options.keyShift.isDown() || minecraft.options.keyAttack.isDown() || minecraft.options.keyUse.isDown();

        if (feedKeyTouched)
        {
            lastFeedKeyAction = event.getAction();
        }

        //Just pressed the key, if not feeding need to request server to start feeding
        if (feedKeyTouched && lastFeedKeyAction == InputConstants.PRESS)
        {
            if (!ClientCache.getVampireVars().feeding && FeedingHandler.canFeed())
            {
                KeyAction.FEED_START.handle();
            }
        }
        else if (ClientCache.getVampireVars().feeding)
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
            minecraft.options.keyUp.setDown(false);
            minecraft.options.keyDown.setDown(false);
            minecraft.options.keyLeft.setDown(false);
            minecraft.options.keyRight.setDown(false);
            minecraft.options.keyJump.setDown(false);
            minecraft.options.keyShift.setDown(false);
            minecraft.options.keyAttack.setDown(false);
            minecraft.options.keyUse.setDown(false);
        }
    }
}
