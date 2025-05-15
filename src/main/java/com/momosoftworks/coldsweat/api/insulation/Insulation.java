package com.momosoftworks.coldsweat.api.insulation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.checkerframework.checker.units.qual.C;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class Insulation implements NbtSerializable
{
    private static Codec<Insulation> CODEC = null;
    private static StreamCodec<FriendlyByteBuf, Insulation> STREAM_CODEC = null;
    public static Codec<Insulation> getCodec()
    {
        if (CODEC == null)
            CODEC = Codec.either(StaticInsulation.CODEC, AdaptiveInsulation.CODEC).xmap(
                    either -> either.map(stat -> stat, adapt -> adapt),
                    insul -> insul instanceof StaticInsulation ? Either.left((StaticInsulation) insul) : Either.right((AdaptiveInsulation) insul));
        return CODEC;
    }

    public static StreamCodec<FriendlyByteBuf, Insulation> getNetworkCodec()
    {
        if (STREAM_CODEC == null)
        STREAM_CODEC = StreamCodec.of(
        (buf, insul) ->
        {
            if (insul instanceof StaticInsulation st)
            {   buf.writeUtf("static");
                StaticInsulation.STREAM_CODEC.encode(buf, st);
            }
            else if (insul instanceof AdaptiveInsulation ad)
            {   buf.writeUtf("adaptive");
                AdaptiveInsulation.STREAM_CODEC.encode(buf, ad);
            }
            else buf.writeUtf("none");
        },
        (buf) ->
        {
            String type = buf.readUtf();
            if (type.equals("static"))
            {   return StaticInsulation.STREAM_CODEC.decode(buf);
            }
            else if (type.equals("adaptive"))
            {   return AdaptiveInsulation.STREAM_CODEC.decode(buf);
            }
            else if (type.equals("none"))
            {   return new StaticInsulation(0, 0);
            }
            return null;
        });
        return STREAM_CODEC;
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

        public static final Codec<Slot> CODEC = Codec.STRING.xmap(Slot::byName, Slot::getSerializedName);
        public static final StreamCodec<ByteBuf, Slot> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

        @Override
        public String getSerializedName()
        {   return name;
        }

        @Nullable
        public static Slot byName(String name)
        {
            for (Slot type : values())
            {   if (type.name.equals(name))
                {   return type;
                }
            }
            return null;
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
            throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("Unknown insulation type: " + name));
        }
    }
}
