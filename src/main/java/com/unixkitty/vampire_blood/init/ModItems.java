package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.item.BloodBottleItem;
import com.unixkitty.vampire_blood.item.BloodKnifeItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VampireBlood.MODID);

    public static final RegistryObject<Item> VAMPIRE_DUST = ITEMS.register("vampire_dust", () -> new Item((new Item.Properties())));
    public static final RegistryObject<Item> BLOODLETTING_KNIFE = ITEMS.register("bloodletting_knife", () -> new BloodKnifeItem());

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
            event.accept(HUMAN_BLOOD_BOTTLE.get());
            event.accept(FRAIL_BLOOD_BOTTLE.get());
            event.accept(CREATURE_BLOOD_BOTTLE.get());
            event.accept(VAMPIRE_BLOOD_BOTTLE.get());
            event.accept(PIGLIN_BLOOD_BOTTLE.get());
        }
    }
}
