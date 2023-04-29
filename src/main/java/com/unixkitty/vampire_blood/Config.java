package com.unixkitty.vampire_blood;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config
{
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    /* BEGIN COMMON CONFIG ENTRIES */
    public static final String BLOOD_USAGE_RATE = "bloodUsageRate";
    public static ForgeConfigSpec.IntValue bloodUsageRate;

    public static final String HEALING_RATE = "naturalHealingRate";
    public static ForgeConfigSpec.IntValue naturalHealingRate;

    public static final String HEALING_MULTIPLIER = "naturalHealingMultiplier";
    public static ForgeConfigSpec.DoubleValue naturalHealingMultiplier;

    public static final String UNDEAD_IGNORE = "shouldUndeadIgnoreVampires";
    public static ForgeConfigSpec.BooleanValue shouldUndeadIgnoreVampires;

    public static final String INCREASED_WOOD_DAMAGE = "increasedDamageFromWood";
    public static ForgeConfigSpec.BooleanValue increasedDamageFromWood;

    public static final String TIME_TO_SUN = "ticksToSunDamage";
    public static ForgeConfigSpec.IntValue ticksToSunDamage;
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
                commonConfig.push("Health regen");
                naturalHealingRate = commonConfig.comment("Every N (this value) ticks regenerate 1 health when above 1/6th blood").defineInRange(HEALING_RATE, 20, 1, Integer.MAX_VALUE);
                naturalHealingMultiplier = commonConfig.comment("By default, vampires regenerate their health fully in 20 seconds. This value can multiply this speed. More than 1 will mean faster regen, less than 1 - slower.").defineInRange(HEALING_MULTIPLIER, 1.0D, 0.001, 100.0D);
                commonConfig.pop();

                shouldUndeadIgnoreVampires = commonConfig.comment("Should undead mobs be neutral to vampires").define(UNDEAD_IGNORE, true);
                increasedDamageFromWood = commonConfig.comment("Do wooden tools have 1.25x increased damage against vampires").define(INCREASED_WOOD_DAMAGE, true);

                ticksToSunDamage = commonConfig.comment("How many ticks in sunlight before pain").defineInRange(TIME_TO_SUN, 60, 1, Integer.MAX_VALUE);
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
}
