package com.unixkitty.vampire_blood.init;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.advancement.trigger.BloodlettingTrigger;
import com.unixkitty.vampire_blood.advancement.trigger.DrinkBloodTrigger;
import com.unixkitty.vampire_blood.advancement.trigger.VampireAbilityUseTrigger;
import com.unixkitty.vampire_blood.advancement.trigger.VampireLevelChangeTrigger;
import com.unixkitty.vampire_blood.command.EntitySummonArgument;
import com.unixkitty.vampire_blood.command.VampireCommand;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public final class ModRegistry
{
    public static final ReputationEventType REPUTATION_VAMPIRE_PLAYER = ReputationEventType.register("vampire_player");
    public static final ReputationEventType REPUTATION_CHARMED_BY_VAMPIRE_PLAYER = ReputationEventType.register("charmed_by_vampire_player");

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, VampireBlood.MODID);
    private static final RegistryObject<SingletonArgumentInfo<EntitySummonArgument>> ENTITY_SUMMON_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("entity_summon", () ->
            ArgumentTypeInfos.registerByClass(EntitySummonArgument.class,
                    SingletonArgumentInfo.contextFree(EntitySummonArgument::id)));

    public static PlayerTrigger CHARMED_ENTITY_TRIGGER;

    private static Boolean registered = false;

    public static void register(IEventBus modEventBus)
    {
        if (registered != null && !registered)
        {
            registered = null;

            ModEffects.EFFECTS.register(modEventBus);
            ModBlocks.BLOCKS.register(modEventBus);
            ModItems.ITEMS.register(modEventBus);
            ModFluids.FLUID_TYPES.register(modEventBus);
            ModFluids.FLUIDS.register(modEventBus);
            ModParticles.PARTICLES.register(modEventBus);
            COMMAND_ARGUMENT_TYPES.register(modEventBus);

            BloodlettingTrigger.register();
            DrinkBloodTrigger.register();
            VampireLevelChangeTrigger.register();
            VampireAbilityUseTrigger.register();
            CHARMED_ENTITY_TRIGGER = CriteriaTriggers.register(new PlayerTrigger(new ResourceLocation(VampireBlood.MODID, "charmed_entity")));
        }
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event)
    {
        VampireCommand.register(event.getDispatcher());
    }
}
