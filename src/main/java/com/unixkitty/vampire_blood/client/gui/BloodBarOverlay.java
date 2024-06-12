package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerBloodData;
import com.unixkitty.vampire_blood.client.ClientEvents;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.client.feeding.FeedingMouseOverHandler;
import com.unixkitty.vampire_blood.config.Config;
import com.unixkitty.vampire_blood.util.VampireUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.lwjgl.opengl.GL11;

import static com.unixkitty.vampire_blood.client.ClientEvents.MARGIN_PX;

@OnlyIn(Dist.CLIENT)
public class BloodBarOverlay implements IGuiOverlay
{
    public static final BloodBarOverlay INSTANCE = new BloodBarOverlay();

    protected final RandomSource random = RandomSource.create();
    private final Minecraft minecraft;

    private BloodBarOverlay()
    {
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight)
    {
        if (minecraft.player != null && minecraft.player.isAlive() && !minecraft.options.hideGui && gui.shouldDrawSurvivalElements())
        {
            //Thirst level bar
            if (ClientCache.isVampire() && !(minecraft.player.getVehicle() instanceof LivingEntity))
            {
                minecraft.getProfiler().push("blood_bar_overlay");

                int startX = minecraft.getWindow().getGuiScaledWidth() / 2 + 91;
                int startY = minecraft.getWindow().getGuiScaledHeight() - gui.rightHeight;
                gui.rightHeight += 10;

                if (Config.showBloodbarExhaustionUnderlay.get())
                {
                    int width = (int) (Math.min(1F, Math.max(0F, ClientCache.getVampireVars().thirstExhaustion / 100F)) * 81);

                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.625F);
                    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    //Exhaustion underlay
                    guiGraphics.blit(ClientEvents.ICONS_PNG, startX - width, startY, 126 - width, 0, width, 9);

                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.disableBlend();
                }

                for (int i = 0; i < 10; ++i)
                {
                    int x = startX - i * 8 - 9;
                    int idx = i * 2 + 1;
                    int idx2 = i * 2 + ((VampirePlayerBloodData.MAX_THIRST / 2) + 1);
                    int offsetY = 0;
                    int backgroundOffsetY = 0;

                    //If feeding, instead of jitter at low blood, play wave animation similar to health regeneration
                    if (ClientCache.getVampireVars().feeding)
                    {
                        //Alternate dancing-like animation
                        if (Config.alternateBloodbarFeedingAnimation.get())
                        {
                            offsetY -= (int) (((gui.getGuiTicks() - i) % 10.0) / 5.0);
                        }
                        else if (((gui.getGuiTicks() + (9 - i)) % 10) == 0)
                        {
                            offsetY -= 2;
                            backgroundOffsetY = offsetY;
                        }
                    }
                    //Bar jitter that gets faster with lower blood when below 1/6
                    else if (ClientCache.isHungry() && gui.getGuiTicks() % (ClientCache.getVampireVars().thirstLevel * 9 + 1) == 0)
                    {
                        offsetY -= random.nextInt(3) - 1;
                        backgroundOffsetY = offsetY;
                    }

                    //Background
                    guiGraphics.blit(ClientEvents.ICONS_PNG, x, startY + backgroundOffsetY, 0, 0, 9, 9);

                    //Power of Canada
                    if (idx2 < ClientCache.getVampireVars().thirstLevel)
                    {
                        guiGraphics.blit(ClientEvents.ICONS_PNG, x, startY + offsetY, 36, 0, 9, 9); //Double full
                    }
                    else if (idx2 == ClientCache.getVampireVars().thirstLevel)
                    {
                        guiGraphics.blit(ClientEvents.ICONS_PNG, x, startY + offsetY, 27, 0, 9, 9); //Double half full
                    }
                    else if (idx < ClientCache.getVampireVars().thirstLevel)
                    {
                        guiGraphics.blit(ClientEvents.ICONS_PNG, x, startY + offsetY, 18, 0, 9, 9); //Full
                    }
                    else if (idx == ClientCache.getVampireVars().thirstLevel)
                    {
                        guiGraphics.blit(ClientEvents.ICONS_PNG, x, startY + offsetY, 9, 0, 9, 9); //Half
                    }
                }

                minecraft.getProfiler().pop();
            }

            //Entity blood HUD
            renderEntityBlood(minecraft, gui, guiGraphics, screenWidth, screenHeight);
        }
    }

    private void renderEntityBlood(Minecraft minecraft, ForgeGui gui, GuiGraphics guiGraphics, int screenWidth, int screenHeight)
    {
        if (ClientCache.canFeed())
        {
            minecraft.getProfiler().push("entity_blood_overlay");

            if (FeedingMouseOverHandler.isLookingAtEdible() && minecraft.hitResult instanceof EntityHitResult)
            {
                int renderStartX = (screenWidth / 2) + (MARGIN_PX * 3);
                int renderStartY = screenHeight / 2;

                PoseStack poseStack = guiGraphics.pose();

                if (Config.detailedEntityBloodHUD.get())
                {
                    renderStartY -= (int) (gui.getFont().lineHeight * 1.5);
                    int lineNum = 0;

                    drawLine(FeedingMouseOverHandler.bloodType.getTranslation(), gui, guiGraphics, renderStartX, renderStartY, FeedingMouseOverHandler.bloodType.getChatFormatting());
                    drawLine(FeedingMouseOverHandler.bloodPoints + "/" + FeedingMouseOverHandler.maxBloodPoints, gui, guiGraphics, renderStartX, renderStartY + (gui.getFont().lineHeight * ++lineNum), ChatFormatting.DARK_RED);

                    if (Config.entityBloodHUDshowHP.get())
                    {
                        drawLine(Component.translatable("text.vampire_blood.health", VampireUtil.formatDecimal(FeedingMouseOverHandler.getLastEntity().getHealth()), VampireUtil.formatDecimal(FeedingMouseOverHandler.getLastEntity().getMaxHealth())), gui, guiGraphics, renderStartX, renderStartY + (gui.getFont().lineHeight * ++lineNum), ChatFormatting.RED);
                    }

                    if (FeedingMouseOverHandler.charmedSeconds != -2)
                    {
                        drawLine(Component.translatable("text.vampire_blood.charmed_for", FeedingMouseOverHandler.charmedSeconds), gui, guiGraphics, renderStartX, renderStartY + (gui.getFont().lineHeight * ++lineNum), ChatFormatting.DARK_PURPLE);
                    }
                }
                else
                {
                    renderStartY -= gui.getFont().lineHeight / 2;

                    poseStack.pushPose();

                    RenderSystem.enableBlend();

                    guiGraphics.blit(ClientEvents.ICONS_PNG, renderStartX, renderStartY, getIconIndex(), FeedingMouseOverHandler.bloodType.getId() * 9, 9, 9);

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

            minecraft.getProfiler().pop();
        }
    }

    private int getIconIndex()
    {
        double healthPercentage = (double) FeedingMouseOverHandler.bloodPoints / FeedingMouseOverHandler.maxBloodPoints * 100;

        int iconIndex = FeedingMouseOverHandler.bloodPoints <= 1 ? 0 :
                healthPercentage <= 25 ? 1 :
                        healthPercentage <= 50 ? 2 :
                                healthPercentage <= 90 ? 3 : 4;

        return iconIndex * 9;
    }

    @SuppressWarnings("DataFlowIssue")
    private void drawLine(String text, ForgeGui gui, GuiGraphics guiGraphics, int renderStartX, int renderStartY, ChatFormatting format)
    {
        guiGraphics.drawString(gui.getFont(), text, renderStartX, renderStartY, format.getColor(), true);
    }

    @SuppressWarnings("DataFlowIssue")
    private void drawLine(Component text, ForgeGui gui, GuiGraphics guiGraphics, int renderStartX, int renderStartY, ChatFormatting format)
    {
        guiGraphics.drawString(gui.getFont(), text, renderStartX, renderStartY, format.getColor(), true);
    }
}
