package com.unixkitty.vampire_blood.item;

import com.unixkitty.vampire_blood.capability.blood.IBloodVessel;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModDamageTypes;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Objects;

public class BloodKnifeItem extends SwordItem
{
    public static final String VICTIM_NBT_NAME = "knifeVictim";

    public BloodKnifeItem()
    {
        super(Tiers.IRON, 1, 6F, new Item.Properties());
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity livingEntity)
    {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;

        if (player instanceof ServerPlayer serverPlayer && this.stillValidBloodletting(stack, serverPlayer))
        {
            ItemStack bottleStack = livingEntity.getOffhandItem();
            CompoundTag tag = stack.getTag();

            if (tag != null)
            {
                if (tag.contains(VICTIM_NBT_NAME))
                {
                    int entityId = tag.getInt(VICTIM_NBT_NAME);
                    Entity entity = serverPlayer.level().getEntity(entityId);
                    ItemStack resultStack = ItemStack.EMPTY;
                    IBloodVessel[] bloodVessel = new IBloodVessel[1];
                    boolean[] vampireVictimSelf = new boolean[]{false};

                    if (entity instanceof LivingEntity victim)
                    {
                        if (entity instanceof ServerPlayer victimPlayer)
                        {
                            victimPlayer.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                            {
                                bloodVessel[0] = vampirePlayerData;

                                vampireVictimSelf[0] = entityId == serverPlayer.getId() && vampirePlayerData.getVampireLevel().getId() > VampirismLevel.IN_TRANSITION.getId();
                            });
                        }
                        else
                        {
                            victim.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodEntityStorage -> bloodVessel[0] = bloodEntityStorage);
                        }

                        if (bloodVessel[0] != null)
                        {
                            int count = Config.bloodPointsFromBottles.get();

                            if (bloodVessel[0].getBloodPoints() >= count)
                            {
                                boolean success = false;

                                for (int i = 0; i < count; i++)
                                {
                                    success = bloodVessel[0].decreaseBlood(serverPlayer, victim);
                                }

                                if (success)
                                {
                                    if (!vampireVictimSelf[0] && !victim.hasEffect(MobEffects.DAMAGE_BOOST) && !victim.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !bloodVessel[0].isCharmedBy(serverPlayer))
                                    {
                                        victim.hurt(ModDamageTypes.source(ModDamageTypes.BLOOD_LOSS, victim.level(), serverPlayer), 1F);
                                    }

                                    resultStack = BloodBottleItem.createItemStack(bloodVessel[0].getBloodType());
                                }
                            }
                        }
                    }

                    if (!resultStack.isEmpty())
                    {
                        stack.hurtAndBreak(1, serverPlayer, (player1) -> player1.broadcastBreakEvent(serverPlayer.getUsedItemHand()));
                        bottleStack.shrink(1);

                        if (!serverPlayer.getInventory().add(resultStack))
                        {
                            serverPlayer.drop(resultStack, false);
                        }
                    }
                }

                tag.remove(VICTIM_NBT_NAME);
            }
        }

        return stack;
    }

    @Override
    public void onUseTick(@Nonnull Level level, @Nonnull LivingEntity livingEntity, @Nonnull ItemStack stack, int remainingUseDuration)
    {
        if (remainingUseDuration > 0
                && remainingUseDuration % 5 == 0
                && livingEntity instanceof ServerPlayer serverPlayer
                && !stillValidBloodletting(stack, serverPlayer))
        {
            serverPlayer.releaseUsingItem();

            CompoundTag tag = stack.getTag();

            if (tag != null)
            {
                tag.remove(VICTIM_NBT_NAME);
            }
        }

        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
    }

    @Override
    public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity livingEntity, int timeCharged)
    {
        if (livingEntity instanceof ServerPlayer && stack.hasTag())
        {
            Objects.requireNonNull(stack.getTag()).remove(VICTIM_NBT_NAME);
        }

        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }

    @Nonnull
    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack stack)
    {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack stack)
    {
        return Items.POTION.getUseDuration(stack) * 2;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand interactionHand)
    {
        if (interactionHand == InteractionHand.MAIN_HAND)
        {
            ItemStack stack = player.getMainHandItem();

            if (stack.getItem() instanceof BloodKnifeItem && player.getOffhandItem().getItem() == Items.GLASS_BOTTLE)
            {
                if (player instanceof ServerPlayer serverPlayer)
                {
                    CompoundTag tag = stack.getOrCreateTag();

                    if (!tag.contains(VICTIM_NBT_NAME))
                    {
                        serverPlayer.sendSystemMessage(Component.translatable("text.vampire_blood.knife_usage_self").withStyle(ChatFormatting.DARK_PURPLE), true);
                        tag.putInt(VICTIM_NBT_NAME, serverPlayer.getId());
                    }
                }

                return ItemUtils.startUsingInstantly(level, player, interactionHand);
            }
        }

        return super.use(level, player, interactionHand);
    }

    private boolean stillValidBloodletting(@Nonnull ItemStack stack, @Nonnull ServerPlayer player)
    {
        if (stack == player.getMainHandItem() && player.getOffhandItem().is(Items.GLASS_BOTTLE))
        {
            CompoundTag tag = stack.getTag();

            if (tag != null)
            {
                int id = tag.getInt(VICTIM_NBT_NAME);

                return id == player.getId() || player.level().getEntity(id) instanceof LivingEntity livingEntity && VampireUtil.canReachEntity(player, livingEntity);
            }
        }

        return false;
    }
}
