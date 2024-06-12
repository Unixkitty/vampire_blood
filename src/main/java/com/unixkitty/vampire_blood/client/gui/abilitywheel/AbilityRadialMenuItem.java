package com.unixkitty.vampire_blood.client.gui.abilitywheel;

import com.unixkitty.vampire_blood.client.KeyAction;
import com.unixkitty.vampire_blood.client.gui.abilitywheel.radial.GenericRadialMenu;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public abstract class AbilityRadialMenuItem
{
    private final KeyAction abilityAction;
    private final Component centralText;
    private boolean hovered;

    protected AbilityRadialMenuItem(KeyAction abilityAction)
    {
        this.centralText = Component.translatable(abilityAction.getKey().getName());
        this.abilityAction = abilityAction;
    }

    @Nonnull
    public Component getCentralText()
    {
        return this.centralText;
    }

    public boolean isHovered()
    {
        return this.hovered;
    }

    public void setHovered(boolean hovered)
    {
        this.hovered = hovered;
    }

    public abstract void draw(GenericRadialMenu.DrawingContext context);

    public void onClick()
    {
        this.abilityAction.handle();
    }
}
