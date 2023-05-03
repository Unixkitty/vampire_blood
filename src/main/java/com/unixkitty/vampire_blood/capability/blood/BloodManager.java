package com.unixkitty.vampire_blood.capability.blood;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unixkitty.vampire_blood.VampireBlood;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodManager
{
    private static final String configName = "blood.json";

    private static final Map<ResourceLocation, BloodEntityConfig> bloodMap = new HashMap<>();

    public static void loadConfig()
    {
        String configDir = FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(VampireBlood.MODID), VampireBlood.MODID).toString();

        File file = FileUtils.getFile(configDir, configName);

        try
        {
            if (!FileUtils.directoryContains(new File(configDir), file))
            {
                makeNewBloodConfig(file);
            }

            readBloodConfig(file);
        }
        catch (IOException e)
        {
            VampireBlood.log().error("IO error with directory: " + configDir, e);

            mapList(getDefaultList());
        }
    }

    private static void readBloodConfig(File file) throws IOException
    {
        VampireBlood.log().debug("Reading " + file.getName());

        Gson gson = new Gson();
        String json = Files.readString(file.toPath());

        BloodEntityListHolder entities = gson.fromJson(json, BloodEntityListHolder.class);

        mapList(entities);
    }

    private static void makeNewBloodConfig(File file)
    {
        VampireBlood.log().debug("Creating new " + file.getName());

        PrintWriter writer;

        try
        {
            writer = new PrintWriter(file);
        }
        catch (FileNotFoundException e) //This is thrown if file can't be created either
        {
            VampireBlood.log().error("Error creating new blood config", e);

            return;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        writer.print(gson.toJson(getDefaultList()));
        writer.close();
    }

    private static void mapList(BloodEntityListHolder holder)
    {
        bloodMap.clear();

        for (BloodEntityConfig config : holder.entities())
        {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(config.id);

            if (resourceLocation != null)
            {
                bloodMap.put(resourceLocation, config);
            }
        }
    }

    private static BloodEntityListHolder getDefaultList()
    {
        List<BloodEntityConfig> list = new ArrayList<>();

        list.add(new BloodEntityConfig("minecraft:axolotl", "creature", 7, true));
        list.add(new BloodEntityConfig("minecraft:bat", "creature", 3, true));
        list.add(new BloodEntityConfig("minecraft:cat", "creature", 5, true));
        list.add(new BloodEntityConfig("minecraft:chicken", "creature", 2, true));
        list.add(new BloodEntityConfig("minecraft:cow", "creature", 5, true));
        list.add(new BloodEntityConfig("minecraft:dolphin", "creature", 5, true));
        list.add(new BloodEntityConfig("minecraft:donkey", "creature", 26, true));
        list.add(new BloodEntityConfig("minecraft:evoker", "human", 24, true));
        list.add(new BloodEntityConfig("minecraft:fox", "creature", 5, true));
        list.add(new BloodEntityConfig("minecraft:frog", "creature", 5, true));
        list.add(new BloodEntityConfig("minecraft:goat", "creature", 5, true));
        list.add(new BloodEntityConfig("minecraft:hoglin", "creature", 20, true));
        list.add(new BloodEntityConfig("minecraft:horse", "creature", 26, true));
        list.add(new BloodEntityConfig("minecraft:husk", "frail", 6, false));
        list.add(new BloodEntityConfig("minecraft:illusioner", "human", 32, true));
        list.add(new BloodEntityConfig("minecraft:llama", "creature", 26, true));
        list.add(new BloodEntityConfig("minecraft:mooshroom", "creature", 5, true));
        list.add(new BloodEntityConfig("minecraft:mule", "creature", 26, true));
        list.add(new BloodEntityConfig("minecraft:ocelot", "creature", 5, true));
        list.add(new BloodEntityConfig("minecraft:panda", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:parrot", "creature", 3, true));
        list.add(new BloodEntityConfig("minecraft:pig", "creature", 5, true));
        list.add(new BloodEntityConfig("minecraft:piglin", "piglin", 12, true));
        list.add(new BloodEntityConfig("minecraft:piglin_brute", "piglin", 37, true));
        list.add(new BloodEntityConfig("minecraft:pillager", "human", 24, true));
        list.add(new BloodEntityConfig("minecraft:polar_bear", "creature", 15, true));
        list.add(new BloodEntityConfig("minecraft:rabbit", "creature", 1, true));
        list.add(new BloodEntityConfig("minecraft:ravager", "creature", 50, true));
        list.add(new BloodEntityConfig("minecraft:sheep", "creature", 4, true));
        list.add(new BloodEntityConfig("minecraft:trader_llama", "creature", 26, true));
        list.add(new BloodEntityConfig("minecraft:villager", "human", 20, true));
        list.add(new BloodEntityConfig("minecraft:vindicator", "human", 24, true));
        list.add(new BloodEntityConfig("minecraft:wandering_trader", "human", 20, true));
        list.add(new BloodEntityConfig("minecraft:witch", "human", 26, true));
        list.add(new BloodEntityConfig("minecraft:wolf", "creature", 4, true));
        list.add(new BloodEntityConfig("minecraft:zoglin", "frail", 13, false));
        list.add(new BloodEntityConfig("minecraft:zombie", "frail", 6, false));
        list.add(new BloodEntityConfig("minecraft:zombie_horse", "frail", 5, false));
        list.add(new BloodEntityConfig("minecraft:zombie_villager", "frail", 6, false));
        list.add(new BloodEntityConfig("minecraft:zombified_piglin", "frail", 6, false));

        return new BloodEntityListHolder(list);
    }

    public record BloodEntityListHolder(List<BloodEntityConfig> entities)
    {
    }

    public record BloodEntityConfig(String id, String bloodType, int bloodPoints, boolean naturalRegen)
    {
        @Nullable
        public ResourceLocation getId()
        {
            return ResourceLocation.tryParse(this.id);
        }

        public BloodType getBloodType()
        {
            try
            {
                return BloodType.valueOf(bloodType.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                VampireBlood.log().error(e);

                return BloodType.NONE;
            }
        }
    }
}
