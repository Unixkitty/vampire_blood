package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
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
        AtomicInteger i = new AtomicInteger();
        VampirePlayerData vampirePlayer = player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).orElse(null);

        final int renderStartX = screenWidth - fontRenderer.width(longestLine) - fontRenderer.width("     ");
        final int renderStartY = screenHeight / 3;

        drawLine("thirstLevel: " + vampirePlayer.getThirstLevel() + "/" + VampirePlayerData.Blood.MAX_THIRST, poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.DARK_RED.getColor());
        drawLine("thirstExhaustionLevel: " + vampirePlayer.getThirstExhaustion() + "/100", poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.GRAY.getColor());
        drawLine(longestLine.replace("!!!", Integer.toString(vampirePlayer.getThirstExhaustionIncrement())), poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.GRAY.getColor());
        drawLine("thirstTickTimer: " + vampirePlayer.getThirstTickTimer(), poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.DARK_GRAY.getColor());
        drawLine("isFeeding: " + vampirePlayer.isFeeding(), poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.DARK_RED.getColor());
        drawLine("vampireLevel: " + vampirePlayer.getVampireLevel(), poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.DARK_PURPLE.getColor());
        drawLine("ticksInSun: " + vampirePlayer.getSunTicks(), poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.YELLOW.getColor());
        drawLine("bloodType: " + vampirePlayer.getBloodType(), poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.DARK_RED.getColor());

        drawLine("isMoving: " + ModEvents.isPlayerMoving(player), poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.DARK_GREEN.getColor());
        drawLine("tickCount: " + player.tickCount, poseStack, fontRenderer, renderStartX, renderStartY, i.incrementAndGet(), ChatFormatting.GRAY.getColor());
    }

    private static void drawLine(String text, PoseStack poseStack, Font fontRenderer, int renderStartX, int renderStartY, int lineNumber, int color)
    {
        fontRenderer.drawShadow(poseStack, text, renderStartX, renderStartY + (fontRenderer.lineHeight * lineNumber) + MARGIN_PX, color, false);
    }
}
