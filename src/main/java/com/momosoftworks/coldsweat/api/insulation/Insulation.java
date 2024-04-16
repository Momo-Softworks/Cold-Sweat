package com.momosoftworks.coldsweat.api.insulation;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Insulation implements NbtSerializable
{
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
    public abstract double getHot();

    /**
     * Sort the list of insulation items, starting with cold insulation, then neutral, then hot, then adaptive.<br>
     * This method does not modify the input list
     * @return A sorted list of insulation items.
     */
    public static List<Insulation> sort(List<Insulation> pairs)
    {
        List<Insulation> newPairs = new ArrayList<>(pairs);
        newPairs.sort(Comparator.comparingDouble(pair ->
        {
            if (pair instanceof AdaptiveInsulation insul)
                return Math.abs(insul.getInsulation()) >= 2 ? 7 : 6;
            else if (pair instanceof StaticInsulation insul)
            {
                double absCold = Math.abs(insul.getCold());
                double absHot = Math.abs(insul.getHot());
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
        return newPairs;
    }

    public static Insulation deserialize(CompoundTag tag)
    {
        if (tag.contains("cold") && tag.contains("hot"))
        {   return new StaticInsulation(tag.getDouble("cold"), tag.getDouble("hot"));
        }
        else if (tag.contains("insulation"))
        {   return new AdaptiveInsulation(tag.getDouble("insulation"), tag.getDouble("speed"));
        }
        return null;
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

        public static final Codec<Slot> CODEC = Codec.STRING.xmap(Slot::byName, Slot::getSerializedName);

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Slot byName(String name)
        {   for (Slot type : values())
            {   if (type.name.equals(name))
                {   return type;
                }
            }
            throw new IllegalArgumentException("Unknown insulation type: " + name);
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

        public static final Codec<Type> CODEC = Codec.STRING.xmap(Type::byName, Type::getSerializedName);

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Type byName(String name)
        {   for (Type type : values())
            {   if (type.name.equals(name))
                {   return type;
                }
            }
            throw new IllegalArgumentException("Unknown insulation type: " + name);
        }
    }
}
