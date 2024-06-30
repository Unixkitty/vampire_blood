package com.unixkitty.vampire_blood.mixin;

import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Monster.class)
public class MixinMonster
{
    @Inject(at = @At("RETURN"), method = "isPreventingPlayerRest(Lnet/minecraft/world/entity/player/Player;)Z", cancellable = true)
    public void onPreventRestCheck(Player player, CallbackInfoReturnable<Boolean> cir)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            //Should not prevent rest if monster is charmed or has the noAttackUndeadPlayer criteria match
            cir.setReturnValue(!VampireUtil.isEntityCharmedBy((Monster) (Object) this, serverPlayer) && (serverPlayer.getMobType() != MobType.UNDEAD || ((Monster) (Object) this).getMobType() != MobType.UNDEAD || !Config.shouldUndeadIgnoreVampires.get()) && cir.getReturnValue());
        }
    }
}
