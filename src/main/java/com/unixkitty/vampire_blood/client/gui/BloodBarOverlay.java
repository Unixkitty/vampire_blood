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
                && !(gui.getMinecraft().player.getVehicle() instanceof LivingEntity) && !gui.getMinecraft().options.hideGui
        )
        {
            VampirePlayerData vampirePlayer = Minecraft.getInstance().player.getCapability(VampirePlayerProvider.VAMPIRE_PLAYER).orElse(null);

            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, BLOODBAR_TEXTURES);

            int startX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
            int startY = Minecraft.getInstance().getWindow().getGuiScaledHeight() - ((ForgeGui) Minecraft.getInstance().gui).rightHeight;
            ((ForgeGui) Minecraft.getInstance().gui).rightHeight += 10;
            int blood = vampirePlayer.getThirstLevel();
            int maxBlood = VampirePlayerData.Blood.MAX_THIRST;

            for (int i = 0; i < 10; ++i)
            {
                int x = startX - i * 8 - 9;
                int idx = i * 2 + 1;
                int idx2 = i * 2 + ((maxBlood / 2) + 1); // Duuur

                //TODO add more minor animations with different blood levels
                //Bar jitter that gets faster the lower the blood when below 1/6
                if (blood < maxBlood / 6 && Minecraft.getInstance().gui.getGuiTicks() % (blood * 9 + 1) == 0)
                {
                    startY = startY + (random.nextInt(3) - 1);
                }

                //Background
                blit(poseStack, x, startY, 0, 0, 9, 9);

                //Power of Canada
                if (idx2 < blood)
                {
                    blit(poseStack, x, startY, 36, 0, 9, 9); //Double full
                }
                else if (idx2 == blood)
                {
                    blit(poseStack, x, startY, 27, 0, 9, 9); //Double half full
                }
                else if (idx < blood)
                {
                    blit(poseStack, x, startY, 18, 0, 9, 9); //Full
                }
                else if (idx == blood)
                {
                    blit(poseStack, x, startY, 9, 0, 9, 9); //Half
                }
            }

            //Is this needed?
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            RenderSystem.disableBlend();
        }
    }
}
