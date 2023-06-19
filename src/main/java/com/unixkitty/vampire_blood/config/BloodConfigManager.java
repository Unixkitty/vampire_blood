package com.unixkitty.vampire_blood.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.provider.BloodProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class BloodConfigManager
{
    private static final String configName = "blood.json";

    private static final Object2ObjectOpenHashMap<String, BloodEntityConfig> bloodMap = new Object2ObjectOpenHashMap<>();

    private static boolean initialized = false;
    private static boolean working = false;

    public static void init()
    {
        if (initialized) return;

        initialized = true;

        try
        {
            if (!loadConfig(null))
            {
                VampireBlood.LOG.error("Blood config was already being loaded during initialization!");
            }
        }
        catch (Exception e)
        {
            fail(e);
        }
    }

    public static boolean loadConfig(@Nullable MinecraftServer server) throws Exception
    {
        if (!working)
        {
            working = true;

            ObjectObjectImmutablePair<File, File> config = getConfigFile();
            boolean failedNewConfig = false;

            if (!FileUtils.directoryContains(config.left(), config.right()))
            {
                try
                {
                    makeNewBloodConfig(config.right());
                }
                catch (Exception e)
                {
                    VampireBlood.LOG.error("Failed to create new blood config!", e);
                    failedNewConfig = true;
                }
            }

            if (!failedNewConfig)
            {
                readBloodConfig(config.right());
            }

            if (bloodMap.isEmpty()) mapList(getDefaultList());

            if (server != null)
            {
                VampireBlood.LOG.info("Updating blood values for all entities on the server...");

                for (ServerLevel level : server.getAllLevels())
                {
                    for (Entity entity : level.getAllEntities())
                    {
                        if (entity instanceof LivingEntity livingEntity)
                        {
                            livingEntity.getCapability(BloodProvider.BLOOD_STORAGE).ifPresent(bloodEntityStorage -> bloodEntityStorage.updateBlood(livingEntity));
                        }
                    }
                }

                VampireBlood.LOG.info("Done updating blood values for all entities on the server!");
            }

            working = false;
        }

        return !working;
    }

    public static boolean removeEntry(String id, @Nonnull MinecraftServer server)
    {
        return updateEntry(id, BloodType.NONE, 0, false, server);
    }

    public static boolean updateEntry(String id, BloodType bloodType, int bloodPoints, boolean naturalRegen, @Nonnull MinecraftServer server)
    {
        if (!working)
        {
            working = true;

            ResourceLocation test = ResourceLocation.tryParse(id);

            if (test != null)
            {
                if (bloodType == BloodType.NONE)
                {
                    bloodMap.remove(id);
                }
                else
                {
                    bloodMap.put(id, new BloodEntityConfig(id, bloodType.toString().toLowerCase(), bloodPoints, naturalRegen));
                }

                ObjectObjectImmutablePair<File, File> config = getConfigFile();

                try
                {
                    saveBloodConfig(config.right());
                }
                catch (Exception e)
                {
                    VampireBlood.LOG.error("Failed to save modified blood config!", e);
                }
            }

            working = false;

            try
            {
                loadConfig(server);
            }
            catch (Exception e)
            {
                fail(e);
            }
        }

        return !working;
    }

    public static BloodEntityConfig getConfigFor(String id)
    {
        return bloodMap.getOrDefault(id, null);
    }

    private static void fail(Exception e)
    {
        VampireBlood.LOG.error("IO error when loading blood config!", e);
    }

    private static ObjectObjectImmutablePair<File, File> getConfigFile()
    {
        String configDir = FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(VampireBlood.MODID), VampireBlood.MODID).toString();

        return new ObjectObjectImmutablePair<>(new File(configDir), FileUtils.getFile(configDir, configName));
    }

    private static void readBloodConfig(File file) throws Exception
    {
        VampireBlood.LOG.debug("Reading " + file.getName());

        Gson gson = new Gson();
        String json = Files.readString(file.toPath());

        BloodEntityListHolder entities = gson.fromJson(json, BloodEntityListHolder.class);

        mapList(entities);

        VampireBlood.LOG.debug("Finished reading " + file.getName());
    }

    private static void makeNewBloodConfig(File file) throws Exception
    {
        VampireBlood.LOG.debug("Creating new " + file.getName());

        PrintWriter writer = new PrintWriter(file);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        writer.print(gson.toJson(getDefaultList()));
        writer.close();
    }

    private static void saveBloodConfig(File file) throws Exception
    {
        VampireBlood.LOG.debug("Saving " + file.getName());

        PrintWriter writer = new PrintWriter(file);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        writer.print(gson.toJson(new BloodEntityListHolder(new ArrayList<>(bloodMap.values()))));
        writer.close();
    }

    private static void mapList(BloodEntityListHolder holder)
    {
        bloodMap.clear();

        for (BloodEntityConfig config : holder.getEntities())
        {
            ResourceLocation resourceLocation = config.getResourceId();

            if (resourceLocation != null)
            {
                bloodMap.put(resourceLocation.toString(), config);
            }
        }
    }

    private static BloodEntityListHolder getDefaultList()
    {
        VampireBlood.LOG.debug("Creating default entity blood list");

        List<BloodEntityConfig> list = new ArrayList<>();

        list.add(new BloodEntityConfig("minecraft:axolotl", "creature", 14, true));
        list.add(new BloodEntityConfig("minecraft:bat", "creature", 6, true));
        list.add(new BloodEntityConfig("minecraft:cat", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:chicken", "creature", 4, true));
        list.add(new BloodEntityConfig("minecraft:cow", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:dolphin", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:donkey", "creature", 53, true));
        list.add(new BloodEntityConfig("minecraft:evoker", "human", 48, true));
        list.add(new BloodEntityConfig("minecraft:fox", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:frog", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:goat", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:hoglin", "creature", 40, true));
        list.add(new BloodEntityConfig("minecraft:horse", "creature", 53, true));
        list.add(new BloodEntityConfig("minecraft:husk", "frail", 7, false));
        list.add(new BloodEntityConfig("minecraft:illusioner", "human", 64, true));
        list.add(new BloodEntityConfig("minecraft:llama", "creature", 53, true));
        list.add(new BloodEntityConfig("minecraft:mooshroom", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:mule", "creature", 53, true));
        list.add(new BloodEntityConfig("minecraft:ocelot", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:panda", "creature", 20, true));
        list.add(new BloodEntityConfig("minecraft:parrot", "creature", 6, true));
        list.add(new BloodEntityConfig("minecraft:pig", "creature", 10, true));
        list.add(new BloodEntityConfig("minecraft:piglin", "piglin", 24, true));
        list.add(new BloodEntityConfig("minecraft:piglin_brute", "piglin", 75, true));
        list.add(new BloodEntityConfig("minecraft:pillager", "human", 48, true));
        list.add(new BloodEntityConfig("minecraft:polar_bear", "creature", 30, true));
        list.add(new BloodEntityConfig("minecraft:rabbit", "creature", 3, true));
        list.add(new BloodEntityConfig("minecraft:ravager", "creature", 100, true));
        list.add(new BloodEntityConfig("minecraft:sheep", "creature", 8, true));
        list.add(new BloodEntityConfig("minecraft:trader_llama", "creature", 53, true));
        list.add(new BloodEntityConfig("minecraft:villager", "human", 40, true));
        list.add(new BloodEntityConfig("minecraft:vindicator", "human", 48, true));
        list.add(new BloodEntityConfig("minecraft:wandering_trader", "human", 40, true));
        list.add(new BloodEntityConfig("minecraft:witch", "human", 52, true));
        list.add(new BloodEntityConfig("minecraft:wolf", "creature", 8, true));
        list.add(new BloodEntityConfig("minecraft:zoglin", "frail", 14, false));
        list.add(new BloodEntityConfig("minecraft:zombie", "frail", 7, false));
        list.add(new BloodEntityConfig("minecraft:zombie_horse", "frail", 5, false));
        list.add(new BloodEntityConfig("minecraft:zombie_villager", "frail", 7, false));
        list.add(new BloodEntityConfig("minecraft:zombified_piglin", "frail", 7, false));

        return new BloodEntityListHolder(list);
    }
}
