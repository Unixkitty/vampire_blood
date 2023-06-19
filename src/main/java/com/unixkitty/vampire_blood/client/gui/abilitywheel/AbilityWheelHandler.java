package com.unixkitty.vampire_blood.client.gui.abilitywheel;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.vampire_blood.client.KeyAction;
import com.unixkitty.vampire_blood.client.KeyBindings;
import com.unixkitty.vampire_blood.client.cache.ClientCache;
import com.unixkitty.vampire_blood.client.gui.abilitywheel.radial.AbilityRadialMenuItem;
import com.unixkitty.vampire_blood.client.gui.abilitywheel.radial.GenericRadialMenu;
import com.unixkitty.vampire_blood.client.gui.abilitywheel.radial.IRadialMenuHost;
import com.unixkitty.vampire_blood.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;

import javax.annotation.Nonnull;

public class AbilityWheelHandler
{
    private static boolean toolMenuKeyWasDown = false;
    private static boolean justReleasedMenuKey = false;

    public static void handleKeys(Minecraft minecraft, final InputEvent.Key event)
    {
        if (!KeyBindings.ABILITY_WHEEL_KEY.isUnbound() && ClientCache.isVampire())
        {
            if (minecraft.screen == null)
            {
                if (KeyBindings.ABILITY_WHEEL_KEY.isDown() && !toolMenuKeyWasDown)
                {
                    toolMenuKeyWasDown = true;

                    minecraft.setScreen(new AbilityWheelScreen(minecraft));
                }
            }
            else if (minecraft.screen instanceof AbilityWheelScreen)
            {
                if (event.getKey() == KeyBindings.ABILITY_WHEEL_KEY.getKey().getValue() && event.getAction() == InputConstants.RELEASE)
                {
                    justReleasedMenuKey = true;
                }
            }
        }
    }

    private static void reset()
    {
        justReleasedMenuKey = false;
        toolMenuKeyWasDown = false;
    }

    public static class AbilityWheelScreen extends Screen
    {
        private final GenericRadialMenu menu;

        protected AbilityWheelScreen(Minecraft minecraft)
        {
            super(Component.translatable("screen.vampire_blood.title.ability_wheel"));

            this.menu = new GenericRadialMenu(minecraft, new IRadialMenuHost()
            {
                @Nonnull
                @Override
                public Screen getScreen()
                {
                    return AbilityWheelScreen.this;
                }

                @Nonnull
                @Override
                public Font getFontRenderer()
                {
                    return AbilityWheelScreen.this.font;
                }
            })
            {
                @Override
                public void onClickOutside()
                {
                    close();
                }
            };

            for (KeyAction keyAction : KeyAction.values())
            {
                if (keyAction.texture != null)
                {
                    this.menu.add(new AbilityRadialMenuItem(keyAction)
                    {
                        @Override
                        public void draw(@Nonnull GenericRadialMenu.DrawingContext context)
                        {
                            context.matrixStack().pushPose();
                            RenderSystem.setShaderTexture(0, keyAction.texture);
                            GuiComponent.blit(context.matrixStack(), (int) (context.x() - 9), (int) (context.y() - 9), AbilityWheelScreen.this.getBlitOffset(), 0, 0, 18, 18, 18, 18);
                            context.matrixStack().popPose();
                        }
                    });
                }
            }
        }

        @Override
        public void tick()
        {
            super.tick();

            if (this.menu.isClosed())
            {
                Minecraft.getInstance().setScreen(null);

                reset();
            }
            else if (this.menu.isReady())
            {
                if (!KeyBindings.ABILITY_WHEEL_KEY.isDown() && justReleasedMenuKey)
                {
                    reset();

                    if (Config.releaseRadialMenuButtonToActivate.get())
                    {
                        this.menu.clickItem();
                    }

                    this.menu.close();
                }
            }
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button)
        {
            this.menu.clickItem();

            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
        {
            poseStack.pushPose();

            super.render(poseStack, mouseX, mouseY, partialTicks);

            poseStack.popPose();

            this.menu.draw(poseStack, mouseX, mouseY);
        }

        @Override
        public boolean isPauseScreen()
        {
            return false;
        }
    }
}
