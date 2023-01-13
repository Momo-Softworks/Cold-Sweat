package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class InsulatorTooltip implements TooltipComponent
{
    Pair<Double, Double> insulationValues;

    public InsulatorTooltip(Pair<Double, Double> insulationValues)
    {
        this.insulationValues = insulationValues;
    }

    public Pair<Double, Double> getInsulationValues()
    {
        return insulationValues;
    }
}
