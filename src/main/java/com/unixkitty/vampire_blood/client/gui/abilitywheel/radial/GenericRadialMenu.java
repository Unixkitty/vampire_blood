package com.unixkitty.vampire_blood.client.gui.abilitywheel.radial;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.unixkitty.vampire_blood.config.Config;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

public class GenericRadialMenu
{
    private static final float PRECISION = 2.5f / 360.0f;
    private static final double TWO_PI = 2.0 * Math.PI;

    public final IRadialMenuHost host;
    private final ObjectArrayList<AbilityRadialMenuItem> menuItems = new ObjectArrayList<>();
    private final Minecraft minecraft;

    public float radiusIn;
    public float radiusOut;
    public float itemRadius;
    private boolean open = true;

    public GenericRadialMenu(Minecraft minecraft, IRadialMenuHost host)
    {
        this.minecraft = minecraft;
        this.host = host;
    }

    @Nullable
    public AbilityRadialMenuItem getHoveredItem()
    {
        for (AbilityRadialMenuItem item : this.menuItems)
        {
            if (item.isHovered())
            {
                return item;
            }
        }

        return null;
    }

    public void setHovered(int which)
    {
        for (int i = 0; i < this.menuItems.size(); i++)
        {
            this.menuItems.get(i).setHovered(i == which);
        }
    }

    public int getVisibleItemCount()
    {
        return this.menuItems.size();
    }

    public void clickItem()
    {
        if (this.open)
        {
            AbilityRadialMenuItem item = getHoveredItem();

            if (item != null)
            {
                item.onClick();

                return;
            }
        }

        onClickOutside();
    }

    public void onClickOutside()
    {
        // to be implemented by users
    }

    public boolean isClosed()
    {
        return !this.open;
    }

    public boolean isReady()
    {
        return this.open;
    }

    public void add(AbilityRadialMenuItem item)
    {
        this.menuItems.add(item);
    }

    public void close()
    {
        this.open = false;
    }

    public void draw(PoseStack matrixStack, int mouseX, int mouseY)
    {
        if (isClosed())
        {
            return;
        }

        if (isReady())
        {
            processMouse(mouseX, mouseY);
        }

        Screen owner = this.host.getScreen();
        Font fontRenderer = this.host.getFontRenderer();

        this.radiusIn = 30;
        this.radiusOut = this.radiusIn * 2;
        this.itemRadius = (this.radiusIn + this.radiusOut) * 0.5f;

        int x = owner.width / 2;
        int y = owner.height / 2;

        matrixStack.pushPose();
        matrixStack.translate(0, 0, 0);

        drawBackground(x, y, this.radiusIn, this.radiusOut);

        matrixStack.popPose();

        if (isReady())
        {
            matrixStack.pushPose();
            drawItems(matrixStack, x, y, owner.width, owner.height, fontRenderer);
            matrixStack.popPose();

            Component currentCentralText = null;

            for (AbilityRadialMenuItem item : this.menuItems)
            {
                if (item.isHovered())
                {
                    currentCentralText = item.getCentralText();

                    break;
                }
            }

            if (currentCentralText != null)
            {
                String text = currentCentralText.getString();
                float textX = (owner.width - fontRenderer.width(text)) / 2.0f;
                float textY = (owner.height - fontRenderer.lineHeight) / 2.0f;

                fontRenderer.drawShadow(matrixStack, text, textX, textY, 0xFFFFFFFF);
            }
        }
    }

    private void drawItems(PoseStack matrixStack, int x, int y, int width, int height, Font font)
    {
        iterateVisible((item, s, e) -> {
            float middle = (s + e) * 0.5f;
            float posX = x + this.itemRadius * (float) Math.cos(middle);
            float posY = y + this.itemRadius * (float) Math.sin(middle);

            DrawingContext context = new DrawingContext(matrixStack, width, height, posX, posY, 0, font);

            item.draw(context);
        });
    }

    private void iterateVisible(TriConsumer<AbilityRadialMenuItem, Float, Float> consumer)
    {
        int numItems = this.menuItems.size();

        for (int i = 0; i < numItems; i++)
        {
            float s = (float) getAngleFor(i - 0.5, numItems);
            float e = (float) getAngleFor(i + 0.5, numItems);
            AbilityRadialMenuItem item = this.menuItems.get(i);

            consumer.accept(item, s, e);
        }
    }

    private void drawBackground(float x, float y, float radiusIn, float radiusOut)
    {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        iterateVisible((item, s, e) -> {
            int color = item.isHovered() ? 0x3FFFFFFF : 0x3F000000;
            drawPieArc(buffer, x, y, radiusIn, radiusOut, s, e, color);
        });
        tessellator.end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private void drawPieArc(BufferBuilder buffer, float x, float y, float radiusIn, float radiusOut, float startAngle, float endAngle, int color)
    {
        float angle = endAngle - startAngle;
        int sections = Math.max(1, Mth.ceil(angle / PRECISION));
        angle = endAngle - startAngle;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;
        int a = (color >> 24) & 0xFF;

        float slice = angle / sections;

        for (int i = 0; i < sections; i++)
        {
            float angle1 = startAngle + i * slice;
            float angle2 = startAngle + (i + 1) * slice;

            float pos1InX = x + radiusIn * (float) Math.cos(angle1);
            float pos1InY = y + radiusIn * (float) Math.sin(angle1);
            float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
            float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
            float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
            float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
            float pos2InX = x + radiusIn * (float) Math.cos(angle2);
            float pos2InY = y + radiusIn * (float) Math.sin(angle2);

            buffer.vertex(pos1OutX, pos1OutY, 0).color(r, g, b, a).endVertex();
            buffer.vertex(pos1InX, pos1InY, 0).color(r, g, b, a).endVertex();
            buffer.vertex(pos2InX, pos2InY, 0).color(r, g, b, a).endVertex();
            buffer.vertex(pos2OutX, pos2OutY, 0).color(r, g, b, a).endVertex();
        }
    }

    private void processMouse(int mouseX, int mouseY)
    {
        if (!isReady())
        {
            return;
        }

        int numItems = getVisibleItemCount();
        Screen owner = this.host.getScreen();
        int x = owner.width / 2;
        int y = owner.height / 2;
        double a = Math.atan2(mouseY - y, mouseX - x);
        double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));

        if (numItems > 0)
        {
            double s0 = getAngleFor(0 - 0.5, numItems);
            double s1 = getAngleFor(numItems - 0.5, numItems);

            while (a < s0)
            {
                a += TWO_PI;
            }

            while (a >= s1)
            {
                a -= TWO_PI;
            }
        }

        int hovered = -1;

        for (int i = 0; i < numItems; i++)
        {
            float s = (float) getAngleFor(i - 0.5, numItems);
            float e = (float) getAngleFor(i + 0.5, numItems);

            if (a >= s && a < e && d >= this.radiusIn && (d < this.radiusOut || Config.clipMouseToRadialMenu.get() || Config.allowRadialMenuClickOutsideBounds.get()))
            {
                hovered = i;
                break;
            }
        }

        setHovered(hovered);

        if (Config.clipMouseToRadialMenu.get())
        {
            Window mainWindow = this.minecraft.getWindow();

            int windowWidth = mainWindow.getScreenWidth();
            int windowHeight = mainWindow.getScreenHeight();

            double[] xPos = new double[1];
            double[] yPos = new double[1];

            GLFW.glfwGetCursorPos(mainWindow.getWindow(), xPos, yPos);

            double scaledX = xPos[0] - (windowWidth / 2.0f);
            double scaledY = yPos[0] - (windowHeight / 2.0f);

            double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
            double radius = this.radiusOut * (windowWidth / (float) owner.width) * 0.975;

            if (distance > radius)
            {
                double fixedX = scaledX * radius / distance;
                double fixedY = scaledY * radius / distance;

                GLFW.glfwSetCursorPos(mainWindow.getWindow(), (int) (windowWidth / 2 + fixedX), (int) (windowHeight / 2 + fixedY));
            }
        }
    }

    private double getAngleFor(double i, int numItems)
    {
        if (numItems == 0)
        {
            return 0;
        }

        return ((i / numItems) + 0.25) * TWO_PI + Math.PI;
    }

    public record DrawingContext(PoseStack matrixStack, int width, int height, float x, float y, float z, Font fontRenderer)
    {
    }
}
