package com.unixkitty.vampire_blood;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = VampireBlood.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    /* BEGIN COMMON CONFIG ENTRIES */
    private static final String BLOOD_USAGE_RATE = "bloodUsageRate";
    public static ForgeConfigSpec.IntValue bloodUsageRate;

    private static final String HEALING_RATE = "naturalHealingRate";
    public static ForgeConfigSpec.IntValue naturalHealingRate;

    private static final String HEALING_MULTIPLIER = "naturalHealingMultiplier";
    public static ForgeConfigSpec.DoubleValue naturalHealingMultiplier;

    private static final String UNDEAD_IGNORE = "shouldUndeadIgnoreVampires";
    public static ForgeConfigSpec.BooleanValue shouldUndeadIgnoreVampires;

    private static final String INCREASED_WOOD_DAMAGE = "increasedDamageFromWood";
    public static ForgeConfigSpec.BooleanValue increasedDamageFromWood;

    private static final String TIME_TO_SUN = "ticksToSunDamage";
    public static ForgeConfigSpec.IntValue ticksToSunDamage;

    private static final String SUNNY_DIMENSIONS = "sunnyDimensions";
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> sunnyDimensions;

    private static final String NO_REGEN_TICKS = "noRegenTicksLimit";
    public static ForgeConfigSpec.IntValue noRegenTicksLimit;

    private static final String VAMPIRE_DUST_DROPS = "vampireDustDropAmount";
    public static ForgeConfigSpec.IntValue vampireDustDropAmount;
    /* END ENTRIES */

    /* BEGIN CLIENT CONFIG ENTRIES */
    private static final String DEBUG_OUTPUT = "debugOutput";
    public static ForgeConfigSpec.BooleanValue debugOutput;

    private static final String RENDER_DEBUG_OVERLAY = "renderDebugOverlay";
    public static ForgeConfigSpec.BooleanValue renderDebugOverlay;

    private static final String ALTERNATE_BLOODBAR_FEEDING = "alternateBloodbarFeedingAnimation";
    public static ForgeConfigSpec.BooleanValue alternateBloodbarFeedingAnimation;
    /* END ENTRIES */

    static
    {
        {
            ForgeConfigSpec.Builder commonConfig = new ForgeConfigSpec.Builder();

            commonConfig.push("General");
            {
                shouldUndeadIgnoreVampires = commonConfig.comment("Should undead mobs be neutral to vampires").define(UNDEAD_IGNORE, true);

                increasedDamageFromWood = commonConfig.comment("Do wooden tools have 1.25x increased damage against vampires").define(INCREASED_WOOD_DAMAGE, true);

                ticksToSunDamage = commonConfig.comment("How many ticks in sunlight before pain").defineInRange(TIME_TO_SUN, 60, 1, Integer.MAX_VALUE);

                vampireDustDropAmount = commonConfig.comment("How much vampire dust drops on death").defineInRange(VAMPIRE_DUST_DROPS, 2, 0, 64);

                sunnyDimensions = commonConfig.comment("List of dimensions vampires should get sun damage in").defineListAllowEmpty(Lists.newArrayList(SUNNY_DIMENSIONS), () -> Lists.newArrayList(BuiltinDimensionTypes.OVERWORLD.location().toString()), (potentialEntry) -> potentialEntry instanceof String string && ResourceLocation.isValidResourceLocation(string));
            }
            commonConfig.pop();

            commonConfig.push("Health regen");
            {
                naturalHealingRate = commonConfig.comment("Every N (this value) ticks regenerate 1 health when above 1/6th blood").defineInRange(HEALING_RATE, 20, 1, Integer.MAX_VALUE);
                naturalHealingMultiplier = commonConfig.comment("By default, vampires regenerate their health fully in 20 seconds. This value can multiply this speed. More than 1 will mean faster regen, less than 1 - slower.").defineInRange(HEALING_MULTIPLIER, 1.0D, 0.001, 100.0D);
                noRegenTicksLimit = commonConfig.comment("Maximum ticks a vampire can't regenerate health for after getting damaged by things vampires are weak to").defineInRange(NO_REGEN_TICKS, 60, 1, Integer.MAX_VALUE);
            }
            commonConfig.pop();

            commonConfig.push("Blood");
            {
                bloodUsageRate = commonConfig.comment("Base blood usage rate, higher the number == slower usage").defineInRange(BLOOD_USAGE_RATE, 720, 1, Integer.MAX_VALUE);
            }
            commonConfig.pop();

            COMMON_CONFIG = commonConfig.build();
        }

        {
            ForgeConfigSpec.Builder clientConfig = new ForgeConfigSpec.Builder();

            clientConfig.push("GUI");
            {
                alternateBloodbarFeedingAnimation = clientConfig.comment("Alternate wave animation on bloodbar during feeding").define(ALTERNATE_BLOODBAR_FEEDING, false);
            }
            clientConfig.pop();

            clientConfig.push("debug");
            {
                debugOutput = clientConfig.comment("Print verbose debug info to chat").define(DEBUG_OUTPUT, false);
                renderDebugOverlay = clientConfig.comment("Render debug overlay with some data during gameplay").define(RENDER_DEBUG_OVERLAY, false);
            }
            clientConfig.pop();

            CLIENT_CONFIG = clientConfig.build();
        }
    }

    private static void reload(ModConfig config, ModConfig.Type type)
    {
        switch (type)
        {
            case CLIENT -> CLIENT_CONFIG.setConfig(config.getConfigData());
            case COMMON -> COMMON_CONFIG.setConfig(config.getConfigData());
        }
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event)
    {
        if (!event.getConfig().getModId().equals(VampireBlood.MODID)) return;

        reload(event.getConfig(), event.getConfig().getType());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading event)
    {
        if (!event.getConfig().getModId().equals(VampireBlood.MODID)) return;

        reload(event.getConfig(), event.getConfig().getType());
    }
}
