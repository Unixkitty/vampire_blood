package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.init.ModRegistry;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
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
        if (!event.getEntity().level.isClientSide() && event.getEntity() instanceof Player player && VampireUtil.isVampire(player) && event.getSource() != DamageSource.LAVA && Config.vampireDustDropAmount.get() > 0)
        {
            player.level.addFreshEntity(new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), new ItemStack(ModRegistry.VAMPIRE_DUST.get(), player.level.random.nextIntBetweenInclusive(1, Config.vampireDustDropAmount.get()))));
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
        if (!event.getEntity().getLevel().isClientSide() && event.getEntity() instanceof Player player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.NOT_VAMPIRE.getId())
                {
                    if (Config.increasedDamageFromWood.get() && !(event.getSource() instanceof IndirectEntityDamageSource) && event.getSource().getEntity() instanceof LivingEntity attacker && event.getAmount() > 0 && attacker.getMainHandItem().getItem() instanceof TieredItem item && item.getTier() == Tiers.WOOD)
                    {
                        event.setAmount(event.getAmount() * 1.25F);

                        vampirePlayerData.addPreventRegenTicks(60);
                    }

                    if (event.getSource().isFire())
                    {
                        event.setAmount(event.getAmount() > 0 ? event.getAmount() * 2 : event.getAmount());

                        vampirePlayerData.addPreventRegenTicks(20);
                    }
                }
            });
        }
    }
}
