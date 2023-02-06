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

        double neutralTotal = 0;

        // Iterate through insulation items and tally up cold, hot, and neutral insulation
        for (ItemStack stack : insulationItems)
        {
            // Get the item's insulation
            Pair<Double, Double> insulVal = ConfigSettings.INSULATION_ITEMS.get().getOrDefault(stack.getItem(), Pair.of(0d, 0d));

            // Break it up into cold and hot
            double cold = insulVal.getFirst();
            double hot = insulVal.getSecond();
            // Get whether each is positive or negative
            int coldSign = CSMath.getSign(cold);
            int hotSign = CSMath.getSign(hot);
            // If both are positive or negative, then there is overlap (neutral)
            double neutral = hotSign == coldSign ? CSMath.least(cold, hot) : 0;

            neutralTotal += neutral;
            // Subtract neutral from cold/hot to get just the remainder
            double coldTotal = CSMath.shrink(cold, neutral);
            double hotTotal = CSMath.shrink(hot, neutral);

            // Get whether the totals are positive or negative
            int neutralSign = CSMath.getSign(neutralTotal);

            // Tally up the cold slots
            // Use floor to exclude the remainder; only whole values
            for (int i = 0; i < CSMath.ceil(Math.abs(coldTotal) / 2); i++)
            {
                double coldVal = CSMath.least(coldSign * 2d, coldTotal - i*2);
                double hotVal = CSMath.least(CSMath.shrink(2d, coldVal), hotTotal);
                this.insulation.add((Pair.of(coldVal, hotVal)));
                hotTotal -= hotVal;
            }

            // Tally up the neutral slots
            for (int i = 0; i < CSMath.ceil(Math.abs(neutralTotal)); i++)
            {
                double val = CSMath.least(neutralSign, neutralTotal - i);
                this.insulation.add((Pair.of(val, val)));
            }

            // Tally up the hot slots
            for (int i = 0; i < CSMath.ceil(Math.abs(hotTotal) / 2); i++)
            {
                double val = CSMath.least(hotSign * 2d, hotTotal - i*2);
                this.insulation.add((Pair.of(0d, val)));
            }
        }
        insulation.sort(Comparator.comparingDouble(pair -> Math.abs(pair.getFirst() - 2)));

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
