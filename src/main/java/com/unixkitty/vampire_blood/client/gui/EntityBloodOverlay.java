package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.client.ClientEvents;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import static com.unixkitty.vampire_blood.client.ClientEvents.MARGIN_PX;

@OnlyIn(Dist.CLIENT)
public class EntityBloodOverlay
{
    private EntityBloodOverlay() {}

    public static void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight)
    {
        if (ClientVampirePlayerDataCache.canFeed())
        {
            Minecraft.getInstance().getProfiler().push("entity_blood_overlay");

            if (FeedingMouseOverHandler.isLookingAtEdible() && Minecraft.getInstance().hitResult instanceof EntityHitResult)
            {
                int renderStartX = (screenWidth / 2) + (MARGIN_PX * 3);
                int renderStartY = screenHeight / 2;

                if (Config.detailedEntityBloodHUD.get())
                {
                    renderStartY -= gui.getFont().lineHeight * 1.5;

                    ModDebugOverlay.drawLine(FeedingMouseOverHandler.bloodType.getTranslation().getString(), poseStack, gui.getFont(), renderStartX, renderStartY, FeedingMouseOverHandler.bloodType.getChatFormatting());
                    ModDebugOverlay.drawLine(FeedingMouseOverHandler.bloodPoints + "/" + FeedingMouseOverHandler.maxBloodPoints, poseStack, gui.getFont(), renderStartX, renderStartY + gui.getFont().lineHeight, ChatFormatting.DARK_RED);

                    if (Config.entityBloodHUDshowHP.get())
                    {
                        ModDebugOverlay.drawLine("HP: " + VampireUtil.formatDecimal(((LivingEntity) ((EntityHitResult) Minecraft.getInstance().hitResult).getEntity()).getHealth()) + "/" + VampireUtil.formatDecimal(((LivingEntity) ((EntityHitResult) Minecraft.getInstance().hitResult).getEntity()).getMaxHealth()), poseStack, gui.getFont(), renderStartX, renderStartY + (gui.getFont().lineHeight * 2), ChatFormatting.RED);
                    }
                }
                else
                {
                    renderStartY -= gui.getFont().lineHeight / 2;

                    poseStack.pushPose();

                    RenderSystem.enableBlend();
                    RenderSystem.setShaderTexture(0, ClientEvents.ICONS_PNG);

                    gui.blit(poseStack, renderStartX, renderStartY, getIconIndex(), FeedingMouseOverHandler.bloodType.getId() * 9, 9, 9);

                    RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                    RenderSystem.disableBlend();

                    poseStack.popPose();
                }
            }
            else
            {
                if (FeedingMouseOverHandler.hasData())
                {
                    FeedingMouseOverHandler.reset();
                }
            }

            Minecraft.getInstance().getProfiler().pop();
        }
    }

    private static int getIconIndex()
    {
        double healthPercentage = (double) FeedingMouseOverHandler.bloodPoints / FeedingMouseOverHandler.maxBloodPoints * 100;

        int iconIndex = FeedingMouseOverHandler.bloodPoints <= 1 ? 0 :
                healthPercentage <= 25 ? 1 :
                        healthPercentage <= 50 ? 2 :
                                healthPercentage <= 90 ? 3 : 4;

        return iconIndex * 9;
    }
}
