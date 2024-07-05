package com.unixkitty.vampire_blood;

import com.unixkitty.vampire_blood.config.BloodConfigManager;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.init.ModFluids;
import com.unixkitty.vampire_blood.init.ModItems;
import com.unixkitty.vampire_blood.init.ModRegistry;
import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(VampireBlood.MODID)
public class VampireBlood
{
    // The MODID value here should match an entry in the META-INF/mods.toml file
    public static final String MODID = "vampire_blood";
    public static final String MODNAME = "Vampire Blood";

    public static final Logger LOG = LogManager.getLogger(MODNAME);

    public VampireBlood()
    {
        Config.register(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModRegistry.register(modEventBus);

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onLoadComplete);
        modEventBus.addListener(ModItems::onBuildCreativeTabs);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            ModNetworkDispatcher.register();
            PotionBrewing.addMix(Potions.AWKWARD, ModItems.VAMPIRE_DUST.get(), Potions.INVISIBILITY);
        });

        BloodConfigManager.init();
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        event.enqueueWork(ModFluids::postInit);
    }
}
