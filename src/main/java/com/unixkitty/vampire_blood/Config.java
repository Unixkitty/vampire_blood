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
    public static ForgeConfigSpec.BooleanValue shouldUndeadIgnoreVampires;
    public static ForgeConfigSpec.BooleanValue increasedDamageFromWood;
    public static ForgeConfigSpec.IntValue ticksToSunDamage;
    public static ForgeConfigSpec.IntValue vampireDustDropAmount;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> sunnyDimensions;

    public static ForgeConfigSpec.IntValue naturalHealingRate;
    public static ForgeConfigSpec.DoubleValue naturalHealingMultiplier;
    public static ForgeConfigSpec.IntValue noRegenTicksLimit;

    public static ForgeConfigSpec.IntValue bloodUsageRate;
    public static ForgeConfigSpec.IntValue playerBloodPoints;
    public static ForgeConfigSpec.BooleanValue healthOrBloodPoints;
    public static ForgeConfigSpec.BooleanValue entityRegen;
    public static ForgeConfigSpec.IntValue entityRegenTime;
    /* END ENTRIES */

    /* BEGIN CLIENT CONFIG ENTRIES */
    public static ForgeConfigSpec.BooleanValue renderDebugOverlay;
    public static ForgeConfigSpec.BooleanValue alternateBloodbarFeedingAnimation;
    public static ForgeConfigSpec.BooleanValue detailedEntityBloodHUD;
    public static ForgeConfigSpec.BooleanValue entityBloodHUDshowHP;
    /* END ENTRIES */

    static
    {
        {
            ForgeConfigSpec.Builder commonConfig = new ForgeConfigSpec.Builder();

            commonConfig.push("General");
            {
                shouldUndeadIgnoreVampires = commonConfig.comment("Should undead mobs be neutral to vampires").comment("Requires world restart").worldRestart().define("shouldUndeadIgnoreVampires", true);

                increasedDamageFromWood = commonConfig.comment("Do wooden tools have 1.25x increased damage against vampires").define("increasedDamageFromWood", true);

                ticksToSunDamage = commonConfig.comment("How many ticks in sunlight before pain").defineInRange("ticksToSunDamage", 60, 1, Integer.MAX_VALUE);

                vampireDustDropAmount = commonConfig.comment("How much vampire dust drops on death").defineInRange("vampireDustDropAmount", 2, 0, 64);

                sunnyDimensions = commonConfig.comment("List of dimensions vampires should get sun damage in").defineListAllowEmpty(Lists.newArrayList("sunnyDimensions"), () -> Lists.newArrayList(BuiltinDimensionTypes.OVERWORLD.location().toString()), (potentialEntry) -> potentialEntry instanceof String string && ResourceLocation.isValidResourceLocation(string));
            }
            commonConfig.pop();

            commonConfig.push("Health regen");
            {
                naturalHealingRate = commonConfig.comment("Every N (this value) ticks regenerate 1 health when above 1/6th blood").defineInRange("naturalHealingRate", 20, 1, Integer.MAX_VALUE);
                naturalHealingMultiplier = commonConfig.comment("By default, vampires regenerate their health fully in 20 seconds. This value can multiply this speed. More than 1 will mean faster regen, less than 1 - slower.").defineInRange("naturalHealingMultiplier", 1.0D, 0.001, 100.0D);
                noRegenTicksLimit = commonConfig.comment("Maximum ticks a vampire can't regenerate health for after getting damaged by things vampires are weak to").defineInRange("noRegenTicksLimit", 60, 1, Integer.MAX_VALUE);
            }
            commonConfig.pop();

            commonConfig.push("Blood");
            {
                bloodUsageRate = commonConfig.comment("Base blood usage rate, higher the number == slower usage").defineInRange("bloodUsageRate", 720, 1, Integer.MAX_VALUE);
                playerBloodPoints = commonConfig.comment("Amount of blood points players have").defineInRange("playerBloodPoints", 40, 1, Integer.MAX_VALUE);
                healthOrBloodPoints = commonConfig.comment("Global toggle for whether to tie drinkable blood points directly to entity health or a separate value").comment("true = health, false = separate blood points").comment("Requires world restart").worldRestart().define("healthOrBloodPoints", true);
                entityRegen = commonConfig.comment("Should entities regenerate either their blood points or health, depending on healthOrBloodPoints?").define("entityRegen", true);
                entityRegenTime = commonConfig.comment("How long in ticks it takes an entity to regenerate blood/health to full").defineInRange("entityRegenTime", 24000, 1200, Integer.MAX_VALUE);
            }
            commonConfig.pop();

            COMMON_CONFIG = commonConfig.build();
        }

        {
            ForgeConfigSpec.Builder clientConfig = new ForgeConfigSpec.Builder();

            clientConfig.push("GUI");
            {
                alternateBloodbarFeedingAnimation = clientConfig.comment("Alternate wave animation on bloodbar during feeding").define("alternateBloodbarFeedingAnimation", false);
                detailedEntityBloodHUD = clientConfig.comment("Alternate detailed entity blood HUD with numbers").define("detailedEntityBloodHUD", false);
                entityBloodHUDshowHP = clientConfig.comment("Additionally show entity HP with detailed blood HUD").define("entityBloodHUDshowHP", false);
            }
            clientConfig.pop();

            clientConfig.push("debug");
            {
                renderDebugOverlay = clientConfig.comment("Render debug overlay with some data during gameplay").define("renderDebugOverlay", false);
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
