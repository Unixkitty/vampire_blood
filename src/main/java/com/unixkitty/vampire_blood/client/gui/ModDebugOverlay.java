package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import com.unixkitty.vampire_blood.init.ModEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

import java.util.concurrent.atomic.AtomicInteger;

public class ModDebugOverlay
{
    private static boolean drawDebugGui = true;
    private static final int MARGIN_PX = 5;

//    private static int longestLineWidth = 0;

    public static void onRenderText(RenderGuiOverlayEvent event)
    {
        if (drawDebugGui && event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) && Minecraft.getInstance().getCameraEntity() instanceof Player player && !player.isSpectator())
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
        String longestLine = "thirstExhaustionIncrement: " + "!!!" + "/" + Config.bloodUsageRate.get();
        VampirePlayerData vampirePlayer = player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).orElse(null);

        int i = 0;

        final int renderStartX = screenWidth - fontRenderer.width(longestLine) - fontRenderer.width("     ");
        final int renderStartY = screenHeight / 3;

        drawLine("thirstLevel: " + ClientVampirePlayerDataCache.thirstLevel + "/" + VampirePlayerData.Blood.MAX_THIRST, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_RED.getColor());
        drawLine("thirstExhaustionLevel: " + vampirePlayer.getThirstExhaustion() + "/100", poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.GRAY.getColor());
        drawLine(longestLine.replace("!!!", Integer.toString(vampirePlayer.getThirstExhaustionIncrement())), poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.GRAY.getColor());
        drawLine("thirstTickTimer: " + vampirePlayer.getThirstTickTimer(), poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_GRAY.getColor());
        drawLine("isFeeding: " + ClientVampirePlayerDataCache.isFeeding, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_RED.getColor());
        drawLine("vampireLevel: " + ClientVampirePlayerDataCache.vampireLevel, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_PURPLE.getColor());
        drawLine("ticksInSun: " + vampirePlayer.getSunTicks(), poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.YELLOW.getColor());
        drawLine("bloodType: " + ClientVampirePlayerDataCache.bloodType, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.DARK_RED.getColor());
        
        drawLine("tickCount: " + player.tickCount, poseStack, fontRenderer, renderStartX, renderStartY, ++i, ChatFormatting.GRAY.getColor());
    }

    private static void drawLine(String text, PoseStack poseStack, Font fontRenderer, int renderStartX, int renderStartY, int lineNumber, int color)
    {
        fontRenderer.drawShadow(poseStack, text, renderStartX, renderStartY + (fontRenderer.lineHeight * lineNumber) + MARGIN_PX, color, false);
    }
}
