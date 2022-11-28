package dev.momostudios.coldsweat.common.capability;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
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

        double coldTotal = 0;
        double hotTotal = 0;
        double neutralTotal = 0;

        // Iterate through insulation items and tally up cold, hot, and neutral insulation
        for (ItemStack stack : insulationItems)
        {
            // Get the item's insulation
            Pair<Double, Double> insulation = ConfigSettings.INSULATION_ITEMS.get().getOrDefault(stack.getItem(), Pair.of(0d, 0d));

            // Break it up into cold and hot
            double cold = insulation.getFirst();
            double hot = insulation.getSecond();
            // Get whether each is positive or negative
            int coldSign = CSMath.getSign(cold);
            int hotSign = CSMath.getSign(hot);
            // If both are positive or negative, then there is overlap (neutral)
            double neutral = hotSign == coldSign ? CSMath.getLeastExtreme(cold, hot) : 0;

            neutralTotal += neutral;
            // Subtract neutral from cold/hot to get just the remainder
            coldTotal += CSMath.reduce(cold, neutral);
            hotTotal += CSMath.reduce(hot, neutral);
        }
        // Get whether the totals are positive or negative
        int coldSign = CSMath.getSign(coldTotal);
        int hotSign = CSMath.getSign(hotTotal);
        int neutralSign = CSMath.getSign(neutralTotal);

        // Tally up the cold slots
        double coldAbs = Math.abs(coldTotal);
        // Use floor to exclude the remainder; only whole values
        for (int i = 0; i < CSMath.floor(coldAbs / 2); i++)
        {
            this.insulation.add((Pair.of(2d * coldSign, 0d)));
        }
        // Add the remainder as its own slot
        double coldRemainder = coldAbs - CSMath.floor(coldAbs);
        if (coldRemainder > 0)
        {
            this.insulation.add((Pair.of(coldRemainder * coldSign, 0d)));
        }

        // Tally up the neutral slots
        double neutralAbs = Math.abs(neutralTotal);
        // Use floor to exclude the remainder; only whole values
        for (int i = 0; i < CSMath.floor(neutralAbs); i++)
        {
            this.insulation.add((Pair.of(neutralSign * 1d, neutralSign * 1d)));
        }
        // Add the remainder as its own slot
        double neutralRemainder = neutralAbs - CSMath.floor(neutralAbs);
        if (neutralRemainder > 0)
        {
            this.insulation.add((Pair.of(neutralRemainder * neutralSign, neutralRemainder * neutralSign)));
        }

        // Tally up the cold slots
        double hotAbs = Math.abs(hotTotal);
        // Use floor to exclude the remainder; only whole values
        for (int i = 0; i < CSMath.floor(hotAbs / 2); i++)
        {
            this.insulation.add((Pair.of(0d, 2d * hotSign)));
        }
        // Add the remainder as its own slot
        double hotRemainder = hotAbs - CSMath.floor(hotAbs);
        if (hotRemainder > 0)
        {
            this.insulation.add((Pair.of(0d, hotRemainder * hotSign)));
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
