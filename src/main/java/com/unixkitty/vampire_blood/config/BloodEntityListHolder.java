package com.unixkitty.vampire_blood.config;

import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class BloodEntityListHolder
{
    private List<BloodEntityConfig> entities;

    BloodEntityListHolder(List<BloodEntityConfig> entities)
    {
        this.entities = entities;
    }

    public List<BloodEntityConfig> getEntities()
    {
        return this.entities;
    }
}
