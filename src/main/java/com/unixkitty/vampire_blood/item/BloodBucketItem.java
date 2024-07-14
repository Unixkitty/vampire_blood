package com.unixkitty.vampire_blood.item;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class BloodBucketItem extends BucketItem implements IBloodVesselItem
{
    private final BloodType bloodType;
    private final Item emptyVesselItem;

    public BloodBucketItem(BloodType bloodType, Supplier<? extends Fluid> storedFluid)
    {
        this(bloodType, Items.BUCKET, storedFluid);
    }

    public BloodBucketItem(BloodType bloodType, Item emptyVesselItem, Supplier<? extends Fluid> storedFluid)
    {
        super(storedFluid, new Item.Properties().craftRemainder(emptyVesselItem).stacksTo(1).rarity(bloodType.getItemRarity()));

        this.bloodType = bloodType;
        this.emptyVesselItem = emptyVesselItem;
    }

    @Override
    public BloodType getBloodType()
    {
        return this.bloodType;
    }

    @Override
    public int getBloodValue()
    {
        return Config.bloodPointsFromBottles.get() * 4;
    }

    @Override
    public ItemStack getEmptyVesselItem()
    {
        return new ItemStack(this.emptyVesselItem);
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack pStack, @Nonnull Level pLevel, @Nonnull LivingEntity pLivingEntity)
    {
        return this.consumeStoredBlood(pStack, pLivingEntity);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack pStack, @Nullable Level pLevel, @Nonnull List<Component> pTooltipComponents, @Nonnull TooltipFlag pIsAdvanced)
    {
        this.appendHoverText(pTooltipComponents);

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack pStack)
    {
        return Items.HONEY_BOTTLE.getUseDuration(pStack) * 4;
    }

    @Nonnull
    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack pStack)
    {
        return UseAnim.DRINK;
    }

    @Nonnull
    @Override
    public SoundEvent getDrinkingSound()
    {
        return SoundEvents.HONEY_DRINK;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level pLevel, @Nonnull Player pPlayer, @Nonnull InteractionHand pHand)
    {
        InteractionResultHolder<ItemStack> fluidResult = super.use(pLevel, pPlayer, pHand);

        return fluidResult.getResult() == InteractionResult.FAIL || fluidResult.getResult() == InteractionResult.PASS ? ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand) : fluidResult;
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt)
    {
        return new FluidBucketWrapper(stack);
    }
}
