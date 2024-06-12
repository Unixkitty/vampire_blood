package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampireAttributeModifier;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.effect.BasicStatusEffect;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public class VampirePlayerEvents
{
    @SubscribeEvent
    public static void onPlayerGetBreakSpeed(final PlayerEvent.BreakSpeed event)
    {
        AttributeInstance attributeInstance = event.getEntity().getAttribute(Attributes.ATTACK_SPEED);

        if (attributeInstance != null)
        {
            AttributeModifier modifier = attributeInstance.getModifier(VampireAttributeModifier.ATTACK_SPEED.getUUID());

            if (modifier != null)
            {
                event.setNewSpeed(event.getOriginalSpeed() * ((float) modifier.getAmount() * 2F));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onApplyMobEffect(final MobEffectEvent.Applicable event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player)
        {
            var effect = event.getEffectInstance().getEffect();

            final VampirismLevel vampirismLevel = player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(VampirePlayerData::getVampireLevel).orElse(VampirismLevel.NOT_VAMPIRE);

            if (vampirismLevel.getId() > VampirismLevel.NOT_VAMPIRE.getId())
            {
                if (effect == MobEffects.HUNGER || effect == MobEffects.SATURATION || (effect == MobEffects.FIRE_RESISTANCE && vampirismLevel != VampirismLevel.ORIGINAL) || effect == MobEffects.NIGHT_VISION || effect instanceof BasicStatusEffect)
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
    public static void onPlayerEquipmentChange(final LivingEquipmentChangeEvent event)
    {
        final ItemStack itemStack = event.getTo();

        if (event.getEntity() instanceof ServerPlayer player && !itemStack.isEmpty())
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismLevel.IN_TRANSITION.getId() && vampirePlayerData.isZooming())
                {
                    if ((event.getSlot() == EquipmentSlot.HEAD
                            || event.getSlot() == EquipmentSlot.CHEST
                            || event.getSlot() == EquipmentSlot.LEGS
                            || event.getSlot() == EquipmentSlot.FEET)
                            && VampireUtil.isArmour(itemStack))
                    {
                        vampirePlayerData.toggleAbility(player, VampireActiveAbility.SPEED);
                    }
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerCloned(final PlayerEvent.Clone event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player)
        {
            event.getOriginal().reviveCaps();

            VampirePlayerData.copyData(event.getOriginal(), player, event.isWasDeath());

            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(final LivingDeathEvent event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismLevel.IN_TRANSITION.getId())
                {
                    if (Config.vampireDustDropAmount.get() > 0 && !event.getSource().is(DamageTypes.LAVA))
                    {
                        player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), new ItemStack(ModItems.VAMPIRE_DUST.get(), player.level().random.nextIntBetweenInclusive(1, Config.vampireDustDropAmount.get()))));
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
    public static void onLivingDamaged(final LivingDamageEvent event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismLevel.IN_TRANSITION.getId())
                {
                    //TODO replace with knockout mechanic in the future
                    if (event.getAmount() >= player.getHealth() && event.getAmount() < player.getMaxHealth() / 5F && vampirePlayerData.getNoRegenTicks() <= 0)
                    {
                        event.setAmount(0);
                        player.setHealth(1F);

                        vampirePlayerData.decreaseBlood(2, false);

                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false, true));
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(final LivingHurtEvent event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismLevel.NOT_VAMPIRE.getId())
                {
                    if (event.getSource().getEntity() instanceof LivingEntity attacker && !event.getSource().isIndirect() && event.getAmount() > 0 && Config.increasedDamageFromWood.get() && attacker.getMainHandItem().getItem() instanceof TieredItem item && item.getTier() == Tiers.WOOD)
                    {
                        event.setAmount(event.getAmount() * 1.25F);

                        vampirePlayerData.addPreventRegenTicks(player, 60);
                    }
                    else if (event.getSource().is(DamageTypeTags.IS_FIRE))
                    {
                        if (vampirePlayerData.getVampireLevel() == VampirismLevel.ORIGINAL)
                        {
                            event.setAmount(event.getAmount() > 0 ? event.getAmount() * 0.5F : event.getAmount());
                        }
                        else
                        {
                            event.setAmount(event.getAmount() > 0 ? event.getAmount() * 2 : event.getAmount());

                            vampirePlayerData.addPreventRegenTicks(player, 20);
                        }
                    }

                    if (event.getAmount() > 0 && vampirePlayerData.isFeeding())
                    {
                        vampirePlayerData.tryStopFeeding(player, event.getAmount());
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(final LivingAttackEvent event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player)
        {
            boolean charmedByPlayer;

            LivingEntity attacker = event.getEntity();

            if (attacker instanceof ServerPlayer)
            {
                charmedByPlayer = attacker.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.isCharmedBy(player)).orElse(false);
            }
            else
            {
                charmedByPlayer = VampireUtil.isEntityCharmedBy(attacker, player);
            }

            if (charmedByPlayer && VampireUtil.isVampire(player))
            {
                event.setCanceled(true);
            }
        }
    }
}
