package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerBloodData;
import com.unixkitty.vampire_blood.capability.player.VampirismTier;
import com.unixkitty.vampire_blood.client.ClientEvents;
import com.unixkitty.vampire_blood.client.cache.ClientVampirePlayerDataCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.util.StringCrafter;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ModDebugOverlay
{
    private static final StringCrafter crafter = new StringCrafter();
    private static final List<Pair<String, Integer>> drawList = new ArrayList<>();

    public static boolean mainEnabled = false;
    public static SecondaryElement secondaryElement = SecondaryElement.OFF;

    private static boolean registered = false;

    public static void register()
    {
        if (!registered && Config.renderDebugOverlay.get())
        {
            MinecraftForge.EVENT_BUS.addListener(ModDebugOverlay::render);

            registered = true;
        }
    }

    public static void nextSecondaryElement()
    {
        SecondaryElement[] values = SecondaryElement.values();

        secondaryElement = values[(secondaryElement.ordinal() + 1) % values.length];
    }

    public static boolean isMainOverlayEnabled()
    {
        return Config.renderDebugOverlay.get() && mainEnabled;
    }

    public static void render(final RenderGuiOverlayEvent event)
    {
        if (Config.renderDebugOverlay.get() && (mainEnabled || secondaryElement != SecondaryElement.OFF) && event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) && Minecraft.getInstance().getCameraEntity() instanceof Player player && !player.isSpectator() && !player.isCreative())
        {
            if (Minecraft.getInstance().options.hideGui || Minecraft.getInstance().options.renderDebug) return;

            Minecraft.getInstance().getProfiler().push("vampire_debug_overlay");

            final int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            final int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            PoseStack poseStack = event.getPoseStack();

            poseStack.pushPose();

            if (mainEnabled)
            {
                renderDebugText(poseStack, Minecraft.getInstance().font, screenWidth, screenHeight, player);
            }

            if (secondaryElement != SecondaryElement.OFF)
            {
                renderSecondary(poseStack, Minecraft.getInstance().font, screenWidth, screenHeight);
            }

            poseStack.popPose();

            Minecraft.getInstance().getProfiler().pop();
        }
    }

    private static void renderSecondary(PoseStack poseStack, Font fontRenderer, int screenWidth, int screenHeight)
    {
        drawList.clear();
        crafter.clear();

        craftLine(ChatFormatting.WHITE, secondaryElement.name(), ":");

        switch (secondaryElement)
        {
            case DIET ->
            {
                BloodType type;

                for (int i = 0; i < ClientVampirePlayerDataCache.Debug.diet.length; i++)
                {
                    type = VampirismTier.fromId(BloodType.class, ClientVampirePlayerDataCache.Debug.diet[i]);

                    if (type != null)
                    {
                        craftLine(type.getChatFormatting(), i + 1, i < 9 ? ".   " : ".  ", type.getTranslation().getString());
                    }
                }
            }
            case ABILITIES ->
            {
                for (VampireActiveAbility ability : VampireActiveAbility.values())
                {
                    craftLine(ChatFormatting.GRAY, ability.getSimpleName(), ": ", String.valueOf(ClientVampirePlayerDataCache.activeAbilities.contains(ability)));
                }
            }
        }

        drawList(poseStack, fontRenderer, screenWidth, screenHeight, false);
    }

    private static void renderDebugText(PoseStack poseStack, Font fontRenderer, int screenWidth, int screenHeight, Player player)
    {
        drawList.clear();
        crafter.clear();

        craftLine(ChatFormatting.GRAY, "player.tickCount: ", player.tickCount);
        craftLine(ChatFormatting.DARK_PURPLE, "vampireLevel: ", ClientVampirePlayerDataCache.vampireLevel);

        if (ClientVampirePlayerDataCache.isVampire())
        {
            craftLine(ChatFormatting.RED, "bloodType: ", ClientVampirePlayerDataCache.bloodType.getTranslation().getString());
            craftLine(ChatFormatting.GRAY, "bloodPurity: ", VampireUtil.formatPercent100(ClientVampirePlayerDataCache.bloodPurity));

            craftLine(ChatFormatting.DARK_RED, "thirstLevel: ", ClientVampirePlayerDataCache.thirstLevel, "/", VampirePlayerBloodData.MAX_THIRST);
            craftLine(ChatFormatting.DARK_RED, "bloodlust: ", VampireUtil.formatPercent100(ClientVampirePlayerDataCache.bloodlust));
            craftLine(ChatFormatting.GRAY, "thirstExhaustionLevel: ", VampireUtil.formatPercent100(ClientVampirePlayerDataCache.thirstExhaustion));
            craftLine(ChatFormatting.DARK_GRAY, "thirstExhaustionIncrement: ", ClientVampirePlayerDataCache.Debug.thirstExhaustionIncrement, "/", Config.bloodUsageRate.get(), " | Rate: ", VampireUtil.formatDecimal(ClientVampirePlayerDataCache.Debug.thirstExhaustionIncrementRate), "/s");
            craftLine(ChatFormatting.DARK_GRAY, "highestThirstExhaustionIncrement: ", ClientVampirePlayerDataCache.Debug.highestThirstExhaustionIncrement);
            craftLine(ChatFormatting.GRAY, "thirstTickTimer: ", ClientVampirePlayerDataCache.Debug.thirstTickTimer);

            craftLine(ChatFormatting.RED, "Health: ", VampireUtil.formatDecimal(player.getHealth()), "/", VampireUtil.formatDecimal(player.getMaxHealth()), " | Rate: ", VampireUtil.formatDecimal(VampireUtil.getHealthRegenRate(player)), "/", ClientVampirePlayerDataCache.isHungry() ? Config.naturalHealingRate.get() * 4 : Config.naturalHealingRate.get(), "t");
            craftLine(ChatFormatting.LIGHT_PURPLE, "noRegenTicks: ", ClientVampirePlayerDataCache.Debug.noRegenTicks);
        }

        if (ClientVampirePlayerDataCache.canFeed())
        {
            craftLine(ChatFormatting.YELLOW, "ticksInSun: ", ClientVampirePlayerDataCache.Debug.ticksInSun);
            craftLine(ChatFormatting.DARK_PURPLE, "lookingAtEdible: ", FeedingMouseOverHandler.isLookingAtEdible());
            craftLine(ChatFormatting.DARK_GRAY, "feeding: ", ClientVampirePlayerDataCache.feeding);

            addAttributes(player);
        }

        drawList(poseStack, fontRenderer, screenWidth, screenHeight, true);
    }

    @SuppressWarnings("unused")
    private static void drawList(PoseStack poseStack, Font fontRenderer, int screenWidth, int screenHeight, boolean leftOrRight)
    {
        final int drawWidth = fontRenderer.width(crafter.getLongestLine() + 1) + (ClientEvents.MARGIN_PX * 4);
        final int drawHeight = (fontRenderer.lineHeight * drawList.size()) + (ClientEvents.MARGIN_PX * 3);

        //BACKGROUND BOX
        final int boxStartX;
        final int boxStartY;

        if (leftOrRight)
        {
            boxStartX = ClientEvents.MARGIN_PX;
            boxStartY = ClientEvents.MARGIN_PX;
        }
        else
        {
            boxStartX = screenWidth - drawWidth - ClientEvents.MARGIN_PX;
            boxStartY = ClientEvents.MARGIN_PX;
        }

        final int boxEndX = boxStartX + drawWidth - ClientEvents.MARGIN_PX;
        final int boxEndY = boxStartY + drawHeight - ClientEvents.MARGIN_PX;

        ForgeGui.fill(poseStack, boxStartX, boxStartY, boxEndX, boxEndY, Minecraft.getInstance().options.getBackgroundColor(0.4F));

        //TEXT LINES
        final int textStartX = boxStartX + ClientEvents.MARGIN_PX;
        final int textStartY = boxStartY + ClientEvents.MARGIN_PX;

        for (int i = 0; i < drawList.size(); i++)
        {
            var line = drawList.get(i);

            drawLine(line.getLeft(), poseStack, fontRenderer, textStartX, textStartY + (fontRenderer.lineHeight * i), line.getRight());
        }
    }

    private static void craftLine(ChatFormatting formatting, Object... objects)
    {
        drawList.add(crafter.addLine(formatting, objects));
    }

    private static void addAttributes(Player player)
    {
        AttributeInstance attributeInstance;

        for (VampireAttributeModifiers.Modifier modifier : VampireAttributeModifiers.Modifier.values())
        {
            if (modifier.isApplicableStage(ClientVampirePlayerDataCache.vampireLevel))
            {
                attributeInstance = player.getAttribute(modifier.getBaseAttribute());

                if (attributeInstance != null)
                {
                    //mod: currentValue ( vampireLevelMod ( BloodTypeMod * BloodTypePurity ) )
                    craftLine(ChatFormatting.DARK_AQUA, modifier.name(), ": ", VampireUtil.formatDecimal(attributeInstance.getValue()), " ( ", VampireUtil.formatDecimal(ClientVampirePlayerDataCache.vampireLevel.getAttributeMultiplier(modifier)), " * ( ", VampireUtil.formatDecimal(ClientVampirePlayerDataCache.bloodType.getAttributeMultiplier(modifier)), " * ", VampireUtil.formatDecimal(ClientVampirePlayerDataCache.bloodPurity), " ) )");
                }
            }
        }
    }

    private static void drawLine(String text, PoseStack poseStack, Font fontRenderer, int renderStartX, int renderStartY, int color)
    {
        fontRenderer.drawShadow(poseStack, text, renderStartX, renderStartY, color, false);
    }

    public enum SecondaryElement
    {
        OFF,
        DIET,
        ABILITIES
    }
}
