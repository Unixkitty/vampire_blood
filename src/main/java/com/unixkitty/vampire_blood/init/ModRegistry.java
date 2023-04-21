package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.VampireCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public final class ModRegistry
{
    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event)
    {
        VampireCommand.register(event.getDispatcher());
    }
}
