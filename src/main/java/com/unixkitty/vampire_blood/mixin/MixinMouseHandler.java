package com.unixkitty.vampire_blood.mixin;

import com.unixkitty.vampire_blood.client.feeding.FeedingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler
{
    @Inject(method = "turnPlayer()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), cancellable = true)
    public void beforeMoveMouse(CallbackInfo ci)
    {
        if (Minecraft.getInstance().mouseHandler.isMouseGrabbed() && Minecraft.getInstance().isWindowActive() && FeedingHandler.isFeeding())
        {
            ci.cancel();
        }
    }
}
