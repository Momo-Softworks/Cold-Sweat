package dev.momostudios.coldsweat.client.gui.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class SoulspringTooltip implements TooltipComponent
{
    double fuel;

    public SoulspringTooltip(double fuel)
    {
        this.fuel = fuel;
    }

    public double getFuel()
    {
        return fuel;
    }
}
