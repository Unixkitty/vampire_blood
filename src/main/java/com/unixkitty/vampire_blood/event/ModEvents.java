package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodStorage;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.BiFunction;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public class ModEvents
{
    @SubscribeEvent
    public static void onLivingTick(final LivingEvent.LivingTickEvent event)
    {
        if (!event.getEntity().level.isClientSide())
        {
            event.getEntity().getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodStorage -> bloodStorage.tick(event.getEntity()));
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(final EntityJoinLevelEvent event)
    {
        if (!event.getLevel().isClientSide())
        {
            if (event.getEntity() instanceof LivingEntity entity)
            {
                if (entity instanceof ServerPlayer)
                {
                    entity.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                    {
                        vampirePlayerData.sync();
                        vampirePlayerData.syncBlood();
                    });
                }
                else
                {
                    String id = entity.getEncodeId();

                    if (id != null)
                    {
                        entity.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodStorage -> bloodStorage.updateBlood(id));
                    }
                }

                if (Config.shouldUndeadIgnoreVampires.get() && entity instanceof Monster monster && entity.getMobType() == MobType.UNDEAD)
                {
                    noAttackUndeadPlayer(monster);
                }
            }
        }
        /*else
        {
            TestListGenerator.generate();
        }*/
    }

    private static void noAttackUndeadPlayer(Monster monster)
    {
        Goal goalToReplace = monster.targetSelector.availableGoals.stream()
                .filter(wrappedGoal -> {
                    Goal goal = wrappedGoal.getGoal();
                    return goal instanceof NearestAttackableTargetGoal && wrappedGoal.getPriority() == 2 && Player.class.equals(((NearestAttackableTargetGoal<?>) goal).targetType);
                })
                .map(WrappedGoal::getGoal)
                .findFirst()
                .orElse(null);

        if (goalToReplace != null)
        {
            BiFunction<Monster, Predicate<LivingEntity>, NearestAttackableTargetGoal<Player>> replacement = (entity, predicate) -> new NearestAttackableTargetGoal<>(entity, Player.class, 10, true, false, predicate);

            monster.targetSelector.removeGoal(goalToReplace);
            monster.targetSelector.addGoal(2, replacement.apply(monster, entity -> !(entity instanceof Player && entity.getMobType() == MobType.UNDEAD)));
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(final RegisterCapabilitiesEvent event)
    {
        event.register(VampirePlayerData.class);
        event.register(BloodStorage.class);
    }

    @SubscribeEvent
    public static void onAttachCapability(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity)
        {
            if (event.getObject() instanceof Player)
            {
                //Attach vampirism cap
                if (!event.getObject().getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
                {
                    event.addCapability(new ResourceLocation(VampireBlood.MODID, "vampirism"), new VampirePlayerProvider());
                }
            }

            //Attach blood storage
            if (!event.getObject().getCapability(BloodProvider.BLOOD_STORAGE).isPresent())
            {
                event.addCapability(new ResourceLocation(VampireBlood.MODID, "blood"), new BloodProvider());
            }
        }
    }
}
