package com.unixkitty.vampire_blood.datagen;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

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
        basicItem(ModItems.BLOODLETTING_KNIFE.get());
    }
}
