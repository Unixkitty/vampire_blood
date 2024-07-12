package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.blood.IBloodVessel;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampireAttributeModifier;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.effect.BasicStatusEffect;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.item.BloodKnifeItem;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
    public static void onPlayerInteract(final PlayerInteractEvent.EntityInteract event)
    {
        if (event.getHand() == InteractionHand.OFF_HAND)
        {
            return;
        }

        Player player = event.getEntity();
        ServerPlayer serverPlayer = player instanceof ServerPlayer ? (ServerPlayer) player : null;
        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack offhandStack = player.getOffhandItem();

        if (event.getTarget() instanceof LivingEntity targetEntity && targetEntity.isAlive() && VampireUtil.canReachEntity(player, targetEntity))
        {
            if (mainHandStack.getItem() == ModItems.BLOODLETTING_KNIFE.get() && (offhandStack.getItem() == Items.GLASS_BOTTLE || offhandStack.getItem() == Items.BUCKET))
            {
                if (serverPlayer != null)
                {
                    CompoundTag tag = mainHandStack.getOrCreateTag();

                    tag.putInt(BloodKnifeItem.VICTIM_NBT_NAME, targetEntity.getId());
                    tag.putBoolean(BloodKnifeItem.USING_BUCKET_NBT_NAME, offhandStack.getItem() == Items.BUCKET);
                    serverPlayer.sendSystemMessage(Component.translatable("text.vampire_blood.knife_usage_entity", targetEntity.getDisplayName()).withStyle(ChatFormatting.DARK_RED), true);
                }

                event.setCanceled(true);
            }
            else if (mainHandStack.isEdible() && !targetEntity.isSleeping() && !targetEntity.isFullyFrozen())
            {
                IBloodVessel bloodVessel = VampireUtil.getEntityBloodVessel(targetEntity);
                BloodType bloodType = bloodVessel.getBloodType();

                boolean canFeed = false;

                if ((bloodType == BloodType.HUMAN || bloodType == BloodType.PIGLIN) && bloodVessel.hasNoFoodItemCooldown())
                {
                    canFeed = targetEntity.getHealth() < targetEntity.getMaxHealth() || (targetEntity instanceof ServerPlayer targetPlayer && targetPlayer.getFoodData().needsFood());

                    if (canFeed && Config.onlyFeedCharmedHumanoids.get())
                    {
                        canFeed = serverPlayer == null || bloodVessel.isCharmedBy(serverPlayer);
                    }
                }

                if (canFeed)
                {
                    if (serverPlayer != null)
                    {
                        if ((targetEntity instanceof Piglin && mainHandStack.is(ItemTags.PIGLIN_FOOD)) || !(targetEntity instanceof Player))
                        {
                            FoodProperties foodProperties = mainHandStack.getFoodProperties(null);

                            if (foodProperties != null)
                            {
                                float amount = foodProperties.getNutrition() * foodProperties.getSaturationModifier();

                                if (targetEntity.isInvertedHealAndHarm())
                                {
                                    targetEntity.heal(amount);
                                }
                                else
                                {
                                    VampireUtil.applyEffect(targetEntity, MobEffects.REGENERATION, (int) (amount * 50), 0);
                                }
                            }
                        }

                        //This is called anyway because it only applies food effects to non-players
                        targetEntity.eat(targetEntity.level(), mainHandStack);

                        bloodVessel.addFoodItemCooldown(targetEntity, mainHandStack);

                        if (targetEntity instanceof Piglin piglin)
                        {
                            piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, bloodVessel.getFoodItemCooldown());
                        }
                    }

                    event.setCanceled(true);
                }
                else if (serverPlayer != null && targetEntity instanceof Villager villager)
                {
                    villager.setUnhappy();
                    event.setCanceled(true);
                }
            }
        }
    }

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
                if (effect == MobEffects.HUNGER
                        || effect == MobEffects.SATURATION
                        || effect == MobEffects.POISON
                        || effect == MobEffects.NIGHT_VISION
                        || effect instanceof BasicStatusEffect
                        || (effect == MobEffects.FIRE_RESISTANCE && vampirismLevel != VampirismLevel.ORIGINAL)
                )
                {
                    event.setResult(Event.Result.DENY);
                }
                else if (event.getEffectInstance().getDuration() >= 2)
                {
                    event.getEffectInstance().duration = event.getEffectInstance().getDuration() / 2;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(final LivingEquipmentChangeEvent event)
    {
        final ItemStack itemStack = event.getTo();

        if (event.getEntity() instanceof ServerPlayer player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() >= VampirismLevel.IN_TRANSITION.getId())
                {
                    vampirePlayerData.updateSunCoverage(player);

                    if (vampirePlayerData.isZooming() && VampireUtil.isArmour(itemStack))
                    {
                        vampirePlayerData.toggleAbility(player, VampireActiveAbility.SPEED);

                        player.sendSystemMessage(Component.translatable("text.vampire_blood.speed_in_armour").withStyle(ChatFormatting.RED), true);
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
                else if (vampirePlayerData.getVampireLevel() == VampirismLevel.NOT_VAMPIRE && player.hasEffect(ModEffects.VAMPIRE_BLOOD.get()))
                {
                    vampirePlayerData.setShouldTransition();
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
                    //TODO replace with knockout mechanic in the future?
                    if (event.getAmount() >= player.getHealth() && event.getAmount() < player.getMaxHealth() / 5F && vampirePlayerData.getNoRegenTicks() <= 0)
                    {
                        event.setAmount(0);
                        player.setHealth(1F);

                        vampirePlayerData.decreaseBlood(2, false);

                        VampireUtil.applyEffect(player, MobEffects.WEAKNESS, 1200, 0);
                        VampireUtil.applyEffect(player, MobEffects.DIG_SLOWDOWN, 1200, 0);
                        VampireUtil.applyEffect(player, MobEffects.MOVEMENT_SLOWDOWN, 1200, 0);
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
                VampirismLevel vampirismLevel = vampirePlayerData.getVampireLevel();

                if (vampirismLevel.getId() > VampirismLevel.NOT_VAMPIRE.getId())
                {
                    if (event.getSource().getEntity() instanceof LivingEntity attacker && !event.getSource().isIndirect() && event.getAmount() > 0 && Config.increasedDamageFromWood.get() && attacker.getMainHandItem().getItem() instanceof TieredItem item && item.getTier() == Tiers.WOOD && vampirismLevel != VampirismLevel.ORIGINAL)
                    {
                        event.setAmount(event.getAmount() * 2F);

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
                    else if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))
                    {
                        vampirePlayerData.addPreventRegenTicks(player, Config.noRegenTicksLimit.get());
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
            boolean charmedByPlayer = false;

            Entity attacker = event.getSource().getEntity();

            if (attacker instanceof ServerPlayer)
            {
                charmedByPlayer = attacker.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(vampirePlayerData -> vampirePlayerData.isCharmedBy(player)).orElse(false);
            }
            else if (attacker instanceof LivingEntity livingEntity)
            {
                charmedByPlayer = VampireUtil.isEntityCharmedBy(livingEntity, player);
            }

            if (charmedByPlayer && VampireUtil.isVampire(player))
            {
                event.setCanceled(true);
            }
        }
    }
}
