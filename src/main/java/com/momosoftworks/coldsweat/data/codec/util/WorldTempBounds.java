package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.WorldTempRequirement;
import com.momosoftworks.coldsweat.util.math.CSMath;

import java.util.Objects;

public class WorldTempBounds
{
    private final WorldTempRequirement min;
    private final WorldTempRequirement max;

    public static final Codec<WorldTempBounds> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WorldTempRequirement.CODEC.optionalFieldOf("min", WorldTempRequirement.NEGATIVE_INFINITY).forGetter(bounds -> bounds.min),
            WorldTempRequirement.CODEC.optionalFieldOf("max", WorldTempRequirement.INFINITY).forGetter(bounds -> bounds.max)
    ).apply(instance, WorldTempBounds::new));

    public static final Codec<WorldTempBounds> CODEC = Codec.either(DIRECT_CODEC, WorldTempRequirement.CODEC).xmap(
            either -> either.map(left -> left, right -> new WorldTempBounds(right, right)),
            bounds -> bounds.max.equals(bounds.min) ? Either.right(bounds.min) : Either.left(bounds)
    );

    public WorldTempBounds(WorldTempRequirement min, WorldTempRequirement max)
    {
        this.min = min;
        this.max = max;
    }

    public WorldTempRequirement min()
    {   return min;
    }

    public WorldTempRequirement max()
    {   return max;
    }

    public boolean test(double value)
    {   return value >= min.get() && value <= max.get();
    }

    public boolean contains(WorldTempBounds bounds)
    {   return bounds.min.get() >= min.get() && bounds.max.get() <= max.get();
    }

    public double getRandom()
    {   return min.get() + (Math.random() * (max.get() - min.get()));
    }

    public double clamp(double value)
    {   return CSMath.clamp(value, min.get(), max.get());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldTempBounds that = (WorldTempBounds) o;
        return Objects.equals(min, that.min) &&
                Objects.equals(max, that.max);
    }
}