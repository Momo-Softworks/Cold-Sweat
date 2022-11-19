package dev.momostudios.coldsweat.common.capability;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemInsulationCap implements IInsulatableCap
{
    private List<ItemStack> insulationItems = new ArrayList<>();
    private List<Pair<Double, Double>> insulation = new ArrayList<>();
    int saveCooldown = 0;
    CompoundTag savedTag = new CompoundTag();

    @Override
    public ImmutableList<ItemStack> getInsulationItems()
    {
        return ImmutableList.copyOf(insulationItems);
    }

    @Override
    public ImmutableList<Pair<Double, Double>> getInsulation()
    {
        return ImmutableList.copyOf(this.insulation);
    }

    // Sorts the items and insulation values based on their temperatures
    void calculateInsulation()
    {
        insulationItems.sort(Comparator.comparingDouble(stack -> ConfigSettings.INSULATION_ITEMS.get().getOrDefault(stack.getItem(), Pair.of(0d, 0d)).getFirst()));

        insulation.clear();
        for (ItemStack stack : insulationItems)
        {
            Pair<Double, Double> insulation = ConfigSettings.INSULATION_ITEMS.get().getOrDefault(stack.getItem(), Pair.of(0d, 0d));

            double coldTotal = insulation.getFirst();
            double hotTotal = insulation.getSecond();

            // Add the "neutral" insulation slots (where both values are the same)
            for (int i = 0; i < Math.min(coldTotal, hotTotal); i++)
            {
                this.insulation.add((Pair.of(1d, 1d)));
            }

            // The remaining slots are either hot or cold
            for (int i = 0; i < Math.abs(coldTotal - hotTotal) / 2; i++)
            {
                double cold = Math.max(0, coldTotal - i * 2);
                double hot = Math.max(0, hotTotal - i * 2);

                this.insulation.add((Pair.of(cold, hot)));
            }
        }

        saveCooldown = 0;
    }

    public void addInsulationItem(ItemStack stack)
    {
        this.insulationItems.add(stack);
        calculateInsulation();
    }

    public ItemStack removeInsulationItem(ItemStack stack)
    {
        this.insulationItems.remove(stack);
        calculateInsulation();
        return stack;
    }

    public ItemStack removeInsulationItem(int index)
    {
        ItemStack oldStack = this.insulationItems.remove(index);
        calculateInsulation();
        return oldStack;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        // For some reason Forge runs this every tick,
        // so we store the last-saved tag and return it
        if (saveCooldown > 0)
        {
            saveCooldown--;
            return savedTag;
        }

        ListTag list = new ListTag();
        for (ItemStack stack : insulationItems)
        {
            list.add(stack.save(new CompoundTag()));
        }

        CompoundTag nbt = new CompoundTag();
        nbt.put("Insulation", list);

        saveCooldown = 400;
        savedTag = nbt;
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        this.insulationItems = tag.getList("Insulation", 10).stream().map(nbt ->
                ItemStack.of((CompoundTag) nbt)).collect(Collectors.toList());
        calculateInsulation();
    }
}
