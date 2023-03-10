package dev.momostudios.coldsweat.common.capability;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemInsulationCap implements IInsulatableCap
{
    private List<ItemStack> insulationItems = new ArrayList<>();
    private final List<Pair<Double, Double>> insulation = new ArrayList<>();
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
        insulation.clear();

        // Iterate through insulation items and tally up cold, hot, and neutral insulation
        for (ItemStack stack : insulationItems)
        {
            // Get the item's insulation
            Pair<Double, Double> insulVal = ConfigSettings.INSULATION_ITEMS.get().getOrDefault(stack.getItem(), Pair.of(0d, 0d));

            // Break it up into cold and hot
            double cold = insulVal.getFirst();
            double hot = insulVal.getSecond();
            double neutral = cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0;
            if (cold == neutral) cold = 0;
            if (hot == neutral) hot = 0;

            // Cold insulation
            for (int i = 0; i < CSMath.ceil(Math.abs(cold)) / 2; i++)
            {
                double coldInsul = CSMath.minAbs(CSMath.shrink(cold, i * 2), 2);
                insulation.add(Pair.of(coldInsul, 0d));
            }

            // Neutral insulation
            for (int i = 0; i < CSMath.ceil(Math.abs(neutral)); i++)
            {
                double neutralInsul = CSMath.minAbs(CSMath.shrink(neutral, i), 1);
                insulation.add(Pair.of(neutralInsul, neutralInsul));
            }

            // Hot insulation
            for (int i = 0; i < CSMath.ceil(Math.abs(hot)) / 2; i++)
            {
                double hotInsul = CSMath.minAbs(CSMath.shrink(hot, i * 2), 2);
                insulation.add(Pair.of(0d, hotInsul));
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
        return removeInsulationItem(insulationItems.indexOf(stack));
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

    public void serializeSimple(ItemStack stack)
    {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = new ListTag();
        for (Pair<Double, Double> pair : insulation)
        {
            CompoundTag compound = new CompoundTag();
            compound.putDouble("Cold", pair.getFirst());
            compound.putDouble("Hot", pair.getSecond());
            list.add(compound);
        }
        tag.put("Insulation", list);
    }

    public List<Pair<Double, Double>> deserializeSimple(ItemStack stack)
    {
        insulation.clear();
        List<Pair<Double, Double>> newInsul = stack.getOrCreateTag().getList("Insulation", 10).stream()
        .map(nbt ->
        {
            CompoundTag compound = (CompoundTag) nbt;
            return Pair.of(compound.getDouble("Cold"), compound.getDouble("Hot"));
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        insulation.addAll(newInsul);
        return newInsul;
    }
}
