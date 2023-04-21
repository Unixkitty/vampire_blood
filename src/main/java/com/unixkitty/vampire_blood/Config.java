package com.unixkitty.vampire_blood;

import net.minecraftforge.common.ForgeConfigSpec;

@SuppressWarnings("CanBeFinal")
public class Config
{
    public static boolean isDebug = true;
    public static boolean renderDebugOverlay = true;

    public static ForgeConfigSpec COMMON_CONFIG;
    //public static ForgeConfigSpec CLIENT_CONFIG; This will be needed for client-specific options

    /* BEGIN ENTRIES */
    public static final String BLOOD_USAGE_RATE = "bloodUsageRate";
    public static ForgeConfigSpec.IntValue bloodUsageRate;

    public static final String NATURAL_REGEN = "naturalHealthRegen";
    public static ForgeConfigSpec.BooleanValue naturalHealthRegen;

    public static final String NATURAL_REGEN_GAMERULE = "naturalHealthRegenWithGamerule";
    public static ForgeConfigSpec.BooleanValue naturalHealthRegenWithGamerule;

    public static final String HEALING_RATE = "naturalHealingRate";
    public static ForgeConfigSpec.IntValue naturalHealingRate;
    /* END ENTRIES */

    static
    {
        ForgeConfigSpec.Builder commonConfig = new ForgeConfigSpec.Builder();

        commonConfig.push("Blood");

        bloodUsageRate = commonConfig.comment("Base blood usage rate, the higher the number, the slower is usage").defineInRange(BLOOD_USAGE_RATE, 720, 1, Integer.MAX_VALUE);
        naturalHealthRegen = commonConfig.comment("Should vampires regenerate health naturally").define(NATURAL_REGEN, true);
        naturalHealthRegenWithGamerule = commonConfig.comment("Should only regenerate health naturally if vanilla gamerule allows").define(NATURAL_REGEN_GAMERULE, false);
        naturalHealingRate = commonConfig.comment("Every N (this value) ticks regenerate 1 health").defineInRange(HEALING_RATE, 10, 1, Integer.MAX_VALUE);

        commonConfig.pop();

        COMMON_CONFIG = commonConfig.build();
    }
}
