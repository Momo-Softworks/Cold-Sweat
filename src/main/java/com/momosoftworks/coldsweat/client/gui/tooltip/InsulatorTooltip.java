package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class InsulatorTooltip implements TooltipComponent
{
    Pair<Double, Double> insulationValues;
    InsulationType type;

    public InsulatorTooltip(Pair<Double, Double> insulationValues, InsulationType type)
    {
        this.insulationValues = insulationValues;
        this.type = type;
    }

    public Pair<Double, Double> getInsulationValues()
    {
        return insulationValues;
    }

    public InsulationType getType()
    {   return type;
    }
}
