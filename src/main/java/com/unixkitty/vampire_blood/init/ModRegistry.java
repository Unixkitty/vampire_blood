package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.VampireCommand;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public final class ModRegistry
{
    public static final ReputationEventType REPUTATION_VAMPIRE_PLAYER = ReputationEventType.register("vampire_player");
    public static final ReputationEventType REPUTATION_CHARMED_BY_VAMPIRE_PLAYER = ReputationEventType.register("charmed_by_vampire_player");

    private static Boolean registered = false;

    public static void register(IEventBus modEventBus)
    {
        if (registered != null && !registered)
        {
            registered = null;

            ModEffects.EFFECTS.register(modEventBus);
            ModItems.ITEMS.register(modEventBus);
            ModParticles.PARTICLES.register(modEventBus);
        }
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event)
    {
        VampireCommand.register(event.getDispatcher());
    }
}
