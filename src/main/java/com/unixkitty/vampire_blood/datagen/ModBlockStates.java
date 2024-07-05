package com.unixkitty.vampire_blood.datagen;

import com.unixkitty.vampire_blood.VampireBlood;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStates extends BlockStateProvider
{
    public ModBlockStates(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, VampireBlood.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {

    }
}
