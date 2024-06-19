package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VampireBlood.MODID);

    public static final RegistryObject<Item> VAMPIRE_DUST = ITEMS.register("vampire_dust", () -> new Item((new Item.Properties())));
    public static final RegistryObject<Item> BLOODLETTING_KNIFE = ITEMS.register("bloodletting_knife", () -> new SwordItem(Tiers.IRON, 1, 6F, new Item.Properties()));

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
    }
}
