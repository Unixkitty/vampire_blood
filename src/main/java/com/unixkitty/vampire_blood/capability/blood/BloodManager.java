package com.unixkitty.vampire_blood.capability.blood;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unixkitty.vampire_blood.VampireBlood;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodManager
{
    private static final String configName = "blood.json";

    private static final Map<String, BloodEntityConfig> bloodMap = new HashMap<>();

    private static boolean initialized = false;

    public static void loadConfig()
    {
        if (initialized) return;

        initialized = true;

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
        catch (Exception e)
        {
            VampireBlood.log().error("IO error with: " + file, e);
        }

        if (bloodMap.isEmpty()) mapList(getDefaultList());
    }

    public static BloodEntityConfig getConfigFor(String id)
    {
        return bloodMap.getOrDefault(id, null);
    }

    private static void readBloodConfig(File file) throws Exception
    {
        VampireBlood.log().debug("Reading " + file.getName());

        Gson gson = new Gson();
        String json = Files.readString(file.toPath());

        BloodEntityListHolder entities = gson.fromJson(json, BloodEntityListHolder.class);

        mapList(entities);

        VampireBlood.log().debug("Finished reading " + file.getName());
    }

    private static void makeNewBloodConfig(File file) throws Exception
    {
        VampireBlood.log().debug("Creating new " + file.getName());

        PrintWriter writer = new PrintWriter(file);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        writer.print(gson.toJson(getDefaultList()));
        writer.close();
    }

    private static void mapList(BloodEntityListHolder holder)
    {
        bloodMap.clear();

        for (BloodEntityConfig config : holder.entities)
        {
            ResourceLocation resourceLocation = config.getResourceId();

            if (resourceLocation != null)
            {
                bloodMap.put(resourceLocation.getPath(), config);
            }
        }
    }

    private static BloodEntityListHolder getDefaultList()
    {
        VampireBlood.log().debug("Creating default entity blood list");

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

    @SuppressWarnings("FieldMayBeFinal")
    public static class BloodEntityListHolder
    {
        private List<BloodEntityConfig> entities;

        private BloodEntityListHolder(List<BloodEntityConfig> entities)
        {
            this.entities = entities;
        }
    }
}
