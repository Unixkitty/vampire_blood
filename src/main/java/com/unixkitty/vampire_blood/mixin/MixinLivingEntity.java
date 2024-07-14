package com.unixkitty.vampire_blood.mixin;

import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity
{
    @Unique
    private boolean vampire_blood$noImpactKnockback = false;

    @Inject(at = @At("HEAD"), method = "getMobType()Lnet/minecraft/world/entity/MobType;", cancellable = true)
    public void onGetMobType(CallbackInfoReturnable<MobType> result)
    {
        if ((LivingEntity) (Object) this instanceof Player player && !player.level().isClientSide() && VampireUtil.isUndead((ServerPlayer) player))
        {
            result.setReturnValue(MobType.UNDEAD);
        }
    }

    @Inject(at = @At("HEAD"), method = "canBreatheUnderwater()Z", cancellable = true)
    public void shouldBreatheUnderwater(CallbackInfoReturnable<Boolean> result)
    {
        if ((LivingEntity) (Object) this instanceof Player player && !player.level().isClientSide())
        {
            //TODO does this conflict with enchantments or other external effects?
            result.setReturnValue(false);
        }
    }

    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    public void onHurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir)
    {
        if (pSource.is(DamageTypeTags.NO_IMPACT))
        {
            this.vampire_blood$noImpactKnockback = true;
        }
    }

    @Inject(at = @At("HEAD"), method = "knockback(DDD)V", cancellable = true)
    public void onKnockback(double pStrength, double pX, double pZ, CallbackInfo ci)
    {
        if (this.vampire_blood$noImpactKnockback)
        {
            this.vampire_blood$noImpactKnockback = false;

            ci.cancel();
        }
    }
}
