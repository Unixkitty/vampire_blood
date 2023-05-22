package com.unixkitty.vampire_blood.mixin;

import com.unixkitty.vampire_blood.client.VampiricSensesUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
    @Shadow @Nullable public LocalPlayer player;

    @Inject(at = @At(value = "HEAD"), method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
    public void shouldEntityGlow(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (VampiricSensesUtil.shouldEntityGlow(entity, this.player))
        {
            cir.setReturnValue(true);
        }
    }
}
