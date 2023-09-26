package dev.momostudios.coldsweat.client.gui.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;

public class SoulspringTooltip implements TooltipComponent
{
    Item[] fuelItems;
    double fuel;

    public SoulspringTooltip(double fuel)
    {
        this.fuel = fuel;
    }

    public double getFuel()
    {
        return fuel;
    }

    public Item[] getFuelItems()
    {
        return fuelItems;
    }
}
