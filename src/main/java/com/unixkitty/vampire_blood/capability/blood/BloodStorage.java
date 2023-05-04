package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.VampireBlood;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class BloodStorage
{
    private static final String ID_NBT_NAME = "id";
    private static final String BLOOD_POINTS_NBT_NAME = "bloodPoints";
    private static final String REGEN_NBT_NAME = "naturalRegen";

    private String id;
    private BloodType bloodType;
    private int maxBloodPoints;
    private int bloodPoints;
    private boolean naturalRegen;

    public void tick(LivingEntity entity)
    {
        entity.level.getProfiler().push("entity_blood_tick");

        //TODO regen either blood points or HP based on config
        if (this.bloodType == BloodType.NONE) return;

        entity.level.getProfiler().pop();
    }

    public void updateBlood(String id)
    {
        this.id = id;

        VampireBlood.log().debug("Updating blood data for entity: " + this.id);

        BloodEntityConfig bloodConfig = BloodManager.getConfigFor(this.id);

        if (bloodConfig == null)
        {
            this.bloodType = BloodType.NONE;
            this.maxBloodPoints = 0;
            this.bloodPoints = 0;
            this.naturalRegen = false;
        }
        else
        {
            this.bloodType = bloodConfig.getBloodType();
            this.naturalRegen = bloodConfig.isNaturalRegen();

            int lastMaxBloodPoints = this.maxBloodPoints;
            int newMaxBloodPoints = bloodConfig.getBloodPoints();

            if (newMaxBloodPoints != lastMaxBloodPoints)
            {
                this.maxBloodPoints = newMaxBloodPoints;

                if (newMaxBloodPoints < lastMaxBloodPoints && this.bloodPoints >= lastMaxBloodPoints)
                {
                    this.bloodPoints = this.maxBloodPoints;
                }
            }
        }
    }

    public boolean isEdible()
    {
        return this.bloodType != BloodType.NONE && this.maxBloodPoints > 0;
    }

    @Nullable
    public BloodType getBloodType()
    {
        return this.bloodType == BloodType.NONE ? null : this.bloodType;
    }

    public void saveNBTData(CompoundTag tag)
    {
        tag.putString(ID_NBT_NAME, this.id);
        tag.putInt(BloodType.BLOODTYPE_NBT_NAME, this.bloodType.getId());
        tag.putInt(BLOOD_POINTS_NBT_NAME, this.bloodPoints);
        tag.putBoolean(REGEN_NBT_NAME, this.naturalRegen);
    }

    public void loadNBTData(CompoundTag tag)
    {
        this.id = tag.getString(ID_NBT_NAME);
        this.bloodType = BloodType.fromId(tag.getInt(BloodType.BLOODTYPE_NBT_NAME));
        this.bloodPoints = tag.getInt(BLOOD_POINTS_NBT_NAME);
        this.naturalRegen = tag.getBoolean(REGEN_NBT_NAME);
    }
}
