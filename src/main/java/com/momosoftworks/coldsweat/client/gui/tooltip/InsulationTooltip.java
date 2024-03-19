package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.InsulationSlot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public class InsulationTooltip implements TooltipComponent
{
    List<Insulation> insulation;
    InsulationSlot slot;

    public InsulationTooltip(List<Insulation> insulation, InsulationSlot slot)
    {   this.insulation = insulation;
        this.slot = slot;
    }

    public List<Insulation> getInsulation()
    {   return insulation;
    }

    public InsulationSlot getSlot()
    {   return slot;
    }
}
