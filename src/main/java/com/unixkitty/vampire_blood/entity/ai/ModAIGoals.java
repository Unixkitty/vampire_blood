package com.unixkitty.vampire_blood.entity.ai;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodEntityStorage;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ModAIGoals
{
    public static final ReputationEventType VAMPIRE_PLAYER = ReputationEventType.register("vampire_player");
    public static final ReputationEventType CHARMED_BY_VAMPIRE_PLAYER = ReputationEventType.register("charmed_by_vampire_player");

    public static class CharmedFollowGoal extends TemptGoal
    {
        public CharmedFollowGoal(PathfinderMob charmedFollower, BloodEntityStorage bloodData)
        {
            super(charmedFollower, 0.5D, Ingredient.of(Items.AIR), false);

            this.targetingConditions = TargetingConditions.forNonCombat().range(16.0D).selector(bloodData::isCurrentlyCharmingPlayer);
        }
    }

    public static class FleeFromKnownVampireGoal extends AvoidEntityGoal<Player>
    {
        public FleeFromKnownVampireGoal(PathfinderMob mob, BloodEntityStorage bloodData)
        {
            super(mob, Player.class, 16F, 0.75D, 0.75D);

            this.avoidEntityTargeting = TargetingConditions.forNonCombat().range(this.maxDist).selector(bloodData::isKnownVampire);
        }

        @Override
        public boolean canUse()
        {
            boolean result = super.canUse();

            if (result && this.toAvoid != null)
            {
                VampireBlood.log().debug("{} is beginning to avoid {}", this.mob.getDisplayName().getString(), this.toAvoid.getDisplayName().getString());
            }

            return result;
        }

        @Override
        public void stop()
        {
            if (this.toAvoid != null)
            {
                VampireBlood.log().debug("{} is done avoiding {} for now", this.mob.getDisplayName().getString(), this.toAvoid.getDisplayName().getString());
            }

            super.stop();
        }
    }
}
