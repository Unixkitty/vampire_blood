package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.effect.BasicStatusEffect;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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
    @SubscribeEvent
    public static void onPlayerGetBreakSpeed(final PlayerEvent.BreakSpeed event)
    {
        AttributeInstance attributeInstance = event.getEntity().getAttribute(Attributes.ATTACK_SPEED);

        if (attributeInstance != null)
        {
            AttributeModifier modifier = attributeInstance.getModifier(VampireAttributeModifiers.Modifier.ATTACK_SPEED.getUUID());

            if (modifier != null)
            {
                event.setNewSpeed(event.getOriginalSpeed() * ((float) modifier.getAmount() * 2F));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onApplyMobEffect(final MobEffectEvent.Applicable event)
    {
        if (!event.getEntity().getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player)
        {
            var effect = event.getEffectInstance().getEffect();

            if (VampireUtil.isUndead(player))
            {
                if (effect == MobEffects.HUNGER || effect == MobEffects.SATURATION || effect == MobEffects.FIRE_RESISTANCE || effect == MobEffects.NIGHT_VISION || effect instanceof BasicStatusEffect)
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerCloned(final PlayerEvent.Clone event)
    {
        if (!event.getEntity().getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player)
        {
            event.getOriginal().reviveCaps();

            VampirePlayerData.copyData(event.getOriginal(), player, event.isWasDeath());

            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(final LivingDeathEvent event)
    {
        if (!event.getEntity().level.isClientSide() && event.getEntity() instanceof Player player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.IN_TRANSITION.getId())
                {
                    if (Config.vampireDustDropAmount.get() > 0 && event.getSource() != DamageSource.LAVA)
                    {
                        player.level.addFreshEntity(new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), new ItemStack(ModItems.VAMPIRE_DUST.get(), player.level.random.nextIntBetweenInclusive(1, Config.vampireDustDropAmount.get()))));
                    }

                    //TODO replace with knockout mechanic in the future
                    if (vampirePlayerData.getNoRegenTicks() <= 0)
                    {
                        player.setHealth(1);
                        event.setCanceled(true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event)
    {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (!player.isCreative() && !event.player.isSpectator())
                {
                    vampirePlayerData.tick(player);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(final LivingHurtEvent event)
    {
        if (!event.getEntity().getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.NOT_VAMPIRE.getId())
                {
                    if (event.getSource().getEntity() instanceof LivingEntity attacker && !(event.getSource() instanceof IndirectEntityDamageSource) && event.getAmount() > 0 && Config.increasedDamageFromWood.get() && attacker.getMainHandItem().getItem() instanceof TieredItem item && item.getTier() == Tiers.WOOD)
                    {
                        event.setAmount(event.getAmount() * 1.25F);

                        vampirePlayerData.addPreventRegenTicks(player, 60);
                    }
                    else if (event.getSource().isFire())
                    {
                        event.setAmount(event.getAmount() > 0 ? event.getAmount() * 2 : event.getAmount());

                        vampirePlayerData.addPreventRegenTicks(player, 20);
                    }

                    if (event.getAmount() > 0 && vampirePlayerData.isFeeding())
                    {
                        vampirePlayerData.tryStopFeeding(player, event.getAmount());
                    }
                }
            });
        }
    }
}
