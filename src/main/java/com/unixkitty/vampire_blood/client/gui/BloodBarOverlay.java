package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.VampireBlood;
import com.unixkitty.vampire_blood.capability.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.VampirePlayerProvider;
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

    public int regenBloodWave = 0;
    private boolean tickZeroClamped = false;

    private int lastGuiTick = 0;

    private static final ResourceLocation BLOODBAR_TEXTURES = new ResourceLocation(VampireBlood.MODID, "textures/gui/icons.png");

    private BloodBarOverlay()
    {

    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight)
    {
        if (Minecraft.getInstance().player != null
                && VampirePlayerData.isVampire(Minecraft.getInstance().player)
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
            int blood = Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(VampirePlayerData::getThirstLevel).orElse(0);
            int maxBlood = VampirePlayerData.Blood.MAX_THIRST;

            if (lastGuiTick != gui.getGuiTicks())
            {
                lastGuiTick = gui.getGuiTicks();

                if (!tickZeroClamped)
                {
                    if (Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).map(VampirePlayerData::isFeeding).orElse(false) && regenBloodWave <= 0)
                    {
                        regenBloodWave = 20;
                    }
                    else if (regenBloodWave > 0)
                    {
                        regenBloodWave--;
                    }

                    tickZeroClamped = true;
                }
            }
            else
            {
                tickZeroClamped = false;
            }

            for (int i = 0; i < 10; ++i)
            {
                int x = startX - i * 8 - 9;
                int idx = i * 2 + 1;
                int idx2 = i * 2 + ((maxBlood / 2) + 1);
                int offsetY = 0;

                //TODO add more minor animations with different blood levels
                //If feeding, don't need to jitter at low blood
                //We check over 10 instead of 0 to treat 0~10 as a 'rest' period and 11~20 as active
                //Calculate which icon to offset
                if (regenBloodWave > 0 && regenBloodWave > 10 && (20 - regenBloodWave) == i)
                {
                    offsetY -= 2;
                }
                //Bar jitter that gets faster the lower the blood when below 1/6
                else if (blood < maxBlood / 6 && gui.getGuiTicks() % (blood * 9 + 1) == 0)
                {
                    offsetY -= random.nextInt(3) - 1;
                }

                //Background
                blit(poseStack, x, startY + offsetY, 0, 0, 9, 9);

                //Power of Canada
                if (idx2 < blood)
                {
                    blit(poseStack, x, startY + offsetY, 36, 0, 9, 9); //Double full
                }
                else if (idx2 == blood)
                {
                    blit(poseStack, x, startY + offsetY, 27, 0, 9, 9); //Double half full
                }
                else if (idx < blood)
                {
                    blit(poseStack, x, startY + offsetY, 18, 0, 9, 9); //Full
                }
                else if (idx == blood)
                {
                    blit(poseStack, x, startY + offsetY, 9, 0, 9, 9); //Half
                }
            }

            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            RenderSystem.disableBlend();
        }
    }
}
