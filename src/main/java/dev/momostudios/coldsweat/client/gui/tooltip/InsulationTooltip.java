package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class InsulationTooltip implements TooltipComponent
{
    List<Pair<Double, Double>> insulationValues;
    ItemStack stack;
    int size;

    public InsulationTooltip(List<Pair<Double, Double>> insulationValues, ItemStack stack, int size)
    {
        this.insulationValues = insulationValues;
        this.stack = stack;
        this.size = size;
    }

    public List<Pair<Double, Double>> getInsulationValues()
    {
        return insulationValues;
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
