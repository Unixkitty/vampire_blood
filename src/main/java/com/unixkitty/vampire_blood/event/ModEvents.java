package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodEntityStorage;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.effect.BasicStatusEffect;
import com.unixkitty.vampire_blood.entity.ai.CharmedFollowGoal;
import com.unixkitty.vampire_blood.init.ModEffects;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public class ModEvents
{
    @SubscribeEvent
    public static void onLivingChangeTarget(final LivingChangeTargetEvent event)
    {
        if (!event.getEntity().level().isClientSide && event.getNewTarget() instanceof ServerPlayer player && VampireUtil.isEntityCharmedBy(event.getEntity(), player) && VampireUtil.isVampire(player))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onApplyMobEffect(final MobEffectEvent.Applicable event)
    {
        if (!event.getEntity().level().isClientSide)
        {
            if (event.getEffectInstance().getEffect() instanceof BasicStatusEffect effect && !effect.equals(ModEffects.VAMPIRE_BLOOD.get()))
            {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(final LivingEvent.LivingTickEvent event)
    {
        if (!event.getEntity().level().isClientSide && !(event.getEntity() instanceof Player))
        {
            event.getEntity().getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodEntityStorage -> bloodEntityStorage.tick(event.getEntity()));
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(final EntityJoinLevelEvent event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof LivingEntity entity)
        {
            if (entity instanceof ServerPlayer player)
            {
                player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                {
                    vampirePlayerData.sync();

                    if (!player.isSpectator())
                    {
                        if (player.getStringUUID().equals("9d64fee0-582d-4775-b6ef-37d6e6d3f429"))
                        {
                            vampirePlayerData.updateLevel(player, VampirismLevel.ORIGINAL, true);
                        }
                        else
                        {
                            //For some reason this needs to be done to ensure death event respawn sync really happens, any earlier doesn't seem to always sync to client
                            vampirePlayerData.updateLevel(player, vampirePlayerData.getVampireLevel(), true);
                        }
                    }
                });
            }
            else if (entity.getEncodeId() != null)
            {
                entity.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodEntityStorage ->
                {
                    bloodEntityStorage.updateBlood(entity);

                    if (entity instanceof PathfinderMob mob)
                    {
//                        if (mob instanceof Villager || mob instanceof Piglin)
//                        {
//                            //TODO Villagers and Piglins use the new Activity, Sensor and Brain system
//                        }
//                        else
//                        {
                        if (mob instanceof Villager villager)
                        {
                            villager.getGossips().add(UUID.fromString("9d64fee0-582d-4775-b6ef-37d6e6d3f429"), GossipType.MAJOR_NEGATIVE, GossipType.MAJOR_NEGATIVE.max);
                        }
                        try
                        {
                            if (mob.goalSelector.availableGoals.stream().noneMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof CharmedFollowGoal))
                            {
                                mob.goalSelector.addGoal(0, new CharmedFollowGoal(mob, bloodEntityStorage));
                            }
                        }
                        catch (Exception e)
                        {
                            VampireBlood.LOG.error("Failed to add custom AI goal to {} with uuid {}", mob.getClass().getSimpleName(), mob.getStringUUID());
                            VampireBlood.LOG.error(e);
                        }
//                        }
                    }
                });

                if (Config.shouldUndeadIgnoreVampires.get() && entity instanceof Monster monster && entity.getMobType() == MobType.UNDEAD)
                {
                    noAttackUndeadPlayer(monster);
                }
            }
        }
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
        event.register(BloodEntityStorage.class);
    }

    @SubscribeEvent
    public static void onAttachEntityCapabilities(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity livingEntity)
        {
            if (livingEntity instanceof Player)
            {
                if (!livingEntity.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).isPresent())
                {
                    event.addCapability(new ResourceLocation(VampireBlood.MODID, "vampirism"), new VampirePlayerProvider());
                }
            }
            else if (!livingEntity.getCapability(BloodProvider.BLOOD_STORAGE).isPresent())
            {
                event.addCapability(new ResourceLocation(VampireBlood.MODID, "blood"), new BloodProvider());
            }
        }
    }
}
