package com.unixkitty.vampire_blood.mixin;

import com.unixkitty.vampire_blood.entity.ai.ModAIGoals;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class MixinVillager
{
    @Shadow
    @Final
    private GossipContainer gossips;

    @Inject(at = @At("TAIL"), method = "onReputationEventFrom(Lnet/minecraft/world/entity/ai/village/ReputationEventType;Lnet/minecraft/world/entity/Entity;)V")
    public void onReputationEventFrom(ReputationEventType eventType, Entity target, CallbackInfo ci)
    {
        if (eventType == ModAIGoals.VAMPIRE_PLAYER)
        {
            this.gossips.add(target.getUUID(), GossipType.MAJOR_NEGATIVE, GossipType.MAJOR_NEGATIVE.max);
        }
        else if (eventType == ModAIGoals.CHARMED_BY_VAMPIRE_PLAYER)
        {
            this.gossips.add(target.getUUID(), GossipType.MAJOR_POSITIVE, GossipType.MAJOR_POSITIVE.max);
        }
    }
}
