package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.Config;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class BloodBarOverlay extends GuiComponent implements IGuiOverlay
{
    public static final BloodBarOverlay INSTANCE = new BloodBarOverlay();

    protected final RandomSource random = RandomSource.create();

    private static final ResourceLocation BLOODBAR_TEXTURES = new ResourceLocation(VampireBlood.MODID, "textures/gui/icons.png");

    private BloodBarOverlay() {}

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight)
    {
        if (Minecraft.getInstance().player != null
                && ClientVampirePlayerDataCache.isVampire()
                && gui.shouldDrawSurvivalElements()
                && Minecraft.getInstance().player.isAlive()
                && !(Minecraft.getInstance().player.getVehicle() instanceof LivingEntity) && !Minecraft.getInstance().options.hideGui
        )
        {
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, BLOODBAR_TEXTURES);

            int startX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
            int startY = Minecraft.getInstance().getWindow().getGuiScaledHeight() - gui.rightHeight;
            gui.rightHeight += 10;

            for (int i = 0; i < 10; ++i)
            {
                int x = startX - i * 8 - 9;
                int idx = i * 2 + 1;
                int idx2 = i * 2 + ((VampirePlayerData.Blood.MAX_THIRST / 2) + 1);
                int offsetY = 0;
                int backgroundOffsetY = 0;

                //TODO add more minor animations with different blood levels
                //If feeding, instead of jitter at low blood, play wave animation similar to health regeneration
                if (ClientVampirePlayerDataCache.isFeeding)
                {
                    //Dancing
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
                else if (ClientVampirePlayerDataCache.thirstLevel < VampirePlayerData.Blood.MAX_THIRST / 6 && gui.getGuiTicks() % (ClientVampirePlayerDataCache.thirstLevel * 9 + 1) == 0)
                {
                    offsetY -= random.nextInt(3) - 1;
                    backgroundOffsetY = offsetY;
                }

                //Background
                blit(poseStack, x, startY + backgroundOffsetY, 0, 0, 9, 9);

                //Power of Canada
                if (idx2 < ClientVampirePlayerDataCache.thirstLevel)
                {
                    blit(poseStack, x, startY + offsetY, 36, 0, 9, 9); //Double full
                }
                else if (idx2 == ClientVampirePlayerDataCache.thirstLevel)
                {
                    blit(poseStack, x, startY + offsetY, 27, 0, 9, 9); //Double half full
                }
                else if (idx < ClientVampirePlayerDataCache.thirstLevel)
                {
                    blit(poseStack, x, startY + offsetY, 18, 0, 9, 9); //Full
                }
                else if (idx == ClientVampirePlayerDataCache.thirstLevel)
                {
                    blit(poseStack, x, startY + offsetY, 9, 0, 9, 9); //Half
                }
            }

            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            RenderSystem.disableBlend();
        }
    }
}
