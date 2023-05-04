package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import static com.unixkitty.vampire_blood.client.ClientEvents.MARGIN_PX;

@OnlyIn(Dist.CLIENT)
public class EntityBloodOverlay extends GuiComponent implements IGuiOverlay
{
    public static EntityBloodOverlay INSTANCE = new EntityBloodOverlay();

    private EntityBloodOverlay() {}

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight)
    {
        if (Minecraft.getInstance().player != null
                && ClientVampirePlayerDataCache.vampireLevel.getId() > VampirismStage.NOT_VAMPIRE.getId()
                && Minecraft.getInstance().player.isAlive()
                && !Minecraft.getInstance().options.hideGui
        )
        {
            Minecraft.getInstance().getProfiler().push("entity_blood_overlay");

            if (!(Minecraft.getInstance().hitResult instanceof EntityHitResult))
            {
                MouseOverHandler.reset();
            }
            else if (MouseOverHandler.bloodType != BloodType.NONE)
            {
                int renderStartX = (screenWidth / 2) + MARGIN_PX * 2;
                int renderStartY = (screenHeight / 2) - 3;

                poseStack.pushPose();

                ModDebugOverlay.drawLine("BloodType: " + MouseOverHandler.bloodType.toString().toLowerCase(), poseStack, gui.getFont(), renderStartX, renderStartY, ChatFormatting.LIGHT_PURPLE.getColor());
                ModDebugOverlay.drawLine("Blood: " + MouseOverHandler.bloodPoints + "/" + MouseOverHandler.maxBloodPoints, poseStack, gui.getFont(), renderStartX, renderStartY + gui.getFont().lineHeight, ChatFormatting.RED.getColor());

                poseStack.popPose();
            }

            Minecraft.getInstance().getProfiler().pop();
        }
    }
}
