package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.momosoftworks.coldsweat.common.capability.ItemInsulationCap;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class InsulationTooltip implements TooltipComponent
{
    List<ItemInsulationCap.InsulationPair> insulation;
    ItemStack stack;
    int size;

    public InsulationTooltip(List<ItemInsulationCap.InsulationPair> insulation, ItemStack stack)
    {
        this.insulation = insulation;
        this.stack = stack;
    }

    public List<ItemInsulationCap.InsulationPair> getInsulation()
    {
        return insulation;
    }

    public ItemStack getStack()
    {
        return stack;
    }
}
