package com.unixkitty.vampire_blood.item;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nonnull;
import java.util.List;

public interface IBloodVesselItem
{
    BloodType getBloodType();

    int getBloodValue();

    ItemStack getEmptyVesselItem();

    default ItemStack consumeStoredBlood(@Nonnull ItemStack itemStack, @Nonnull Level level, @Nonnull LivingEntity livingEntity)
    {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;
        Item item = itemStack.getItem();

        if (player instanceof ServerPlayer serverPlayer)
        {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(item));

            serverPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                BloodType bloodType = this.getBloodType();

                if (vampirePlayerData.getVampireLevel() == VampirismLevel.NOT_VAMPIRE)
                {
                    serverPlayer.eat(level, new ItemStack(Items.ROTTEN_FLESH));

                    if (bloodType == BloodType.VAMPIRE)
                    {
                        int duration = Config.vampireBloodEffectDuration.get();

                        VampireUtil.applyEffect(serverPlayer, ModEffects.VAMPIRE_BLOOD.get(), this.getBloodValue() * 2400, 0);
                        VampireUtil.applyEffect(serverPlayer, MobEffects.REGENERATION, duration / 2, 0);
                        VampireUtil.applyEffect(serverPlayer, MobEffects.DAMAGE_RESISTANCE, duration / 4, 0);
                        VampireUtil.applyEffect(serverPlayer, MobEffects.SATURATION, duration / 10, 0);
                        serverPlayer.removeEffect(MobEffects.POISON);
                    }
                    else
                    {
                        VampireUtil.chanceEffect(serverPlayer, MobEffects.POISON, 400, 0, bloodType == BloodType.FRAIL ? 100 : 35);
                    }
                }
                else
                {
                    if (vampirePlayerData.getVampireLevel() == VampirismLevel.IN_TRANSITION && bloodType == BloodType.HUMAN)
                    {
                        vampirePlayerData.updateLevel(serverPlayer, VampirismLevel.FLEDGLING, true);
                    }

                    vampirePlayerData.addBlood(serverPlayer, this.getBloodValue(), bloodType);
                }
            });
        }

        if (player != null)
        {
            player.awardStat(Stats.ITEM_USED.get(item));

            if (!player.getAbilities().instabuild)
            {
                itemStack.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild)
        {
            if (itemStack.isEmpty())
            {
                return this.getEmptyVesselItem();
            }

            if (player != null)
            {
                ItemStack bottleStack = this.getEmptyVesselItem();

                if (!player.getInventory().add(bottleStack))
                {
                    player.drop(bottleStack, false);
                }
            }
        }

        livingEntity.gameEvent(GameEvent.DRINK);

        return itemStack;
    }

    default void appendHoverText(@Nonnull List<Component> pTooltipComponents)
    {
        pTooltipComponents.add(Component.translatable("text.vampire_blood.blood_bottle_points", this.getBloodValue()).withStyle(ChatFormatting.RED));
    }
}
