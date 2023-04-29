package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.VampireUtil;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.attribute.VampireAttributeModifiers;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

public class ModDebugOverlay
{
    private static final int MARGIN_PX = 5;

    public static void onRenderText(RenderGuiOverlayEvent event)
    {
        if (Config.renderDebugOverlay.get() && event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) && Minecraft.getInstance().getCameraEntity() instanceof Player player && !player.isSpectator() && !player.isCreative())
        {
            if (Minecraft.getInstance().options.renderDebug) return;

            final int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            final int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            PoseStack poseStack = event.getPoseStack();

            poseStack.pushPose();

            //Render stuff here
            renderDebugText(poseStack, Minecraft.getInstance().font, screenWidth, screenHeight, player);

            poseStack.popPose();
        }
    }

    private static void renderDebugText(PoseStack poseStack, Font fontRenderer, int screenWidth, int screenHeight, Player player)
    {
//        String longestLine = "thirstExhaustionIncrement: " + "!!!" + "/" + Config.bloodUsageRate.get();
        int i = 0;

//        final int renderStartX = screenWidth - fontRenderer.width(longestLine) - fontRenderer.width("     ");
        final int renderStartX = MARGIN_PX;
        final int renderStartY = MARGIN_PX;

        final int fillRenderStartX = renderStartX / 2;
        final int fillRenderStartY = renderStartY * 3;

        ForgeGui.fill(poseStack, fillRenderStartX, fillRenderStartY, fillRenderStartX + 200, fillRenderStartY + 115, Minecraft.getInstance().options.getBackgroundColor(0.3F));

        drawLine("vampireLevel: " + ClientVampirePlayerDataCache.vampireLevel, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_PURPLE.getColor());

        if (ClientVampirePlayerDataCache.isVampire())
        {
            AttributeInstance attributeInstance;

            drawLine("thirstLevel: " + ClientVampirePlayerDataCache.thirstLevel + "/" + VampirePlayerData.Blood.MAX_THIRST, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_RED.getColor());
            drawLine("thirstExhaustionLevel: " + ClientVampirePlayerDataCache.Debug.thirstExhaustion + "/100", poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.GRAY.getColor());
            drawLine("thirstExhaustionIncrement: " + ClientVampirePlayerDataCache.Debug.thirstExhaustionIncrement + "/" + Config.bloodUsageRate.get(), poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.GRAY.getColor());
            drawLine("thirstTickTimer: " + ClientVampirePlayerDataCache.Debug.thirstTickTimer, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_GRAY.getColor());
            drawLine("isFeeding: " + ClientVampirePlayerDataCache.isFeeding, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_GRAY.getColor());
            drawLine("ticksInSun: " + ClientVampirePlayerDataCache.Debug.ticksInSun, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.YELLOW.getColor());
            drawLine("bloodType: " + ClientVampirePlayerDataCache.bloodType, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_RED.getColor());
            drawLine("Health: " + VampireUtil.formatDecimal(player.getHealth(), 1) + "/" + player.getMaxHealth() + " | Rate: " + VampireUtil.getHealthRegenRate(player) + "/" + Config.naturalHealingRate.get() + "t", poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.RED.getColor());

            for (VampireAttributeModifiers.Modifier modifier : VampireAttributeModifiers.Modifier.values())
            {
                attributeInstance = player.getAttribute(modifier.getBaseAttribute());

                if (attributeInstance != null)
                {
                    drawLine(modifier.name() + ": " + attributeInstance.getValue() + " ( " + attributeInstance.getBaseValue() + " ( " + VampireUtil.formatDecimal(ClientVampirePlayerDataCache.vampireLevel.getAttributeMultiplier(modifier), 2) + " * " + VampireUtil.formatDecimal(ClientVampirePlayerDataCache.bloodType.getAttributeMultiplier(modifier), 2) + " ) ", poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_AQUA.getColor());
                }
            }
        }
        
        drawLine("player.tickCount: " + player.tickCount, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.GRAY.getColor());
    }

    private static void drawLine(String text, PoseStack poseStack, Font fontRenderer, int renderStartX, int renderStartY, int lineNumber, int color)
    {
        fontRenderer.drawShadow(poseStack, text, renderStartX, renderStartY + (fontRenderer.lineHeight * lineNumber) + MARGIN_PX, color, false);
    }
}
