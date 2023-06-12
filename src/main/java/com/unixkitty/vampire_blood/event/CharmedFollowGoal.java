package com.unixkitty.vampire_blood.event;

import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class CharmedFollowGoal extends TemptGoal
{
    public CharmedFollowGoal(PathfinderMob charmedFollower)
    {
        super(charmedFollower, 0.75D, Ingredient.of(Items.AIR), false);

        this.targetingConditions = TargetingConditions.forNonCombat().range(16.0D).selector(this::isCharmedBy);
    }

    private boolean isCharmedBy(LivingEntity entity)
    {
        return entity instanceof ServerPlayer serverPlayer && !serverPlayer.isCreative() && !serverPlayer.isSpectator() && this.mob.getCapability(BloodProvider.BLOOD_STORAGE).map(bloodEntityStorage -> bloodEntityStorage.isCurrentlyCharmingPlayer(serverPlayer)).orElse(false);
    }
}
