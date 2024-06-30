package com.unixkitty.vampire_blood.util;

import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class SunExposurer
{
    public static void chanceEffect(Player player, MobEffect effect, int durationMultiplier, int linearAmplifier, VampirismLevel stage)
    {
        VampireUtil.chanceEffect(player, effect, Config.ticksToSunDamage.get() * durationMultiplier, linearAmplifier, stage == VampirismLevel.IN_TRANSITION ? effect == MobEffects.BLINDNESS ? 2 : 10 : 100);
    }

    public static void applyEffect(Player player, MobEffect effect, int durationMultiplier, int linearAmplifier)
    {
        VampireUtil.applyEffect(player, effect, Config.ticksToSunDamage.get() * durationMultiplier, linearAmplifier);
    }

    public static boolean isCatchingUV(Player player)
    {
        if (player.level().skyDarken >= 4) return false;

        final BlockPos playerEyePos = new BlockPos(Mth.floor(player.getX()), Mth.floor(player.getEyeY()), Mth.floor(player.getZ()));

        //If fully submerged, including eyes, check if deep enough (4 blocks) or if the block above the liquid can't see the sky
        if (player.isUnderWater())
        {
            BlockPos abovePos;
            BlockPos blockPosAboveLiquid = null;

            for (int i = 0; i <= 4; i++)
            {
                abovePos = playerEyePos.above(i);

                if (!player.level().getBlockState(abovePos).liquid())
                {
                    blockPosAboveLiquid = abovePos;
                    break;
                }
            }

            //If shallow enough liquid and found nonwater above, can it see sky
            return blockPosAboveLiquid != null && player.level().canSeeSky(blockPosAboveLiquid);
        }
        else //If Player not in water and can see sky
        {
            return player.level().canSeeSky(playerEyePos);
        }
    }
}
