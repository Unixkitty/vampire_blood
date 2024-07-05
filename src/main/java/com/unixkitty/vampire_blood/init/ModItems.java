package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.item.BloodBottleItem;
import com.unixkitty.vampire_blood.item.BloodKnifeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VampireBlood.MODID);

    public static final RegistryObject<Item> VAMPIRE_DUST = ITEMS.register("vampire_dust", () -> new Item((new Item.Properties())));
    public static final RegistryObject<Item> BLOODLETTING_KNIFE = ITEMS.register("bloodletting_knife", BloodKnifeItem::new);

    public static final RegistryObject<Item> HUMAN_BLOOD_BOTTLE = bloodBottle(BloodType.HUMAN);
    public static final RegistryObject<Item> FRAIL_BLOOD_BOTTLE = bloodBottle(BloodType.FRAIL);
    public static final RegistryObject<Item> CREATURE_BLOOD_BOTTLE = bloodBottle(BloodType.CREATURE);
    public static final RegistryObject<Item> VAMPIRE_BLOOD_BOTTLE = bloodBottle(BloodType.VAMPIRE);
    public static final RegistryObject<Item> PIGLIN_BLOOD_BOTTLE = bloodBottle(BloodType.PIGLIN);

    private static RegistryObject<Item> bloodBottle(BloodType bloodType)
    {
        return ITEMS.register(bloodType.name().toLowerCase() + "_blood_bottle", () -> new BloodBottleItem(bloodType));
    }

    public static void onBuildCreativeTabs(final BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(VAMPIRE_DUST.get());
        }
        else if (event.getTabKey() == CreativeModeTabs.COMBAT)
        {
            event.accept(BLOODLETTING_KNIFE.get());
        }
        else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS)
        {
            for (BloodType bloodType : BloodType.values())
            {
                if (bloodType != BloodType.NONE)
                {
                    event.accept(BloodBottleItem.getItem(bloodType));
                }
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            for (BloodType bloodType : BloodType.values())
            {
                if (bloodType != BloodType.NONE)
                {
                    event.accept(getBloodBucketItem(bloodType));
                }
            }
        }
    }

    public static Item getBloodBucketItem(BloodType bloodType)
    {
        return switch (bloodType)
        {
            case NONE -> Items.AIR;
            case FRAIL -> ModFluids.FRAIL_BLOOD.bucket.get();
            case CREATURE -> ModFluids.CREATURE_BLOOD.bucket.get();
            case HUMAN -> ModFluids.HUMAN_BLOOD.bucket.get();
            case VAMPIRE -> ModFluids.VAMPIRE_BLOOD.bucket.get();
            case PIGLIN -> ModFluids.PIGLIN_BLOOD.bucket.get();
        };
    }

    public static Rarity getBloodItemRarity(BloodType bloodType)
    {
        return Rarity.create(bloodType.name() + "_blood", switch (bloodType)
        {
            case NONE -> ChatFormatting.BLACK;
            case FRAIL -> ChatFormatting.GRAY;
            case CREATURE -> ChatFormatting.GREEN;
            case HUMAN -> ChatFormatting.DARK_RED;
            case VAMPIRE -> ChatFormatting.DARK_PURPLE;
            case PIGLIN -> ChatFormatting.GOLD;
        });
    }
}
