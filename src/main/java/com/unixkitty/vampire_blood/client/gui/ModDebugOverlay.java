package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerBloodData;
import com.unixkitty.vampire_blood.client.ClientEvents;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
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

    public static boolean keyEnabled = false;

    private static boolean isRegistered = false;

    public static void register()
    {
        if (!isRegistered && Config.renderDebugOverlay.get())
        {
            MinecraftForge.EVENT_BUS.addListener(ModDebugOverlay::render);

            isRegistered = true;
        }
    }

    public static void render(final RenderGuiOverlayEvent event)
    {
        if (Config.renderDebugOverlay.get() && keyEnabled && event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) && Minecraft.getInstance().getCameraEntity() instanceof Player player && !player.isSpectator() && !player.isCreative())
        {
            if (Minecraft.getInstance().options.hideGui || Minecraft.getInstance().options.renderDebug) return;

            Minecraft.getInstance().getProfiler().push("vampire_debug_overlay");

            final int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            final int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            PoseStack poseStack = event.getPoseStack();

            poseStack.pushPose();

            //Render stuff here
            renderDebugText(poseStack, Minecraft.getInstance().font, screenWidth, screenHeight, player);

            poseStack.popPose();

            Minecraft.getInstance().getProfiler().pop();
        }
    }

    private static void renderDebugText(PoseStack poseStack, Font fontRenderer, int screenWidth, int screenHeight, Player player)
    {
        buildDrawList(player);

        final int drawWidth = fontRenderer.width(crafter.getLongestLine()) + (ClientEvents.MARGIN_PX * 4);
        final int drawHeight = (fontRenderer.lineHeight * drawList.size()) + (ClientEvents.MARGIN_PX * 3);

        //BACKGROUND BOX
        final int boxStartX = ClientEvents.MARGIN_PX;
        final int boxStartY = ClientEvents.MARGIN_PX;

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

    private static void buildDrawList(Player player)
    {
        drawList.clear();
        crafter.clear();

        craftLine(ChatFormatting.GRAY, "player.tickCount: ", player.tickCount);
        craftLine(ChatFormatting.DARK_PURPLE, "vampireLevel: ", ClientVampirePlayerDataCache.vampireLevel);

        if (ClientVampirePlayerDataCache.isVampire())
        {
            craftLine(ChatFormatting.RED, "bloodType: ", ClientVampirePlayerDataCache.bloodType.getTranslation().getString());
            craftLine(ChatFormatting.DARK_RED, "bloodlust: ", VampireUtil.formatDecimal(ClientVampirePlayerDataCache.bloodlust, 2), "/100");
            craftLine(ChatFormatting.DARK_RED, "thirstLevel: ", ClientVampirePlayerDataCache.thirstLevel, "/", VampirePlayerBloodData.MAX_THIRST);
            craftLine(ChatFormatting.GRAY, "thirstExhaustionLevel: ", ClientVampirePlayerDataCache.thirstExhaustion, "/100");
            craftLine(ChatFormatting.DARK_GRAY, "thirstExhaustionIncrement: ", ClientVampirePlayerDataCache.Debug.thirstExhaustionIncrement, "/", Config.bloodUsageRate.get());
            craftLine(ChatFormatting.GRAY, "thirstTickTimer: ", ClientVampirePlayerDataCache.Debug.thirstTickTimer);
            craftLine(ChatFormatting.DARK_PURPLE, "lookingAtEdible: ", MouseOverHandler.isLookingAtEdible());
            craftLine(ChatFormatting.DARK_GRAY, "isFeeding: ", ClientVampirePlayerDataCache.isFeeding);
            craftLine(ChatFormatting.YELLOW, "ticksInSun: ", ClientVampirePlayerDataCache.Debug.ticksInSun);
            craftLine(ChatFormatting.RED, "Health: ", VampireUtil.formatDecimal(player.getHealth(), 1), "/", player.getMaxHealth(), " | Rate: ", VampireUtil.getHealthRegenRate(player), "/", ClientVampirePlayerDataCache.isHungry() ? Config.naturalHealingRate.get() * 4 : Config.naturalHealingRate.get(), "t");
            craftLine(ChatFormatting.LIGHT_PURPLE, "noRegenTicks: ", ClientVampirePlayerDataCache.Debug.noRegenTicks);
        }

        addAttributes(player);
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
                    craftLine(ChatFormatting.DARK_AQUA, modifier.name(), ": ", VampireUtil.formatDecimal(attributeInstance.getValue(), 2), " ( ", VampireUtil.formatDecimal(attributeInstance.getBaseValue(), 2), " ( ", VampireUtil.formatDecimal(ClientVampirePlayerDataCache.vampireLevel.getAttributeMultiplier(modifier), 2), " * ", VampireUtil.formatDecimal(ClientVampirePlayerDataCache.bloodType.getAttributeMultiplier(modifier), 2), " ) )");
                }
            }
        }
    }

    public static void drawLine(String text, PoseStack poseStack, Font fontRenderer, int renderStartX, int renderStartY, int color)
    {
        fontRenderer.drawShadow(poseStack, text, renderStartX, renderStartY, color, false);
    }

    public static void drawLine(String text, PoseStack poseStack, Font fontRenderer, int renderStartX, int renderStartY, ChatFormatting format)
    {
        fontRenderer.drawShadow(poseStack, text, renderStartX, renderStartY, format.getColor(), false);
    }
}
