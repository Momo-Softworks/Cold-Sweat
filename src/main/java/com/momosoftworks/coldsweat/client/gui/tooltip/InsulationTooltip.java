package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.momosoftworks.coldsweat.api.insulation.Insulation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public class InsulationTooltip implements TooltipComponent
{
    List<Insulation> insulation;
    Insulation.Slot slot;

    public InsulationTooltip(List<Insulation> insulation, Insulation.Slot slot)
    {   this.insulation = insulation;
        this.slot = slot;
    }

    public List<Insulation> getInsulation()
    {   return insulation;
    }

    public Insulation.Slot getSlot()
    {   return slot;
    }
}
