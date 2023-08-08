package dev.momostudios.coldsweat.common.capability;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.*;
import java.util.stream.Collectors;

public class ItemInsulationCap implements IInsulatableCap
{
    private final List<Pair<ItemStack, List<InsulationPair>>> insulation = new ArrayList<>();

    int saveCooldown = 0;
    CompoundNBT savedTag = new CompoundNBT();

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
            if (insulValues.isEmpty()) continue;

            List<InsulationPair> newValues = new ArrayList<>();

            if (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem()))
            {
                Pair<Double, Double> insulation = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem());
                if (insulation == null) continue;

                // Break it up into cold and hot
                double cold = insulation.getFirst();
                double hot = insulation.getSecond();
                double neutral = cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0;
                cold = CSMath.shrink(cold, neutral);
                hot  = CSMath.shrink(hot, neutral);

                /*
                 Subdivide insulation values into groups of 2 (or -2)
                 */
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
            else if (ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().containsKey(stack.getItem()) && !insulValues.isEmpty())
            {
                Pair<Double, Double> insulation = ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem());
                AdaptiveInsulation adaptiveInsulation = (AdaptiveInsulation) insulValues.get(0);

                double insulValue = insulation.getFirst();
                for (int i = 0; i < CSMath.ceil(Math.abs(insulValue) / 2); i++)
                {
                    newValues.add(new AdaptiveInsulation(CSMath.minAbs(CSMath.shrink(insulValue, i * 2), 2 * CSMath.getSign(insulValue)),
                                                         adaptiveInsulation.getFactor(), adaptiveInsulation.getSpeed()));
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
                if (pair instanceof AdaptiveInsulation)
                {
                    AdaptiveInsulation insul = (AdaptiveInsulation) pair;
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
            {   this.insulation.add(Pair.of(stack, new ArrayList<>(Arrays.asList(new AdaptiveInsulation(insulConfig.getFirst(), 0d, insulConfig.getSecond())))));
            }
        }
        else if (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem()))
        {   this.insulation.add(Pair.of(stack, new ArrayList<>(Arrays.asList(new Insulation(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()))))));
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
    public CompoundNBT serializeNBT()
    {
        // For some reason Forge runs this every tick,
        // so we store the last-saved tag and return it
        if (saveCooldown > 0)
        {
            saveCooldown--;
            return savedTag;
        }

        // Save the insulation items
        ListNBT insulNBT = new ListNBT();
        // Iterate over insulation items
        for (Pair<ItemStack, List<InsulationPair>> entry : insulation)
        {
            CompoundNBT entryNBT = new CompoundNBT();
            List<InsulationPair> pairList = entry.getSecond();
            // Store ItemStack data
            entryNBT.put("Item", entry.getFirst().save(new CompoundNBT()));

            ListNBT pairListNBT = new ListNBT();
            // Store insulation values for the item
            for (InsulationPair pair : pairList)
            {
                CompoundNBT pairTag = new CompoundNBT();
                if (pair instanceof Insulation)
                {   Insulation insul = (Insulation) pair;
                    pairTag.putDouble("Cold", insul.getCold());
                    pairTag.putDouble("Hot", insul.getHot());
                }
                else if (pair instanceof AdaptiveInsulation)
                {   AdaptiveInsulation insul = (AdaptiveInsulation) pair;
                    pairTag.putDouble("Amount", insul.getInsulation());
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

        CompoundNBT tag = new CompoundNBT();
        if (!insulNBT.isEmpty())
            tag.put("Insulation", insulNBT);
        else
            tag.remove("Insulation");

        saveCooldown = 400;
        savedTag = tag;
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag)
    {
        this.insulation.clear();

        // Load the insulation items
        ListNBT insulNBT = tag.getList("Insulation", 10);

        // Iterate over insulation items
        for (int i = 0; i < insulNBT.size(); i++)
        {
            CompoundNBT entryNBT = insulNBT.getCompound(i);
            ItemStack stack = ItemStack.of(entryNBT.getCompound("Item"));
            List<InsulationPair> pairList = new ArrayList<>();
            ListNBT pairListNBT = entryNBT.getList("Values", 10);

            // Iterate over insulation values for the item
            for (int j = 0; j < pairListNBT.size(); j++)
            {
                CompoundNBT pairTag = pairListNBT.getCompound(j);
                if (pairTag.contains("Cold"))
                {
                    double cold = pairTag.getDouble("Cold");
                    double hot = pairTag.getDouble("Hot");
                    pairList.add(new Insulation(cold, hot));
                }
                else if (pairTag.contains("Amount"))
                {
                    double insul = pairTag.getDouble("Amount");
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

    public CompoundNBT serializeSimple(ItemStack stack)
    {
        CompoundNBT tag = stack.getOrCreateTag();
        ListNBT insulTag = new ListNBT();

        for (List<InsulationPair> pairList : this.insulation.stream().map(Pair::getSecond).collect(Collectors.toList()))
        {
            for (InsulationPair pair : pairList)
            {
                CompoundNBT compound = new CompoundNBT();
                if (pair instanceof Insulation)
                {   Insulation insul = (Insulation) pair;
                    compound.putDouble("Cold", insul.getCold());
                    compound.putDouble("Hot", insul.getHot());
                }
                else if (pair instanceof AdaptiveInsulation)
                {   AdaptiveInsulation insul = (AdaptiveInsulation) pair;
                    compound.putDouble("Insulation", insul.getInsulation());
                    compound.putDouble("Factor", insul.getFactor());
                    compound.putDouble("Speed", insul.getSpeed());
                }
                insulTag.add(compound);
            }
        }
        if (!insulTag.isEmpty())
            tag.put("Insulation", insulTag);
        else
            tag.remove("Insulation");

        return tag;
    }

    public List<InsulationPair> deserializeSimple(ItemStack stack)
    {
        List<InsulationPair> pairs = stack.getOrCreateTag().getList("Insulation", 10).stream()
        .map(nbt ->
        {
            CompoundNBT compound = (CompoundNBT) nbt;

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
            if (pair instanceof AdaptiveInsulation)
            {   AdaptiveInsulation insul = (AdaptiveInsulation) pair;
                return Math.abs(insul.getInsulation()) >= 2 ? 7 : 6;
            }
            else if (pair instanceof Insulation)
            {   Insulation insul = (Insulation) pair;

                double absCold = Math.abs(insul.cold);
                double absHot = Math.abs(insul.hot);
                if (absCold >= 2 && absHot >= 2)
                    return 2;
                else if (absCold >= 2)
                    return 0;
                else if (absHot >= 2)
                    return 4;
                else if (absCold >= 1 && absHot >= 1)
                    return 3;
                else if (absCold >= 1)
                    return 1;
                else if (absHot >= 1)
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
