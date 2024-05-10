package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.momosoftworks.coldsweat.api.insulation.Insulation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class InsulationTooltip implements TooltipComponent
{
    List<Insulation> insulation;
    Insulation.Slot slot;
    ItemStack stack;

    public InsulationTooltip(List<Insulation> insulation, Insulation.Slot slot, ItemStack stack)
    {
        this.insulation = insulation;
        this.slot = slot;
        this.stack = stack;
    }

    public List<Insulation> getInsulation()
    {   return insulation;
    }

    public Insulation.Slot getSlot()
    {   return slot;
    }

    public ItemStack getItem()
    {   return stack;
    }
}
