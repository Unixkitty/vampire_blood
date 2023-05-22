package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerBloodData;
import com.unixkitty.vampire_blood.client.ClientEvents;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class BloodBarOverlay extends GuiComponent implements IGuiOverlay
{
    public static final BloodBarOverlay INSTANCE = new BloodBarOverlay();

    protected final RandomSource random = RandomSource.create();

    private BloodBarOverlay()
    {
    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight)
    {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isAlive() && !Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements())
        {
            //Thirst level bar
            if (ClientCache.isVampire() && !(Minecraft.getInstance().player.getVehicle() instanceof LivingEntity))
            {
                Minecraft.getInstance().getProfiler().push("blood_bar_overlay");

                RenderSystem.setShaderTexture(0, ClientEvents.ICONS_PNG);

                int startX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
                int startY = Minecraft.getInstance().getWindow().getGuiScaledHeight() - gui.rightHeight;
                gui.rightHeight += 10;

                if (Config.showBloodbarExhaustionUnderlay.get())
                {
                    int width = (int) (Math.min(1F, Math.max(0F, ClientCache.getVampireVars().thirstExhaustion / 100F)) * 81);

                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.625F);
                    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    //Exhaustion underlay
                    blit(poseStack, startX - width, startY, 126 - width, 0, width, 9);

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
                            offsetY -= ((gui.getGuiTicks() - i) % 10.0) / 5.0;
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
                    blit(poseStack, x, startY + backgroundOffsetY, 0, 0, 9, 9);

                    //Power of Canada
                    if (idx2 < ClientCache.getVampireVars().thirstLevel)
                    {
                        blit(poseStack, x, startY + offsetY, 36, 0, 9, 9); //Double full
                    }
                    else if (idx2 == ClientCache.getVampireVars().thirstLevel)
                    {
                        blit(poseStack, x, startY + offsetY, 27, 0, 9, 9); //Double half full
                    }
                    else if (idx < ClientCache.getVampireVars().thirstLevel)
                    {
                        blit(poseStack, x, startY + offsetY, 18, 0, 9, 9); //Full
                    }
                    else if (idx == ClientCache.getVampireVars().thirstLevel)
                    {
                        blit(poseStack, x, startY + offsetY, 9, 0, 9, 9); //Half
                    }
                }

                RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

                Minecraft.getInstance().getProfiler().pop();
            }

            //Entity blood HUD
            EntityBloodOverlay.render(gui, poseStack, partialTick, screenWidth, screenHeight);
        }
    }


}
