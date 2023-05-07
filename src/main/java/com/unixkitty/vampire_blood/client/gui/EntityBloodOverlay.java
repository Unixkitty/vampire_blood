package com.unixkitty.vampire_blood.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.capability.player.VampirismStage;
import com.unixkitty.vampire_blood.client.ClientVampirePlayerDataCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.LivingEntity;
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
                && !Minecraft.getInstance().player.isSpectator()
                && ClientVampirePlayerDataCache.vampireLevel.getId() > VampirismStage.NOT_VAMPIRE.getId()
                && Minecraft.getInstance().player.isAlive()
                && !Minecraft.getInstance().options.hideGui
        )
        {
            Minecraft.getInstance().getProfiler().push("entity_blood_overlay");

            if (MouseOverHandler.isLookingAtEdible() && Minecraft.getInstance().hitResult instanceof EntityHitResult)
            {
                int renderStartX = (screenWidth / 2) + MARGIN_PX * 3;
                int renderStartY = (screenHeight / 2) - 3 - gui.getFont().lineHeight;

                poseStack.pushPose();

                ModDebugOverlay.drawLine(MouseOverHandler.bloodType.getTranslation().getString(), poseStack, gui.getFont(), renderStartX, renderStartY, MouseOverHandler.bloodType.getColor());
                ModDebugOverlay.drawLine(MouseOverHandler.bloodPoints + "/" + MouseOverHandler.maxBloodPoints, poseStack, gui.getFont(), renderStartX, renderStartY + gui.getFont().lineHeight, ChatFormatting.DARK_RED);

                //TODO remove debug?
                ModDebugOverlay.drawLine("HP: " + ((LivingEntity)((EntityHitResult) Minecraft.getInstance().hitResult).getEntity()).getHealth() + "/" + ((LivingEntity)((EntityHitResult) Minecraft.getInstance().hitResult).getEntity()).getMaxHealth(), poseStack, gui.getFont(), renderStartX, renderStartY + (gui.getFont().lineHeight * 2), ChatFormatting.RED);

                poseStack.popPose();
            }
            else
            {
                if (MouseOverHandler.hasData())
                {
                    MouseOverHandler.reset();
                }
            }

            Minecraft.getInstance().getProfiler().pop();
        }
    }
}
