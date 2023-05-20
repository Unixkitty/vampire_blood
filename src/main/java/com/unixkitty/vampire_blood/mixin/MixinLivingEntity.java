package com.unixkitty.vampire_blood.mixin;

import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity
{
    @Inject(at = @At("HEAD"), method = "getMobType()Lnet/minecraft/world/entity/MobType;", cancellable = true)
    public void vampire$getMobType(CallbackInfoReturnable<MobType> result)
    {
        if ((LivingEntity) (Object) this instanceof Player player && !player.getLevel().isClientSide() && VampireUtil.isUndead((ServerPlayer) player))
        {
            result.setReturnValue(MobType.UNDEAD);
        }
    }

    @Inject(at = @At("HEAD"), method = "canBreatheUnderwater()Z", cancellable = true)
    public void vampire$canBreatheUnderwater(CallbackInfoReturnable<Boolean> result)
    {
        if ((LivingEntity) (Object) this instanceof Player player && !player.getLevel().isClientSide())
        {
            //TODO does this conflict with enchantments or other external effects?
            result.setReturnValue(false);
        }
    }
}
