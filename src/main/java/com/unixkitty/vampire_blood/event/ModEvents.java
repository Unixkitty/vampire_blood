package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.provider.VampirePlayerProvider;
import com.unixkitty.vampire_blood.capability.VampirismStage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.BiFunction;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = VampireBlood.MODID)
public class ModEvents
{
    @SubscribeEvent
    public static void onLivingHurt(final LivingHurtEvent event)
    {
        if (!event.getEntity().getLevel().isClientSide() && event.getEntity() instanceof Player player)
        {
            player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
            {
                if (vampirePlayerData.getVampireLevel().getId() > VampirismStage.NOT_VAMPIRE.getId())
                {
                    if (Config.increasedDamageFromWood.get() && !(event.getSource() instanceof IndirectEntityDamageSource) && event.getSource().getEntity() instanceof LivingEntity attacker && event.getAmount() > 0 && attacker.getMainHandItem().getItem() instanceof TieredItem item && item.getTier() == Tiers.WOOD)
                    {
                        event.setAmount(event.getAmount() * 1.25F);

                        vampirePlayerData.addPreventRegenTicks(60);
                    }

                    if (event.getSource().isFire())
                    {
                        event.setAmount(event.getAmount() > 0 ? event.getAmount() * 2 : event.getAmount());

                        vampirePlayerData.addPreventRegenTicks(20);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(final EntityJoinLevelEvent event)
    {
        if (!event.getLevel().isClientSide())
        {
            if (event.getEntity() instanceof ServerPlayer player)
            {
                player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).ifPresent(vampirePlayerData ->
                {
                    vampirePlayerData.sync();
                    vampirePlayerData.syncBlood();
                });
            }
            else if (Config.shouldUndeadIgnoreVampires.get() && event.getEntity() instanceof Monster entity && entity.getMobType() == MobType.UNDEAD)
            {
                noAttackUndeadPlayer(entity);
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
    }

}
