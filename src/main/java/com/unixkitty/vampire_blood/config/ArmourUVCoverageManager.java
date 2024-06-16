package com.unixkitty.vampire_blood.config;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.registries.ForgeRegistries;

public class ArmourUVCoverageManager
{
    public static final float ZERO_COVERAGE = 0F;
    private static final Object2FloatOpenHashMap<ResourceLocation> map = new Object2FloatOpenHashMap<>();

    public static boolean hasEntries()
    {
        return map.size() > 0;
    }

    public static float getCoverage(ArmorItem item)
    {
        return map.getOrDefault(ForgeRegistries.ITEMS.getKey(item), ZERO_COVERAGE);
    }

    static boolean isValidConfigListEntry(Object o)
    {
        if (o instanceof String s && s.contains("|"))
        {
            String[] strings = s.split("\\|", 2);

            ResourceLocation resourceLocation = ResourceLocation.tryParse(strings[0]);

            if (resourceLocation != null)
            {
                try
                {
                    float coveragePercentage = Float.parseFloat(strings[1]);

                    if (coveragePercentage > 0)
                    {
                        return true;
                    }
                }
                catch (NumberFormatException ignored)
                {

                }
            }
        }

        return false;
    }

    static void updateMap()
    {
        map.clear();

        String[] strings;

        for (String entry : Config.armourUVCoveragePercentages.get())
        {
            strings = entry.split("\\|", 2);

            map.put(ResourceLocation.tryParse(strings[0]), Float.parseFloat(strings[1]));
        }
    }
}
