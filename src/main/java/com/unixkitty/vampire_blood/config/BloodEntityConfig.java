package com.unixkitty.vampire_blood.config;

import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

@SuppressWarnings("FieldMayBeFinal")
public class BloodEntityConfig
{
    private String id;
    private String bloodType;
    private int bloodPoints;
    private boolean naturalRegen;

    BloodEntityConfig(String id, String bloodType, int bloodPoints, boolean naturalRegen)
    {
        this.id = id;
        this.bloodType = bloodType;
        this.bloodPoints = bloodPoints;
        this.naturalRegen = naturalRegen;
    }

    public boolean isNaturalRegen()
    {
        return this.naturalRegen;
    }

    public int getBloodPoints()
    {
        return this.bloodPoints;
    }

    public String getId()
    {
        return this.id;
    }

    @Nullable
    public ResourceLocation getResourceId()
    {
        return ResourceLocation.tryParse(this.id);
    }

    public BloodType getBloodType()
    {
        try
        {
            return BloodType.valueOf(this.bloodType.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            VampireBlood.LOG.error("Error parsing blood type from config for: " + id, e);

            return BloodType.NONE;
        }
    }
}
