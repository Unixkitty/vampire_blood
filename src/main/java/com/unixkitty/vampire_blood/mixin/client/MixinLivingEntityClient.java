package com.unixkitty.vampire_blood.mixin.client;

import com.unixkitty.vampire_blood.client.VampiricSensesUtil;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntityClient
{
    @Inject(at = @At("HEAD"), method = "isCurrentlyGlowing()Z", cancellable = true)
    public void shouldEntityGlow(CallbackInfoReturnable<Boolean> result)
    {
        if (VampiricSensesUtil.shouldEntityGlow((LivingEntity) (Object) this))
        {
            result.setReturnValue(true);
        }
    }
}
