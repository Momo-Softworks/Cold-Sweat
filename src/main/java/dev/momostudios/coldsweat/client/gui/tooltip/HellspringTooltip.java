package dev.momostudios.coldsweat.client.gui.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class HellspringTooltip implements TooltipComponent
{
    double fuel;

    public HellspringTooltip(double fuel)
    {
        this.fuel = fuel;
    }

    public double getFuel()
    {
        return fuel;
    }
}
