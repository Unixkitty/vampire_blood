package com.unixkitty.vampire_blood.capability.blood;

import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.init.ModRegistry;
import com.unixkitty.vampire_blood.util.VampireUtil;
import com.unixkitty.vampire_blood.util.VampirismTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;

public class BloodEntityStorage
{
    private static final String ID_NBT_NAME = "id";
    private static final String BLOOD_POINTS_NBT_NAME = "bloodPoints";
    private static final String MAX_BLOOD_POINTS_NBT_NAME = "maxBloodPoints";
    private static final String REGEN_NBT_NAME = "naturalRegen";
    private static final String REGEN_TIMER_NBT_NAME = "naturalRegenTimer";
    private static final String FRESH_ENTITY_NBT_NAME = "freshEntity";

    private String id = "";
    private BloodType bloodType = BloodType.NONE;
    private int maxBloodPoints = 0;
    private int bloodPoints = 0;
    private boolean naturalRegen = false;
    private int naturalRegenTimer = 0;

    private int ticksPerRegen = 0;

    private boolean freshEntity = true;

    public void tick(LivingEntity entity)
    {
        if (this.bloodType == BloodType.NONE) return;

        entity.level.getProfiler().push("entity_blood_tick");

        if (Config.healthOrBloodPoints.get() && entity.getMobType() != MobType.UNDEAD)
        {
            updateBloodHealth(entity);
        }

        if (this.bloodType != BloodType.FRAIL && entity.getMobType() != MobType.UNDEAD && Config.entityRegen.get() && naturalRegen && entity.tickCount % this.ticksPerRegen == 0)
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

            if (Config.healthOrBloodPoints.get() && entity.getMobType() != MobType.UNDEAD)
            {
                updateBloodHealth(entity);
            }
            else
            {
                int lastMaxBloodPoints = this.maxBloodPoints;
                int newMaxBloodPoints = bloodConfig.getBloodPoints();

                //If fresh entity, directly set values
                if (this.freshEntity)
                {
                    this.maxBloodPoints = newMaxBloodPoints;
                    this.bloodPoints = this.maxBloodPoints;
                }
                //If pre existing entity, update only if max blood points in config changed
                else if (newMaxBloodPoints != lastMaxBloodPoints)
                {
                    //Get last ratio of blood points to max
                    this.bloodPoints = (int) (newMaxBloodPoints * ((float) this.bloodPoints / this.maxBloodPoints));
                    this.maxBloodPoints = newMaxBloodPoints;
                }

                this.ticksPerRegen = Config.entityRegenTime.get() / this.maxBloodPoints;
            }
        }

        this.freshEntity = false;
    }

    public boolean isEdible()
    {
        return this.bloodType != BloodType.NONE && this.maxBloodPoints > 0 && this.bloodPoints > 0;
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

    public boolean decreaseBlood(LivingEntity attacker, LivingEntity victim)
    {
        if (isEdible())
        {
            if (Config.healthOrBloodPoints.get() && victim.getMobType() != MobType.UNDEAD)
            {
                float resultingHealth = victim.getHealth() - (1F / this.bloodType.getBloodSaturationModifier());

                if (resultingHealth > 0)
                {
                    victim.setHealth(resultingHealth);
                }
                else
                {
                    attacker.doHurtTarget(victim);
                    victim.hurt(ModRegistry.BLOOD_LOSS, Float.MAX_VALUE);
                }
            }
            else
            {
                int resultingBloodPoints = this.bloodPoints - 1;

                if (victim.getMobType() == MobType.UNDEAD)
                {
                    if (resultingBloodPoints >= 0)
                    {
                        --this.bloodPoints;
                    }
                    else return false;
                }
                else if (resultingBloodPoints > 0)
                {
                    --this.bloodPoints;
                }
                else
                {
                    attacker.doHurtTarget(victim);
                    victim.hurt(ModRegistry.BLOOD_LOSS, Float.MAX_VALUE);
                }
            }

            return true;
        }

        return false;
    }

    public void preventMovement(LivingEntity entity)
    {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 9, false, false, true));
    }

    private void updateBloodHealth(LivingEntity entity)
    {
        this.maxBloodPoints = healthToBlood(entity.getMaxHealth());
        this.bloodPoints = healthToBlood(entity.getHealth());
        this.ticksPerRegen = Config.entityRegenTime.get() / (int) entity.getMaxHealth();
    }

    private int healthToBlood(float health)
    {
        return VampireUtil.healthToBlood(health, this.bloodType);
    }

    public void saveNBTData(CompoundTag tag)
    {
        tag.putString(ID_NBT_NAME, this.id);
        tag.putInt(BloodType.BLOODTYPE_NBT_NAME, this.bloodType.getId());
        tag.putInt(BLOOD_POINTS_NBT_NAME, this.bloodPoints);
        tag.putInt(MAX_BLOOD_POINTS_NBT_NAME, this.maxBloodPoints);
        tag.putBoolean(REGEN_NBT_NAME, this.naturalRegen);
        tag.putInt(REGEN_TIMER_NBT_NAME, this.naturalRegenTimer);
        tag.putBoolean(FRESH_ENTITY_NBT_NAME, this.freshEntity);
    }

    public void loadNBTData(CompoundTag tag)
    {
        this.id = tag.getString(ID_NBT_NAME);
        this.bloodType = VampirismTier.fromId(BloodType.class, tag.getInt(BloodType.BLOODTYPE_NBT_NAME));
        this.bloodPoints = tag.getInt(BLOOD_POINTS_NBT_NAME);
        this.maxBloodPoints = tag.getInt(MAX_BLOOD_POINTS_NBT_NAME);
        this.naturalRegen = tag.getBoolean(REGEN_NBT_NAME);
        this.naturalRegenTimer = tag.getInt(REGEN_TIMER_NBT_NAME);
        this.freshEntity = tag.getBoolean(FRESH_ENTITY_NBT_NAME);
    }
}
