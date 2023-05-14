package com.unixkitty.vampire_blood.mixin;

import com.unixkitty.vampire_blood.client.feeding.FeedingHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity
{
    @Inject(at = @At("HEAD"), method = "turn(DD)V", cancellable = true)
    public void vampire$turn(double yRot, double xRot, CallbackInfo ci)
    {
        if ((Entity)(Object)this instanceof LocalPlayer && FeedingHandler.handleMouseTurn())
        {
            ci.cancel();
        }
    }
}
