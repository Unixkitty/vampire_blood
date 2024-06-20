package com.unixkitty.vampire_blood.item;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BloodBottleItem extends Item
{
    private final BloodType bloodType;

    public BloodBottleItem(BloodType bloodType)
    {
        super(new Properties().rarity(Rarity.create(bloodType.name() + "_blood", bloodType.getChatFormatting())).stacksTo(1).craftRemainder(Items.GLASS_BOTTLE));

        this.bloodType = bloodType;
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack itemStack, @Nonnull Level level, @Nonnull LivingEntity livingEntity)
    {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;

        if (player instanceof ServerPlayer serverPlayer)
        {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);

            serverPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                switch (vampirePlayerData.getVampireLevel())
                {
                    case NOT_VAMPIRE ->
                    {
                        //TODO special handling
//                        if (this.bloodType == BloodType.VAMPIRE)
//                        {
//                        }
//                        else
//                        {
                        serverPlayer.eat(level, new ItemStack(Items.ROTTEN_FLESH));
//                        }
                    }
                    case IN_TRANSITION ->
                    {
                        //TODO special handling
                    }
                    default ->
                            vampirePlayerData.addBlood(serverPlayer, Config.bloodPointsFromBottles.get(), this.bloodType);
                }
            });
        }

        if (player != null)
        {
            player.awardStat(Stats.ITEM_USED.get(this));

            if (!player.getAbilities().instabuild)
            {
                itemStack.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild)
        {
            if (itemStack.isEmpty())
            {
                return new ItemStack(Items.GLASS_BOTTLE);
            }

            if (player != null)
            {
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        livingEntity.gameEvent(GameEvent.DRINK);

        return itemStack;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack pStack, @Nullable Level pLevel, @Nonnull List<Component> pTooltipComponents, @Nonnull TooltipFlag pIsAdvanced)
    {
        pTooltipComponents.add(Component.translatable("text.vampire_blood.blood_bottle_points", Config.bloodPointsFromBottles.get()).withStyle(ChatFormatting.RED));

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack pStack)
    {
        return Items.POTION.getUseDuration(pStack) * 2;
    }

    @Nonnull
    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack pStack)
    {
        return UseAnim.DRINK;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level pLevel, @Nonnull Player pPlayer, InteractionHand pHand)
    {
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
    }

    public static ItemStack createItemStack(BloodType bloodType)
    {
        return new ItemStack(switch (bloodType)
        {
            case NONE -> Items.AIR;
            case FRAIL -> ModItems.FRAIL_BLOOD_BOTTLE.get();
            case CREATURE -> ModItems.CREATURE_BLOOD_BOTTLE.get();
            case HUMAN -> ModItems.HUMAN_BLOOD_BOTTLE.get();
            case VAMPIRE -> ModItems.VAMPIRE_BLOOD_BOTTLE.get();
            case PIGLIN -> ModItems.PIGLIN_BLOOD_BOTTLE.get();
        });
    }
}
