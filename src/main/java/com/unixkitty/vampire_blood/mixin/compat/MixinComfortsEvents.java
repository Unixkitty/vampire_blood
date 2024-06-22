package com.unixkitty.vampire_blood.mixin.compat;

import com.illusivesoulworks.comforts.common.ComfortsConfig;
import com.illusivesoulworks.comforts.common.ComfortsEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Pseudo
@Mixin(ComfortsEvents.class)
public class MixinComfortsEvents
{
    @Inject(method = "checkTime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lcom/illusivesoulworks/comforts/common/ComfortsEvents$Result;", at = @At(value = "RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void injected(Level level, BlockPos pos, CallbackInfoReturnable<ComfortsEvents.Result> cir, long time, ComfortsConfig.ComfortsTimeUse timeUse, Block block)
    {
        final long l = level.getDayTime() % 24000L;

        if (!(l >= 12542 && l <= 23460) && (timeUse == ComfortsConfig.ComfortsTimeUse.DAY || timeUse == ComfortsConfig.ComfortsTimeUse.DAY_OR_NIGHT))
        {
            cir.setReturnValue(ComfortsEvents.Result.ALLOW);
        }
    }

    @Inject(method = "getWakeTime(Lnet/minecraft/server/level/ServerLevel;JJ)J", at = @At(value = "RETURN", ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void injected(ServerLevel level, long currentTime, long newTime, CallbackInfoReturnable<Long> cir, boolean[] daySleeping)
    {
        if (daySleeping[0] && level.isDay())
        {
            cir.setReturnValue(newTime + (level.getDayTime() % 24000L > 12000L ? 13000 : -11000));
        }
    }
}
