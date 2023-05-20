package com.unixkitty.vampire_blood.capability.player;

import com.google.common.collect.EvictingQueue;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class VampirePlayerDiet
{
    private static final String DIET_HISTORY_NBT_NAME = "dietHistory";

    static final int DIET_HISTORY_SIZE = VampirePlayerBloodData.MAX_THIRST / 2;

    private final EvictingQueue<BloodType> diet = EvictingQueue.create(DIET_HISTORY_SIZE);

    VampirePlayerDiet(BloodType template)
    {
        //Fill with template in case it's our first time loading and we won't have history from NBT
        reset(template);
    }

    public int[] toIntArray()
    {
        return this.diet.stream().mapToInt(BloodType::getId).toArray();
    }

    //TODO add help to error if this method will be used in many places
    public void fromIntArray(int[] array)
    {
        BloodType type;

        for (int id : array)
        {
            type = VampirismTier.fromId(BloodType.class, id);

            if (type == null || type == BloodType.NONE)
            {
                throw new IllegalArgumentException("Unexpected value inside int array for player bloodtype diet: " + type);
            }

            this.diet.add(type);
        }
    }

    Pair<BloodType, Float> updateWith(@Nonnull BloodType bloodType)
    {
        this.diet.add(bloodType);

        return getBloodTypeAndPurity();
    }

    void reset(BloodType type)
    {
        for (int i = 0; i < DIET_HISTORY_SIZE; i++)
        {
            diet.add(type);
        }
    }

    private Pair<BloodType, Float> getBloodTypeAndPurity()
    {
        Map<BloodType, Integer> bloodTypeFrequency = new HashMap<>();

        for (BloodType bloodType : diet)
        {
            bloodTypeFrequency.put(bloodType, bloodTypeFrequency.getOrDefault(bloodType, 0) + 1);
        }

        return bloodTypeFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> new ImmutablePair<>(entry.getKey(), entry.getValue() / (float) DIET_HISTORY_SIZE))
                .orElse(new ImmutablePair<>(BloodType.FRAIL, 1.0F));
    }

    //===============================================

    void saveNBT(CompoundTag tag)
    {
        tag.putIntArray(DIET_HISTORY_NBT_NAME, toIntArray());
    }

    void loadNBT(CompoundTag tag)
    {
        fromIntArray(tag.getIntArray(DIET_HISTORY_NBT_NAME));
    }
}
