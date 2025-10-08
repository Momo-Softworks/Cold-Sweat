package com.momosoftworks.coldsweat.api.insulation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Insulation
{
    public static Codec<Insulation> getCodec()
    {
        return Codec.either(StaticInsulation.CODEC, AdaptiveInsulation.CODEC)
               .xmap(either -> either.map(stat -> stat, adapt -> adapt),
               insul ->
               {
                   if (insul instanceof StaticInsulation)
                   {   return Either.left(((StaticInsulation) insul));
                   }
                   return Either.right(((AdaptiveInsulation) insul));
               });
    }

    /**
     * @return True if this insulation has no value.
     */
    public abstract boolean isEmpty();

    /**
     * If this insulation is bigger than one slot, split it into multiple insulations.
     * @return A list of insulations.
     */
    public abstract List<Insulation> split();

    public abstract double getCold();
    public abstract double getHeat();
    public abstract double getValue();

    public abstract <T extends Insulation> T copy();

    public static List<Insulation> deepCopy(List<Insulation> list)
    {
        List<Insulation> newList = new ArrayList<>();
        for (Insulation insul : list)
        {   newList.add(insul.copy());
        }
        return newList;
    }

    public static List<Insulation> splitList(List<Insulation> pairs)
    {
        List<Insulation> newPairs = new ArrayList<>();
        for (Insulation pair : pairs)
        {   newPairs.addAll(pair.split());
        }
        return newPairs;
    }

    public static List<Insulation> combine(List<Insulation> list1, List<Insulation> list2)
    {
        List<Insulation> combined = new ArrayList<>();
        List<Insulation> remaining = new ArrayList<>(list2);

        for (Insulation ins1 : list1)
        {
            if (ins1 instanceof StaticInsulation staticIns1)
            {
                boolean matched = false;
                for (int i = 0; i < remaining.size(); i++)
                {
                    Insulation ins2 = remaining.get(i);
                    if (ins2 instanceof StaticInsulation staticIns2)
                    {
                        if (combineStaticInsulations(staticIns1, staticIns2, combined))
                        {   remaining.remove(i);
                            matched = true;
                            break;
                        }
                    }
                }
                if (!matched)
                {   combined.add(staticIns1);
                }
            }
            else
            {   combined.add(ins1);
            }
        }
        combined.addAll(remaining);

        return combined;
    }

    private static boolean combineStaticInsulations(StaticInsulation ins1, StaticInsulation ins2, List<Insulation> result)
    {
        double cold1 = ins1.getCold();
        double heat1 = ins1.getHeat();
        double cold2 = ins2.getCold();
        double heat2 = ins2.getHeat();
        // Try to match cold from ins1 with heat from ins2
        if (cold1 > 0 && heat2 > 0)
        {   combineValues(cold1, heat2, result);
            return true;
        }
        // Try to match heat from ins1 with cold from ins2
        if (heat1 > 0 && cold2 > 0)
        {   combineValues(cold2, heat1, result);
            return true;
        }
        // No match found
        return false;
    }

    private static void combineValues(double coldValue, double heatValue, List<Insulation> result)
    {
        if (coldValue == heatValue)
        {   // Equal values, create a neutral insulation
            result.add(new StaticInsulation(coldValue, heatValue));
        }
        else
        {   // Different values, create a neutral insulation with the minimum value
            double minValue = Math.min(coldValue, heatValue);
            result.add(new StaticInsulation(minValue, minValue));

            // Add remaining insulation if any
            if (coldValue > minValue)
            {   result.add(new StaticInsulation(coldValue - minValue, 0));
            }

            if (heatValue > minValue)
            {   result.add(new StaticInsulation(0, heatValue - minValue));
            }
        }
    }

    /**
     * Sort the list of insulation items, starting with cold insulation, then neutral, then heat, then adaptive.<br>
     * This method does not modify the input list
     * @return A sorted list of insulation items.
     */
    public static List<Insulation> sort(List<Insulation> pairs)
    {
        List<Insulation> newPairs = new ArrayList<>(pairs);
        newPairs.sort(Comparator.comparingInt(Insulation::getCompareValue));
        return newPairs;
    }

    public int getCompareValue()
    {
        List<Insulation> subset = this.split();
        if (subset.size() > 1)
        {   return sort(subset).get(0).getCompareValue() - 10;
        }
        else if (this instanceof AdaptiveInsulation insul)
        {   return Math.abs(insul.getInsulation()) >= 2 ? 7 : 8;
        }
        else if (this instanceof StaticInsulation insul)
        {
            double cold = Math.abs(insul.getCold());
            double hot = Math.abs(insul.getHeat());
            if (cold > hot)
            {   return cold >= 2 ? 1 : 2;
            }
            else if (cold == hot)
            {   return cold >= 1 ? 3 : 4;
            }
            else
            {   return hot >= 2 ? 5 : 6;
            }
        }
        return 0;
    }

    public enum Slot implements StringRepresentable
    {
        ITEM("item"),
        CURIO("curio"),
        ARMOR("armor");

        final String name;

        Slot(String name)
        {   this.name = name;
        }

        public static final Codec<Slot> CODEC = ExtraCodecs.enumIgnoreCase(values());

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Slot byName(String name)
        {   return EnumHelper.byName(values(), name);
        }
    }

    public enum Type implements StringRepresentable
    {
        COLD("cold"),
        HEAT("heat"),
        NEUTRAL("neutral"),
        ADAPTIVE("adaptive");

        final String name;

        Type(String name)
        {   this.name = name;
        }

        public static final Codec<Type> CODEC = ExtraCodecs.enumIgnoreCase(values());

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Type byName(String name)
        {   return EnumHelper.byName(values(), name);
        }
    }
}
