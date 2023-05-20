package com.unixkitty.vampire_blood.util;

import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class SunExposurer
{
    public static void chanceEffect(Player player, MobEffect effect, int durationMultiplier, int linearAmplifier, VampirismStage stage)
    {
        chanceEffect(player, effect, durationMultiplier, linearAmplifier, stage == VampirismStage.IN_TRANSITION ? effect == MobEffects.BLINDNESS ? 2 : 10 : 100);
    }

    public static void chanceEffect(Player player, MobEffect effect, int durationMultiplier, int linearAmplifier, int chance)
    {
        if (chance < 100 && player.getRandom().nextInt(101) > chance)
        {
            return;
        }

        player.addEffect(new MobEffectInstance(effect, Config.ticksToSunDamage.get() * durationMultiplier, linearAmplifier, false, false, true));
    }

    public static boolean isCatchingUV(Player player)
    {
        final BlockPos playerEyePos = new BlockPos(player.getX(), player.getEyeY(), player.getZ());

        //If fully submerged, including eyes, check if deep enough (4 blocks) or if the block above the liquid can't see the sky
        if (player.isUnderWater())
        {
            BlockPos abovePos;
            BlockPos blockPosAboveLiquid = null;

            for (int i = 0; i <= 4; i++)
            {
                abovePos = playerEyePos.above(i);

                if (!player.level.getBlockState(abovePos).getMaterial().isLiquid())
                {
                    blockPosAboveLiquid = abovePos;
                    break;
                }
            }

            //If shallow enough liquid and found nonwater above, can it see sky
            return blockPosAboveLiquid != null && player.level.canSeeSky(blockPosAboveLiquid);
        }
        else //If Player not in water and can see sky
        {
            return player.level.canSeeSky(playerEyePos);
        }
    }
}
