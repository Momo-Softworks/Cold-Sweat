package dev.momostudios.coldsweat.client.gui.tooltip;

import dev.momostudios.coldsweat.common.capability.ItemInsulationCap;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class InsulationTooltip implements TooltipComponent
{
    List<ItemInsulationCap.InsulationPair> insulation;
    ItemStack stack;
    int size;

    public InsulationTooltip(List<ItemInsulationCap.InsulationPair> insulation, ItemStack stack, int size)
    {
        this.insulation = insulation;
        this.stack = stack;
        this.size = size;
    }

    public List<ItemInsulationCap.InsulationPair> getInsulation()
    {
        return insulation;
    }

    public ItemStack getStack()
    {
        return stack;
    }

    public int getSize()
    {
        return size;
    }
}
