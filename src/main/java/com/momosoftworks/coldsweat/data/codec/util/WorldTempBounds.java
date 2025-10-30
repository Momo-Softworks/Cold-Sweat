package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.requirement.WorldTempRequirement;
import com.momosoftworks.coldsweat.util.math.CSMath;

import java.util.Objects;

public class WorldTempBounds
{
    private final WorldTempRequirement min;
    private final WorldTempRequirement max;
    private final Temperature.Units units;

    public static final WorldTempBounds NONE = new WorldTempBounds(WorldTempRequirement.NEGATIVE_INFINITY, WorldTempRequirement.INFINITY, Temperature.Units.MC);

    public static final Codec<WorldTempBounds> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WorldTempRequirement.CODEC.optionalFieldOf("min", WorldTempRequirement.NEGATIVE_INFINITY).forGetter(bounds -> bounds.min),
            WorldTempRequirement.CODEC.optionalFieldOf("max", WorldTempRequirement.INFINITY).forGetter(bounds -> bounds.max),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(bounds -> bounds.units)
    ).apply(instance, WorldTempBounds::new));

    public static final Codec<WorldTempBounds> CODEC = Codec.either(DIRECT_CODEC, WorldTempRequirement.CODEC).xmap(
            either -> either.map(left -> left, right -> new WorldTempBounds(right, right, Temperature.Units.MC)),
            bounds -> bounds.max.equals(bounds.min) ? Either.right(bounds.min) : Either.left(bounds)
    );

    public WorldTempBounds(WorldTempRequirement min, WorldTempRequirement max, Temperature.Units units)
    {
        this.min = min;
        this.max = max;
        this.units = units;
    }

    public WorldTempRequirement min()
    {   return min;
    }

    public WorldTempRequirement max()
    {   return max;
    }

    public boolean test(double value)
    {   return value >= min.get(units) && value <= max.get(units);
    }

    public boolean contains(WorldTempBounds bounds)
    {   return bounds.min.get(units) >= min.get(units) && bounds.max.get(units) <= max.get(units);
    }

    public double getRandom()
    {   return min.get(units) + (Math.random() * (max.get(units) - min.get(units)));
    }

    public double clamp(double value)
    {   return CSMath.clamp(value, min.get(units), max.get(units));
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