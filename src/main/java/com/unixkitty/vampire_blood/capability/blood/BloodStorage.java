package com.unixkitty.vampire_blood.capability.blood;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public class BloodStorage
{
    private static final String BLOOD_POINTS_NBT_NAME = "bloodPoints";

    private BloodType bloodType;
    private int bloodPoints;

    public void tick(LivingEntity entity)
    {
        entity.level.getProfiler().push("entity_blood_tick");

        //TODO regen either blood points or HP based on config

        entity.level.getProfiler().pop();
    }

    public void saveNBTData(CompoundTag tag)
    {
//        tag.putInt(BLOODTYPE_NBT_NAME, this.bloodType.ordinal());
//        tag.putInt(BLOOD_POINTS_NBT_NAME, this.bloodPoints);
    }

    public void loadNBTData(CompoundTag tag)
    {
//        this.bloodType = BloodType.fromId(tag.getInt(VampirePlayerData.BLOODTYPE_NBT_NAME));
//        this.bloodPoints = tag.getInt(BLOOD_POINTS_NBT_NAME);
    }
}
