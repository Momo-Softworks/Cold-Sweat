package com.momosoftworks.coldsweat.api.insulation;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.List;

public class StaticInsulation extends Insulation
{
    public static final Codec<StaticInsulation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("cold").forGetter(StaticInsulation::getCold),
            Codec.DOUBLE.fieldOf("hot").forGetter(StaticInsulation::getHot)
    ).apply(instance, StaticInsulation::new));

    private final double cold;
    private final double hot;

    public StaticInsulation(double cold, double hot)
    {   this.cold = cold;
        this.hot = hot;
    }

    public StaticInsulation(Pair<? extends Number, ? extends Number> pair)
    {   this(pair.getFirst().doubleValue(), pair.getSecond().doubleValue());
    }

    public double getCold()
    {   return cold;
    }

    public double getHot()
    {   return hot;
    }

    @Override
    public boolean isEmpty()
    {   return cold == 0 && hot == 0;
    }

    @Override
    public List<Insulation> split()
    {
        List<Insulation> insulation = new ArrayList<>();
        double cold = getCold();
        double hot = getHot();
        double neutral = cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0;
        if (cold == neutral) cold = 0;
        if (hot == neutral) hot = 0;

        // Cold insulation
        for (int i = 0; i < CSMath.ceil(Math.abs(cold)) / 2; i++)
        {   double coldInsul = CSMath.minAbs(CSMath.shrink(cold, i * 2), 2);
            insulation.add(new StaticInsulation(coldInsul, 0d));
        }

        // Neutral insulation
        for (int i = 0; i < CSMath.ceil(Math.abs(neutral)); i++)
        {   double neutralInsul = CSMath.minAbs(CSMath.shrink(neutral, i), 1);
            insulation.add(new StaticInsulation(neutralInsul, neutralInsul));
        }

        // Hot insulation
        for (int i = 0; i < CSMath.ceil(Math.abs(hot)) / 2; i++)
        {   double hotInsul = CSMath.minAbs(CSMath.shrink(hot, i * 2), 2);
            insulation.add(new StaticInsulation(0d, hotInsul));
        }
        return insulation;
    }

    @Override
    public String toString()
    {   return "Insulation{" + "cold=" + cold + ", hot=" + hot + '}';
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj instanceof StaticInsulation)
        {
            StaticInsulation insul = ((StaticInsulation) obj);
            return cold == insul.cold
                && hot == insul.hot;
        }
        return false;
    }

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble("cold", cold);
        tag.putDouble("heat", hot);
        return tag;
    }

    public static StaticInsulation deserialize(CompoundNBT tag)
    {   return new StaticInsulation(tag.getDouble("cold"), tag.getDouble("heat"));
    }
}
