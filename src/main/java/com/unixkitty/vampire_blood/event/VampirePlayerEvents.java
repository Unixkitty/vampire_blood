package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
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
    public static void onAttachCapabilityPlayer(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            if (!event.getObject().getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
            {
                event.addCapability(new ResourceLocation(VampireBlood.MODID, "vampirism"), new VampirePlayerProvider());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerCloned(final PlayerEvent.Clone event)
    {
        if (!event.getEntity().getLevel().isClientSide())
        {
            event.getOriginal().reviveCaps();

            VampirePlayerData.copyData(event.getOriginal(), event.getEntity(), event.isWasDeath());

            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event)
    {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END)
        {
            event.player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (!event.player.isCreative() && !event.player.isSpectator())
                {
                    vampirePlayerData.tick(event.player);
                }
            });
        }
    }
}
