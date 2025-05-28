package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;

import java.util.Objects;

public record DoubleBounds(double min, double max)
{
    public static final Codec<DoubleBounds> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("min", Double.NEGATIVE_INFINITY).forGetter(bounds -> bounds.min),
            Codec.DOUBLE.optionalFieldOf("max", Double.POSITIVE_INFINITY).forGetter(bounds -> bounds.max)
    ).apply(instance, DoubleBounds::new));

    public static final Codec<DoubleBounds> CODEC = Codec.either(DIRECT_CODEC, Codec.DOUBLE).xmap(
            either -> either.map(left -> left, right -> new DoubleBounds(right, right)),
            bounds -> bounds.max == bounds.min ? Either.right(bounds.min) : Either.left(bounds)
    );

    public static DoubleBounds NONE = new DoubleBounds(-Integer.MAX_VALUE, Integer.MAX_VALUE);

    public DoubleBounds(Double[] bounds)
    {   this(bounds[0], bounds[1]);
    }

    public boolean test(double value)
    {   return CSMath.betweenInclusive(value, min, max);
    }

    public boolean contains(IntegerBounds bounds)
    {   return test(bounds.min()) && test(bounds.max());
    }

    public double getRandom()
    {
        double realMin = Math.min(min, max);
        double realMax = Math.max(min, max);
        return realMin + Math.random() * (realMax - realMin + 1);
    }

    public double clamp(double value)
    {
        double realMin = Math.min(min, max);
        double realMax = Math.max(min, max);
        return CSMath.clamp(value, realMin, realMax);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {   return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {   return false;
        }

        DoubleBounds that = (DoubleBounds) obj;

        if (!Objects.equals(min, that.min))
        {   return false;
        }
        return Objects.equals(max, that.max);
    }
}
