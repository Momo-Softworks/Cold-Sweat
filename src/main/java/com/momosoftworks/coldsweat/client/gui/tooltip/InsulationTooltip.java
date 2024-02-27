package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public class InsulationTooltip implements TooltipComponent
{
    List<Insulation> insulation;
    InsulationType type;

    public InsulationTooltip(List<Insulation> insulation, InsulationType type)
    {   this.insulation = insulation;
        this.type = type;
    }

    public List<Insulation> getInsulation()
    {   return insulation;
    }

    public InsulationType getType()
    {   return type;
    }
}
