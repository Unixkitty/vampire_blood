package com.unixkitty.vampire_blood.mixin.client.compat;

import com.unixkitty.vampire_blood.client.cache.ClientCache;
import it.hurts.sskirillss.relics.effects.VanishingEffect;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(VanishingEffect.ClientEvents.class)
public class MixinVanishingEffectClientEvents
{
    @Inject(method = "onEntityRender(Lnet/minecraftforge/client/event/RenderLivingEvent$Pre;)V", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/event/RenderLivingEvent$Pre;setCanceled(Z)V"), cancellable = true, remap = false)
    private static void onVanishEffect(RenderLivingEvent.Pre<?, ?> event, CallbackInfo ci)
    {
        if (ClientCache.isVampire())
        {
            ci.cancel();
        }
    }
}
