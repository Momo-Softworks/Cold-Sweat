package dev.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class InsulatorTooltip implements TooltipComponent
{
    Pair<Double, Double> insulationValues;
    boolean isAdaptive;

    public InsulatorTooltip(Pair<Double, Double> insulationValues, boolean isAdaptive)
    {
        this.insulationValues = insulationValues;
        this.isAdaptive = isAdaptive;
    }

    public Pair<Double, Double> getInsulationValues()
    {
        return insulationValues;
    }

    public boolean isAdaptive()
    {
        return isAdaptive;
    }
}
