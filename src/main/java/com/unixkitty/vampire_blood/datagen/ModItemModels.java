package com.unixkitty.vampire_blood.datagen;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.item.BloodBottleItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ModItemModels extends ItemModelProvider
{
    public ModItemModels(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, VampireBlood.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        basicItem(ModItems.VAMPIRE_DUST.get());

        tool(ModItems.BLOODLETTING_KNIFE.get());

        for (BloodType bloodType : BloodType.values())
        {
            if (bloodType != BloodType.NONE)
            {
                basicItem(BloodBottleItem.getItem(bloodType));
                basicItem(ModItems.getBloodBucketItem(bloodType));
            }
        }
    }

    private void tool(Item item)
    {
        tool(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)));
    }

    private void tool(ResourceLocation item)
    {
        getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", new ResourceLocation(item.getNamespace(), "item/" + item.getPath()));
    }
}
