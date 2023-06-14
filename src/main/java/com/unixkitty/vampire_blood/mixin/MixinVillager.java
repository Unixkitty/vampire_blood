package com.unixkitty.vampire_blood.mixin;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import com.unixkitty.vampire_blood.entity.ai.ModAIGoals;
import com.unixkitty.vampire_blood.util.debug.GossipMapDifference;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.server.level.ServerLevel;
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

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

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

    @Inject(method = "gossip(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/gossip/GossipContainer;transferFrom(Lnet/minecraft/world/entity/ai/gossip/GossipContainer;Lnet/minecraft/util/RandomSource;I)V"))
    public void beforeTransferGossips(ServerLevel serverLevel, Villager target, long gameTime, CallbackInfo ci)
    {
        VampireBlood.log().debug("{} is gossiping with {}", ((Villager)(Object)this).getDisplayName().getString(), target.getDisplayName().getString());

        var map = target.getGossips().getGossipEntries();

        if (!map.isEmpty())
        {
            target.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodEntityStorage -> bloodEntityStorage.tempGossipMap = map);
        }
    }

    @Inject(method = "gossip(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/gossip/GossipContainer;transferFrom(Lnet/minecraft/world/entity/ai/gossip/GossipContainer;Lnet/minecraft/util/RandomSource;I)V", shift = At.Shift.AFTER))
    public void afterTransferGossips(ServerLevel serverLevel, Villager target, long gameTime, CallbackInfo ci)
    {
        Map<UUID, Object2IntMap<GossipType>> newMap = target.getGossips().getGossipEntries();
        GossipMapDifference.Holder mapHolder = new GossipMapDifference.Holder(Collections.emptyMap());

        target.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(mapHolder::takeGossipMap);

        Map<UUID, Object2IntMap<GossipType>> oldMap = mapHolder.getMap();

        if (!newMap.isEmpty())
        {
            Map<UUID, GossipMapDifference> differences = GossipMapDifference.compareMaps(oldMap, newMap);

            for (Map.Entry<UUID, GossipMapDifference> entry : differences.entrySet())
            {
                GossipMapDifference diff = entry.getValue();
                Object2IntMap<GossipType> oldValue = diff.getOldValue();
                Object2IntMap<GossipType> newValue = diff.getNewValue();

                for (GossipType gossipType : GossipType.values())
                {
                    int oldValueForType = oldValue != null ? oldValue.getInt(gossipType) : 0;
                    int newValueForType = newValue != null ? newValue.getInt(gossipType) : 0;

                    if (oldValueForType != newValueForType)
                    {
                        VampireBlood.log().debug("A villager heard from another {}{} things about {}", oldValueForType < newValueForType ? "+" : "-", gossipType, serverLevel.getPlayerByUUID(entry.getKey()));
                    }
                }
            }
        }
    }
}
