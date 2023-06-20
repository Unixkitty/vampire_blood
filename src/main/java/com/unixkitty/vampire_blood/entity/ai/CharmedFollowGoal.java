package com.unixkitty.vampire_blood.entity.ai;

import com.unixkitty.vampire_blood.capability.blood.BloodEntityStorage;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class CharmedFollowGoal extends TemptGoal
{
    public CharmedFollowGoal(PathfinderMob charmedFollower, BloodEntityStorage bloodData)
    {
        super(charmedFollower, 0.65D, Ingredient.of(Items.AIR), false);

        this.targetingConditions = TargetingConditions.forNonCombat().range(20.0D).selector(bloodData::isCurrentlyCharmingPlayer);
    }

    @Override
    public boolean canUse()
    {
        return !this.mob.isSleeping() && super.canUse();
    }
}
