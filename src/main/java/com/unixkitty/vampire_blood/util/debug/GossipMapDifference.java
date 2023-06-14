package com.unixkitty.vampire_blood.util.debug;

import com.unixkitty.vampire_blood.capability.blood.BloodEntityStorage;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.entity.ai.gossip.GossipType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GossipMapDifference
{
    private final Object2IntMap<GossipType> oldValue;
    private final Object2IntMap<GossipType> newValue;

    private GossipMapDifference(Object2IntMap<GossipType> oldValue, Object2IntMap<GossipType> newValue)
    {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Object2IntMap<GossipType> getOldValue()
    {
        return oldValue;
    }

    public Object2IntMap<GossipType> getNewValue()
    {
        return newValue;
    }

    public static Map<UUID, GossipMapDifference> compareMaps(Map<UUID, Object2IntMap<GossipType>> oldMap, Map<UUID, Object2IntMap<GossipType>> newMap)
    {
        Map<UUID, GossipMapDifference> differences = new HashMap<>();

        for (UUID key : oldMap.keySet())
        {
            if (!newMap.containsKey(key) || !oldMap.get(key).equals(newMap.get(key)))
            {
                differences.put(key, new GossipMapDifference(oldMap.get(key), newMap.get(key)));
            }
        }

        for (UUID key : newMap.keySet())
        {
            if (!oldMap.containsKey(key))
            {
                differences.put(key, new GossipMapDifference(null, newMap.get(key)));
            }
        }

        return differences;
    }

    public static class Holder
    {
        private Map<UUID, Object2IntMap<GossipType>> map;

        public Holder(Map<UUID, Object2IntMap<GossipType>> map)
        {
            this.map = map;
        }

        public void takeGossipMap(BloodEntityStorage bloodData)
        {
            if (bloodData.tempGossipMap != null)
            {
                this.map = bloodData.tempGossipMap;
            }
        }

        public Map<UUID, Object2IntMap<GossipType>> getMap()
        {
            return this.map;
        }
    }
}
