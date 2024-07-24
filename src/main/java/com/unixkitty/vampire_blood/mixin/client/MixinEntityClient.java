package com.unixkitty.vampire_blood.mixin.client;

import com.unixkitty.vampire_blood.client.VampiricSensesUtil;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntityClient
{
    @Inject(at = @At("HEAD"), method = "getTeamColor()I", cancellable = true)
    public void getTeamColor(CallbackInfoReturnable<Integer> cir)
    {
        int color = VampiricSensesUtil.getEntityGlowColor((Entity) (Object) this);

        if (color != -1)
        {
            cir.setReturnValue(color);
        }
    }
}
