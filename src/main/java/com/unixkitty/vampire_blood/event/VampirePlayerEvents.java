package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public class VampirePlayerEvents
{
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onApplyMobEffect(final MobEffectEvent.Applicable event)
    {
        if (!event.getEntity().getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player)
        {
            var effect = event.getEffectInstance().getEffect();

            if (player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.getVampireLevel() != VampirePlayerData.Stage.NOT_VAMPIRE).orElse(false))
            {
                if (effect == MobEffects.HUNGER || effect == MobEffects.SATURATION || effect == MobEffects.FIRE_RESISTANCE || effect == MobEffects.POISON || effect == MobEffects.REGENERATION)
                {
                    event.setResult(Event.Result.DENY);
                }
                else if (effect.getCategory() == MobEffectCategory.HARMFUL && event.getEffectInstance().getDuration() >= 2)
                {
                    event.getEffectInstance().duration = event.getEffectInstance().getDuration() / 2;
                }
            }
        }
    }
}
