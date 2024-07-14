package com.unixkitty.vampire_blood.item;

import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.fluid.BloodFluidType;
import com.unixkitty.vampire_blood.init.ModFluids;
import com.unixkitty.vampire_blood.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BloodBottleItem extends Item implements IBloodVesselItem
{
    private final BloodType bloodType;
    private final Item emptyVesselItem;
    private final int fluidCapacity;

    public BloodBottleItem(BloodType bloodType)
    {
        this(bloodType, Items.GLASS_BOTTLE, 250);
    }

    public BloodBottleItem(BloodType bloodType, Item emptyVesselItem, int fluidCapacity)
    {
        super(new Properties().rarity(bloodType.getItemRarity()).stacksTo(4).craftRemainder(emptyVesselItem));

        this.bloodType = bloodType;
        this.emptyVesselItem = emptyVesselItem;
        this.fluidCapacity = fluidCapacity;
    }

    @Override
    public BloodType getBloodType()
    {
        return this.bloodType;
    }

    @Override
    public int getBloodValue()
    {
        return Config.bloodPointsFromBottles.get();
    }

    @Override
    public ItemStack getEmptyVesselItem()
    {
        return new ItemStack(this.emptyVesselItem);
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack itemStack, @Nonnull Level level, @Nonnull LivingEntity livingEntity)
    {
        return this.consumeStoredBlood(itemStack, livingEntity);
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
        return Items.HONEY_BOTTLE.getUseDuration(pStack);
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
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
    }

    @Override
    public final ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag tag)
    {
        return new FluidHandlerItemStackSimple.SwapEmpty(stack, getEmptyVesselItem(), this.fluidCapacity)
        {
            @Override
            public @NotNull FluidStack getFluid()
            {
                return new FluidStack(switch (BloodBottleItem.this.bloodType)
                {
                    case NONE -> throw new IllegalArgumentException("NONE blood fluid?");
                    case FRAIL -> ModFluids.FRAIL_BLOOD.source.get();
                    case CREATURE -> ModFluids.CREATURE_BLOOD.source.get();
                    case HUMAN -> ModFluids.HUMAN_BLOOD.source.get();
                    case VAMPIRE -> ModFluids.VAMPIRE_BLOOD.source.get();
                    case PIGLIN -> ModFluids.PIGLIN_BLOOD.source.get();
                }, BloodBottleItem.this.fluidCapacity);
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack)
            {
                return stack.getFluid().getFluidType() instanceof BloodFluidType bloodFluidType && bloodFluidType.getBloodType() == BloodBottleItem.this.bloodType;
            }

            @Override
            public int fill(@Nonnull FluidStack resource, FluidAction action)
            {
                return 0;
            }
        };
    }

    public static Item getItem(BloodType bloodType)
    {
        return switch (bloodType)
        {
            case NONE -> Items.AIR;
            case FRAIL -> ModItems.FRAIL_BLOOD_BOTTLE.get();
            case CREATURE -> ModItems.CREATURE_BLOOD_BOTTLE.get();
            case HUMAN -> ModItems.HUMAN_BLOOD_BOTTLE.get();
            case VAMPIRE -> ModItems.VAMPIRE_BLOOD_BOTTLE.get();
            case PIGLIN -> ModItems.PIGLIN_BLOOD_BOTTLE.get();
        };
    }
}
