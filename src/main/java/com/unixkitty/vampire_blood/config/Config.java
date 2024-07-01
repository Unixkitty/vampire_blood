package com.unixkitty.vampire_blood.config;

import com.google.common.collect.Lists;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerBloodData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = VampireBlood.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    /* BEGIN CLIENT CONFIG ENTRIES */
    private static final ForgeConfigSpec CLIENT_CONFIG;
    public static final ForgeConfigSpec.BooleanValue renderDebugOverlay;
    public static final ForgeConfigSpec.BooleanValue alternateBloodbarFeedingAnimation;
    public static final ForgeConfigSpec.BooleanValue showBloodbarExhaustionUnderlay;
    public static final ForgeConfigSpec.BooleanValue detailedEntityBloodHUD;
    public static final ForgeConfigSpec.BooleanValue entityBloodHUDshowHP;

    public static final ForgeConfigSpec.BooleanValue clipMouseToRadialMenu;
    public static final ForgeConfigSpec.BooleanValue allowRadialMenuClickOutsideBounds;
    public static final ForgeConfigSpec.BooleanValue releaseRadialMenuButtonToActivate;
    /* END ENTRIES */

    /* BEGIN COMMON CONFIG ENTRIES */
    private static final ForgeConfigSpec COMMON_CONFIG;
    public static final ForgeConfigSpec.BooleanValue debug;
    /* END ENTRIES */

    /* BEGIN SERVER CONFIG ENTRIES */
    private static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> armourUVCoveragePercentages;

    public static final ForgeConfigSpec.BooleanValue shouldUndeadIgnoreVampires;
    public static final ForgeConfigSpec.BooleanValue increasedDamageFromWood;
    public static final ForgeConfigSpec.IntValue ticksToSunDamage;
    public static final ForgeConfigSpec.IntValue vampireDustDropAmount;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> sunnyDimensions;

    public static final ForgeConfigSpec.IntValue naturalHealingRate;
    public static final ForgeConfigSpec.DoubleValue naturalHealingMultiplier;
    public static final ForgeConfigSpec.IntValue noRegenTicksLimit;

    public static final ForgeConfigSpec.IntValue bloodUsageRate;
    public static final ForgeConfigSpec.BooleanValue healthOrBloodPoints;
    public static final ForgeConfigSpec.BooleanValue entityRegen;
    public static final ForgeConfigSpec.IntValue entityRegenTime;
    public static final ForgeConfigSpec.IntValue bloodPointsFromBottles;

    public static final ForgeConfigSpec.IntValue abilityHungerThreshold;
    public static final ForgeConfigSpec.IntValue charmEffectDuration;
    public static final ForgeConfigSpec.IntValue vampireBloodEffectDuration;
    public static final ForgeConfigSpec.IntValue transitionTime;
    /* END ENTRIES */

    static
    {
        {
            ForgeConfigSpec.Builder clientConfig = new ForgeConfigSpec.Builder();

            clientConfig.push("GUI");
            {
                alternateBloodbarFeedingAnimation = clientConfig.comment("Alternate wave animation on bloodbar during feeding").define("alternateBloodbarFeedingAnimation", false);
                showBloodbarExhaustionUnderlay = clientConfig.comment("Render exhaustion meter under the bloodbar like in Apple Skin mod").define("showBloodbarExhaustionUnderlay", true);
                detailedEntityBloodHUD = clientConfig.comment("Alternate detailed entity blood HUD with numbers").define("detailedEntityBloodHUD", false);
                entityBloodHUDshowHP = clientConfig.comment("Additionally show entity HP with detailed blood HUD").define("entityBloodHUDshowHP", false);

                clientConfig.push("Ability wheel");
                clipMouseToRadialMenu = clientConfig.comment("When ability wheel is open, restrict mouse movement to be within the wheel's bounds?").define("clipMouseToRadialMenu", false);
                allowRadialMenuClickOutsideBounds = clientConfig.define("allowRadialMenuClickOutsideBounds", false);
                releaseRadialMenuButtonToActivate = clientConfig.comment("Should releasing the ability wheel button activate chosen ability?").define("releaseRadialMenuButtonToActivate", true);
                clientConfig.pop();
            }
            clientConfig.pop();

            clientConfig.push("Debug");
            {
                renderDebugOverlay = clientConfig.comment("Render debug overlay with some data during gameplay").define("renderDebugOverlay", false);
            }
            clientConfig.pop();

            CLIENT_CONFIG = clientConfig.build();
        }

        {
            ForgeConfigSpec.Builder commonConfig = new ForgeConfigSpec.Builder();

            commonConfig.push("General");
            {
                debug = commonConfig.comment("Enable debug features (lots of network traffic and additional HUDs)").define("debug", false);
            }
            commonConfig.pop();

            COMMON_CONFIG = commonConfig.build();
        }

        {
            ForgeConfigSpec.Builder serverConfig = new ForgeConfigSpec.Builder();

            serverConfig.push("General");
            {
                shouldUndeadIgnoreVampires = serverConfig.comment("Should undead mobs be neutral to vampires").comment("Requires world restart").worldRestart().define("shouldUndeadIgnoreVampires", true);
                increasedDamageFromWood = serverConfig.comment("Do wooden tools have 2x increased damage against vampires").define("increasedDamageFromWood", true);
                ticksToSunDamage = serverConfig.comment("How many ticks in sunlight before pain").defineInRange("ticksToSunDamage", 60, 1, Integer.MAX_VALUE);
                vampireDustDropAmount = serverConfig.comment("Up to how much vampire dust drops on death").defineInRange("vampireDustDropAmount", 2, 0, 64);
                vampireBloodEffectDuration = serverConfig.comment("How long in ticks does the effect after drinking vampire blood last for humans").defineInRange("vampireBloodEffectDuration", 12000, -1, 72000);
                transitionTime = serverConfig.comment("How long in ticks will players get to transition to full vampirism before dying and reverting back").defineInRange("transitionTime", 24000, 600, Integer.MAX_VALUE);
                sunnyDimensions = serverConfig
                        .comment("List of dimensions vampires should get sun damage in")
                        .defineListAllowEmpty("sunnyDimensions", () -> Lists.newArrayList(BuiltinDimensionTypes.OVERWORLD.location().toString()), (potentialEntry) -> potentialEntry instanceof String string && ResourceLocation.isValidResourceLocation(string));
                armourUVCoveragePercentages = serverConfig
                        .comment("List of armour items to assign some UV coverage percentages. Only actual armour items will be checked in code")
                        .comment("For example if you assign 4 armour pieces 25% coverage each and then wear them together - you'll achieve full coverage from the sun and will no longer be in danger in daylight as long as you keep yourself covered")
                        .comment("Will need to re-equip items if changing the config on a live server")
                        .comment("Example entry: \"minecraft:diamond_leggings|0.25\"")
                        .defineListAllowEmpty("armourUVCoveragePercentages", Lists::newArrayList, ArmourUVCoverageManager::isValidConfigListEntry);
            }
            serverConfig.pop();

            serverConfig.push("Balance");
            {
                serverConfig.push("Health regen");
                {
                    naturalHealingRate = serverConfig.comment("Every N (this value) ticks regenerate health when above 1/6th blood").defineInRange("naturalHealingRate", 20, 1, Integer.MAX_VALUE);
                    naturalHealingMultiplier = serverConfig.comment("By default, vampires regenerate their health fully in 20 seconds. This value can multiply this speed. More than 1 will mean faster regen, less than 1 - slower.").defineInRange("naturalHealingMultiplier", 1.0D, 0.001, 100.0D);
                    noRegenTicksLimit = serverConfig.comment("Maximum ticks a vampire can't regenerate health for after getting damaged by things vampires are weak to").defineInRange("noRegenTicksLimit", 100, 1, Integer.MAX_VALUE);
                }
                serverConfig.pop();

                serverConfig.push("Blood");
                {
                    bloodUsageRate = serverConfig.comment("Base blood usage rate, higher the number == slower usage").defineInRange("bloodUsageRate", 720, 1, Integer.MAX_VALUE);
                    healthOrBloodPoints = serverConfig.comment("Global toggle for whether to tie drinkable blood points directly to entity health or a separate value").comment("true = health, false = separate blood points").comment("Except undead, which always use blood points, and non-vampire players who always use health").comment("Requires world restart").worldRestart().define("healthOrBloodPoints", true);
                    entityRegen = serverConfig.comment("Should entities regenerate either their blood points or health, depending on healthOrBloodPoints?").define("entityRegen", false);
                    entityRegenTime = serverConfig.comment("How long in ticks it takes an entity to regenerate blood/health to full").defineInRange("entityRegenTime", 24000, 1200, Integer.MAX_VALUE);
                    bloodPointsFromBottles = serverConfig.comment("How many blood points are restored per bottle").defineInRange("bloodPointsFromBottles", 4, 1, VampirePlayerBloodData.MAX_THIRST);
                }
                serverConfig.pop();

                serverConfig.push("Abilities");
                {
                    abilityHungerThreshold = serverConfig.comment("Hunger level at which abilities can no longer be used by a vampire player").defineInRange("abilityHungerThreshold", VampirePlayerBloodData.MAX_THIRST / 20, 0, VampirePlayerBloodData.MAX_THIRST);
                    charmEffectDuration = serverConfig.comment("How many ticks charm effect lasts on targets, -1 for unlimited duration").defineInRange("charmEffectDuration", 1200, -1, Integer.MAX_VALUE);
                }
                serverConfig.pop();
            }
            serverConfig.pop();

            SERVER_CONFIG = serverConfig.build();
        }
    }

    private static void reload(ModConfig config, ModConfig.Type type)
    {
        switch (type)
        {
            case CLIENT -> CLIENT_CONFIG.setConfig(config.getConfigData());
            case COMMON -> COMMON_CONFIG.setConfig(config.getConfigData());
            case SERVER ->
            {
                SERVER_CONFIG.setConfig(config.getConfigData());
                ArmourUVCoverageManager.updateMap();
            }
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event)
    {
        if (!event.getConfig().getModId().equals(VampireBlood.MODID)) return;

        reload(event.getConfig(), event.getConfig().getType());
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading event)
    {
        if (!event.getConfig().getModId().equals(VampireBlood.MODID)) return;

        reload(event.getConfig(), event.getConfig().getType());
    }

    public static void register(ModLoadingContext modLoadingContext)
    {
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
    }
}
