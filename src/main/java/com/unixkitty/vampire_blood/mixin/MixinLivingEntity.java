package com.unixkitty.vampire_blood.mixin;

import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity
{
    @Inject(at = @At("HEAD"), method = "isInvertedHealAndHarm()Z", cancellable = true)
    public void vampire$isInvertedHealAndHarm(CallbackInfoReturnable<Boolean> cir)
    {
        if ((LivingEntity)(Object)this instanceof Player player && !player.getLevel().isClientSide() && player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent() && player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel() != VampirePlayerData.Stage.NOT_VAMPIRE).orElse(false))
        {
            cir.setReturnValue(true);
        }
    }
}
