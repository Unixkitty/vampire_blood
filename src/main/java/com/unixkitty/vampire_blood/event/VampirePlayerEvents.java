package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.VampireUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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

            if (VampireUtil.isUndead(player))
            {
                if (effect == MobEffects.HUNGER || effect == MobEffects.SATURATION || effect == MobEffects.FIRE_RESISTANCE)
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

    @SubscribeEvent
    public static void onLivingHurtEvent(final LivingHurtEvent event)
    {
        if (!event.getEntity().getLevel().isClientSide() && event.getEntity() instanceof Player player && VampireUtil.isVampire(player) && event.getSource().isFire())
        {
            event.setAmount(event.getAmount() > 0 ? event.getAmount() * 2 : event.getAmount());
        }
    }
}
