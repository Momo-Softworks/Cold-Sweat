package dev.momostudios.coldsweat.common.capability;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ItemInsulationCap implements IInsulatableCap
{
    private final List<Pair<ItemStack, List<InsulationPair>>> insulation = new ArrayList<>();

    int saveCooldown = 0;
    CompoundTag savedTag = new CompoundTag();

    @Override
    public List<Pair<ItemStack, List<InsulationPair>>> getInsulation()
    {
        return this.insulation;
    }

    // Sorts the items and insulation values based on their temperatures
    void calculateInsulation()
    {
        // Iterate through insulation items and tally up cold, hot, and neutral insulation
        for (Pair<ItemStack, List<InsulationPair>> entry : insulation)
        {
            ItemStack stack = entry.getFirst();
            List<InsulationPair> insulValues = entry.getSecond();

            List<InsulationPair> newValues = new ArrayList<>();

            if (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem()))
            {
                Pair<Double, Double> insul = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem());
                // Break it up into cold and hot
                double cold = insul.getFirst();
                double hot = insul.getSecond();
                double neutral = cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0;
                cold = CSMath.shrink(cold, neutral);
                hot  = CSMath.shrink(hot, neutral);

                // Cold insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(cold) / 2); i++)
                {
                    double coldInsul = CSMath.minAbs(CSMath.shrink(cold, i * 2), 2 * CSMath.getSign(cold));
                    newValues.add(new Insulation(coldInsul, 0d));
                }

                // Neutral insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(neutral)); i++)
                {
                    double neutralInsul = CSMath.minAbs(CSMath.shrink(neutral, i), CSMath.getSign(neutral));
                    newValues.add(new Insulation(neutralInsul, neutralInsul));
                }

                // Hot insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(hot) / 2); i++)
                {
                    double hotInsul = CSMath.minAbs(CSMath.shrink(hot, i * 2), 2 * CSMath.getSign(hot));
                    newValues.add(new Insulation(0d, hotInsul));
                }
            }
            else if (ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().containsKey(stack.getItem()))
            {
                Pair<Double, Double> insul = ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem());
                AdaptiveInsulation adaptiveInsulation = (AdaptiveInsulation) insulValues.get(0);

                double insulValue = insul.getFirst();
                for (int i = 0; i < CSMath.ceil(Math.abs(insulValue) / 2); i++)
                {
                    double adaptInsul = CSMath.minAbs(CSMath.shrink(insulValue, i * 2), 2 * CSMath.getSign(insulValue));
                    newValues.add(new AdaptiveInsulation(adaptInsul, adaptiveInsulation.getFactor(), adaptiveInsulation.getSpeed()));
                }
            }
            insulValues.clear();
            insulValues.addAll(newValues);
        }

        saveCooldown = 0;
    }

    public void calcAdaptiveInsulation(double worldTemp, double minShift, double maxShift)
    {
        double minTemp = ConfigSettings.MIN_TEMP.get() + minShift;
        double maxTemp = ConfigSettings.MAX_TEMP.get() + maxShift;

        insulation.stream().filter(entry -> entry.getSecond().stream().allMatch(element -> element instanceof AdaptiveInsulation)).forEach(entry ->
        {
            List<InsulationPair> list = entry.getSecond();
            for (InsulationPair pair : list)
            {
                if (pair instanceof AdaptiveInsulation insul)
                {
                    double factor = insul.getFactor();
                    double adaptSpeed = insul.getSpeed();

                    double newFactor;
                    if (CSMath.withinRange(CSMath.blend(-1, 1, worldTemp, minTemp, maxTemp), -0.25, 0.25))
                    {   newFactor = CSMath.shrink(factor, adaptSpeed);
                    }
                    else
                    {   newFactor = CSMath.clamp(factor + CSMath.blend(-adaptSpeed, adaptSpeed, worldTemp, minTemp, maxTemp), -1, 1);
                    }
                    insul.setFactor(newFactor);
                }
            }
        });
    }

    public void addInsulationItem(ItemStack stack)
    {
        if (ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().containsKey(stack.getItem()))
        {
            Pair<Double, Double> insulConfig = ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem());
            if (insulConfig != null)
            {   this.insulation.add(Pair.of(stack, new ArrayList<>(List.of(new AdaptiveInsulation(insulConfig.getFirst(), 0d, insulConfig.getSecond())))));
            }
        }
        else if (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem()))
        {   this.insulation.add(Pair.of(stack, new ArrayList<>(List.of(new Insulation(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()))))));
        }
        this.calculateInsulation();
    }

    public ItemStack removeInsulationItem(ItemStack stack)
    {
        Optional<Pair<ItemStack, List<InsulationPair>>> toRemove = this.insulation.stream().filter(entry -> entry.getFirst().sameItem(stack)).findFirst();
        if (toRemove.isPresent())
        {
            this.insulation.remove(toRemove.get());
            this.calculateInsulation();
        }
        return stack;
    }

    public ItemStack getInsulationItem(int index)
    {
        return this.insulation.stream().map(Pair::getFirst).skip(index).findFirst().orElse(ItemStack.EMPTY);
    }

    public List<InsulationPair> getInsulationValues()
    {
        return this.insulation.stream().map(Pair::getSecond).flatMap(Collection::stream).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
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

        // Save the insulation items
        ListTag insulNBT = new ListTag();
        // Iterate over insulation items
        for (Pair<ItemStack, List<InsulationPair>> entry : insulation)
        {
            CompoundTag entryNBT = new CompoundTag();
            List<InsulationPair> pairList = entry.getSecond();
            // Store ItemStack data
            entryNBT.put("Item", entry.getFirst().save(new CompoundTag()));

            ListTag pairListNBT = new ListTag();
            // Store insulation values for the item
            for (InsulationPair pair : pairList)
            {
                CompoundTag pairTag = new CompoundTag();
                if (pair instanceof Insulation insul)
                {
                    pairTag.putDouble("Cold", insul.getCold());
                    pairTag.putDouble("Hot", insul.getHot());
                }
                else if (pair instanceof AdaptiveInsulation insul)
                {
                    pairTag.putDouble("Insulation", insul.getInsulation());
                    pairTag.putDouble("Factor", insul.getFactor());
                    pairTag.putDouble("Speed", insul.getSpeed());
                }
                // Add the value to the pair list
                pairListNBT.add(pairTag);
            }
            entryNBT.put("Values", pairListNBT);
            // Add the item to the list
            insulNBT.add(entryNBT);
        }

        CompoundTag nbt = new CompoundTag();
        nbt.put("Insulation", insulNBT);

        saveCooldown = 400;
        savedTag = nbt;
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        this.insulation.clear();

        // Load the insulation items
        ListTag insulNBT = tag.getList("Insulation", 10);

        // Iterate over insulation items
        for (int i = 0; i < insulNBT.size(); i++)
        {
            CompoundTag entryNBT = insulNBT.getCompound(i);
            ItemStack stack = ItemStack.of(entryNBT.getCompound("Item"));
            List<InsulationPair> pairList = new ArrayList<>();
            ListTag pairListNBT = entryNBT.getList("Values", 10);

            // Iterate over insulation values for the item
            for (int j = 0; j < pairListNBT.size(); j++)
            {
                CompoundTag pairTag = pairListNBT.getCompound(j);
                if (pairTag.contains("Cold"))
                {
                    double cold = pairTag.getDouble("Cold");
                    double hot = pairTag.getDouble("Hot");
                    pairList.add(new Insulation(cold, hot));
                }
                else if (pairTag.contains("Insulation"))
                {
                    double insul = pairTag.getDouble("Insulation");
                    double factor = pairTag.getDouble("Factor");
                    double speed = pairTag.getDouble("Speed");
                    pairList.add(new AdaptiveInsulation(insul, factor, speed));
                }
            }

            // Add the item to the map
            this.insulation.add(Pair.of(stack, pairList));
        }
        this.calculateInsulation();
    }

    public void serializeSimple(ItemStack stack)
    {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag insulation = new ListTag();

        for (List<InsulationPair> pairList : this.insulation.stream().map(Pair::getSecond).toList())
        {
            for (InsulationPair pair : pairList)
            {
                CompoundTag compound = new CompoundTag();
                if (pair instanceof Insulation insul)
                {
                    compound.putDouble("Cold", insul.getCold());
                    compound.putDouble("Hot", insul.getHot());
                }
                else if (pair instanceof AdaptiveInsulation insul)
                {
                    compound.putDouble("Insulation", insul.getInsulation());
                    compound.putDouble("Factor", insul.getFactor());
                    compound.putDouble("Speed", insul.getSpeed());
                }
                insulation.add(compound);
            }
        }

        tag.put("Insulation", insulation);
    }

    public List<InsulationPair> deserializeSimple(ItemStack stack)
    {
        List<InsulationPair> pairs = stack.getOrCreateTag().getList("Insulation", 10).stream()
        .map(nbt ->
        {
            CompoundTag compound = (CompoundTag) nbt;

            if (compound.contains("Cold"))
            {   return new Insulation(compound.getDouble("Cold"), compound.getDouble("Hot"));
            }
            else
            {   return new AdaptiveInsulation(compound.getDouble("Insulation"), compound.getDouble("Factor"), compound.getDouble("Speed"));
            }
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        return sortInsulationList(pairs);
    }

    public static List<InsulationPair> sortInsulationList(List<InsulationPair> pairs)
    {
        pairs.sort(Comparator.comparingDouble(pair ->
        {
            if (pair instanceof AdaptiveInsulation insul)
                return Math.abs(insul.getInsulation()) >= 2 ? 7 : 6;
            else if (pair instanceof Insulation insul)
            {
                double abscold = Math.abs(insul.cold);
                double abshot = Math.abs(insul.hot);
                if (abscold >= 2 && abshot >= 2)
                    return 2;
                else if (abscold >= 2)
                    return 0;
                else if (abshot >= 2)
                    return 4;
                else if (abscold >= 1 && abshot >= 1)
                    return 3;
                else if (abscold >= 1)
                    return 1;
                else if (abshot >= 1)
                    return 5;
                else
                    return 1;
            }
            return 0;
        }));
        return pairs;
    }

    public static abstract class InsulationPair
    {}

    public static class Insulation extends InsulationPair
    {
        private final double cold;
        private final double hot;

        public Insulation(double cold, double hot)
        {
            this.cold = cold;
            this.hot = hot;
        }

        public Insulation(Pair<? extends Number, ? extends Number> pair)
        {
            this(pair.getFirst().doubleValue(), pair.getSecond().doubleValue());
        }

        public double getCold()
        {
            return cold;
        }

        public double getHot()
        {
            return hot;
        }

        @Override
        public String toString()
        {
            return "Insulation{" + "cold=" + cold + ", hot=" + hot + '}';
        }
    }

    public static class AdaptiveInsulation extends InsulationPair
    {
        private final double insulation;
        private double factor;
        private double speed;

        public AdaptiveInsulation(double insulation, double factor, double speed)
        {
            this.insulation = insulation;
            this.factor = factor;
            this.speed = speed;
        }

        public double getInsulation()
        {
            return insulation;
        }

        public double getFactor()
        {
            return factor;
        }

        public void setFactor(double factor)
        {
            this.factor = factor;
        }

        public double getSpeed()
        {
            return speed;
        }

        @Override
        public String toString()
        {
            return "AdaptiveInsulation{" + "insulation=" + insulation + ", factor=" + factor + ", speed=" + speed + '}';
        }
    }
}
