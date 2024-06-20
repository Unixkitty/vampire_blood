package com.unixkitty.vampire_blood.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class BloodKnifeItem extends SwordItem
{
    public BloodKnifeItem()
    {
        super(Tiers.IRON, 1, 6F, new Item.Properties());
    }
}
