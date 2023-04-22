package com.unixkitty.vampire_blood;

import com.unixkitty.vampire_blood.network.ModNetworkDispatcher;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(VampireBlood.MODID)
public class VampireBlood
{
    // The MODID value here should match an entry in the META-INF/mods.toml file
    public static final String MODID = "vampire_blood";
    public static final String MODNAME = "Vampire Blood";

    private static final Logger LOG = LogManager.getLogger(MODNAME);

    public VampireBlood()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);

//        modEventBus.addListener(this::onCommonSetup);

//        MinecraftForge.EVENT_BUS.register(ModRegistry.class);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(ModNetworkDispatcher::register);
    }

    public static Logger log()
    {
        return LOG;
    }
}
