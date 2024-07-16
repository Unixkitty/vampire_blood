package com.unixkitty.vampire_blood.datagen;

import com.unixkitty.vampire_blood.VampireBlood;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = VampireBlood.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new ModBlockStates(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ModItemModels(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new CraftingRecipes(packOutput));
        generator.addProvider(event.includeServer(), new ModAdvancements(packOutput, event.getLookupProvider(), event.getExistingFileHelper()));
        //generator.addProvider(true, new DamageTypeTagProvider(packOutput, lookupProvider, fileHelper));
    }
}
