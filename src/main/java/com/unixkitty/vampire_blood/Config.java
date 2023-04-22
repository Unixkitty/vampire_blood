package com.unixkitty.vampire_blood;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config
{
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    /* BEGIN COMMON CONFIG ENTRIES */
    public static final String BLOOD_USAGE_RATE = "bloodUsageRate";
    public static ForgeConfigSpec.IntValue bloodUsageRate;

    public static final String NATURAL_REGEN = "naturalHealthRegen";
    public static ForgeConfigSpec.BooleanValue naturalHealthRegen;

    public static final String NATURAL_REGEN_GAMERULE = "naturalHealthRegenWithGamerule";
    public static ForgeConfigSpec.BooleanValue naturalHealthRegenWithGamerule;

    public static final String HEALING_RATE = "naturalHealingRate";
    public static ForgeConfigSpec.IntValue naturalHealingRate;
    /* END ENTRIES */

    /* BEGIN CLIENT CONFIG ENTRIES */
    public static final String DEBUG_OUTPUT = "debugOutput";
    public static ForgeConfigSpec.BooleanValue debugOutput;

    public static final String RENDER_DEBUG_OVERLAY = "renderDebugOverlay";
    public static ForgeConfigSpec.BooleanValue renderDebugOverlay;

    public static final String ALTERNATE_BLOODBAR_FEEDING = "alternateBloodbarFeedingAnimation";
    public static ForgeConfigSpec.BooleanValue alternateBloodbarFeedingAnimation;
    /* END ENTRIES */

    static
    {
        {
            ForgeConfigSpec.Builder commonConfig = new ForgeConfigSpec.Builder();

            commonConfig.push("General");
            {
                naturalHealthRegen = commonConfig.comment("Should vampires regenerate health naturally").define(NATURAL_REGEN, true);
                naturalHealthRegenWithGamerule = commonConfig.comment("Should only regenerate health naturally if vanilla gamerule allows").define(NATURAL_REGEN_GAMERULE, false);
                naturalHealingRate = commonConfig.comment("Every N (this value) ticks regenerate 1 health when above 1/6th blood").defineInRange(HEALING_RATE, 20, 1, Integer.MAX_VALUE);
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
                renderDebugOverlay = clientConfig.comment("Render debug overlay with some data during gameplay").define(RENDER_DEBUG_OVERLAY, true);
            }
            clientConfig.pop();

            CLIENT_CONFIG = clientConfig.build();
        }
    }
}
