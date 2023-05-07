package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public class BloodEntityStorage
{
    private static final String ID_NBT_NAME = "id";
    private static final String BLOOD_POINTS_NBT_NAME = "bloodPoints";
    private static final String REGEN_NBT_NAME = "naturalRegen";
    private static final String REGEN_TIMER_NBT_NAME = "naturalRegenTimer";

    private String id = "";
    private BloodType bloodType = BloodType.NONE;
    private int maxBloodPoints = 0;
    private int bloodPoints = 0;
    private boolean naturalRegen = false;
    private int naturalRegenTimer = 0;

    private int ticksPerHeal = 0;

    public void tick(LivingEntity entity)
    {
        if (this.bloodType == BloodType.NONE) return;

        entity.level.getProfiler().push("entity_blood_tick");

        if (Config.healthOrBloodPoints.get())
        {
            updateBloodHealth(entity);
        }

        if (this.bloodType != BloodType.FRAIL && Config.entityRegen.get() && naturalRegen && entity.tickCount % this.ticksPerHeal == 0)
        {
            if (Config.healthOrBloodPoints.get())
            {
                if (entity.getHealth() < entity.getMaxHealth())
                {
                    entity.heal(1.0F);
                }
            }
            else if (this.bloodPoints < this.maxBloodPoints)
            {
                ++this.bloodPoints;
            }
        }

        entity.level.getProfiler().pop();
    }

    public void updateBlood(LivingEntity entity)
    {
        this.id = entity.getEncodeId();

        BloodEntityConfig bloodConfig = BloodManager.getConfigFor(this.id);

        if (bloodConfig == null)
        {
            this.bloodType = BloodType.NONE;
            this.naturalRegen = false;

            this.maxBloodPoints = 0;
            this.bloodPoints = 0;
        }
        else
        {
            this.bloodType = bloodConfig.getBloodType();
            this.naturalRegen = bloodConfig.isNaturalRegen();

            if (Config.healthOrBloodPoints.get())
            {
                updateBloodHealth(entity);
            }
            else
            {
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

                this.ticksPerHeal = Config.entityRegenTime.get() / this.maxBloodPoints;
            }
        }
    }

    public boolean isEdible()
    {
        return this.bloodType != BloodType.NONE && this.maxBloodPoints > 0;
    }

    public BloodType getBloodType()
    {
        return this.bloodType;
    }

    public int getBloodPoints()
    {
        return this.bloodPoints;
    }

    public int getMaxBloodPoints()
    {
        return this.maxBloodPoints;
    }

    private void updateBloodHealth(LivingEntity entity)
    {
        this.maxBloodPoints = healthToBlood(entity.getMaxHealth());
        this.bloodPoints = healthToBlood(entity.getHealth());
        this.ticksPerHeal = Config.entityRegenTime.get() / (int) entity.getMaxHealth();
    }

    private int healthToBlood(float value)
    {
        return (int) Math.ceil(value * this.bloodType.getBloodSaturationModifier());
    }

    public void saveNBTData(CompoundTag tag)
    {
        tag.putString(ID_NBT_NAME, this.id);
        tag.putInt(BloodType.BLOODTYPE_NBT_NAME, this.bloodType.getId());
        tag.putInt(BLOOD_POINTS_NBT_NAME, this.bloodPoints);
        tag.putBoolean(REGEN_NBT_NAME, this.naturalRegen);
        tag.putInt(REGEN_TIMER_NBT_NAME, this.naturalRegenTimer);
    }

    public void loadNBTData(CompoundTag tag)
    {
        this.id = tag.getString(ID_NBT_NAME);
        this.bloodType = BloodType.fromId(tag.getInt(BloodType.BLOODTYPE_NBT_NAME));
        this.bloodPoints = tag.getInt(BLOOD_POINTS_NBT_NAME);
        this.naturalRegen = tag.getBoolean(REGEN_NBT_NAME);
        this.naturalRegenTimer = tag.getInt(REGEN_TIMER_NBT_NAME);
    }
}
