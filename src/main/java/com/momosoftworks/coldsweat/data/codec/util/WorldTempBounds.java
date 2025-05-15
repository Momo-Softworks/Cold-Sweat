package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.WorldTempRequirement;
import com.momosoftworks.coldsweat.util.math.CSMath;

public record WorldTempBounds(WorldTempRequirement min, WorldTempRequirement max)
{
    public static final WorldTempBounds NONE = new WorldTempBounds(WorldTempRequirement.NEGATIVE_INFINITY, WorldTempRequirement.INFINITY);

    public static final Codec<WorldTempBounds> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WorldTempRequirement.CODEC.optionalFieldOf("min", WorldTempRequirement.NEGATIVE_INFINITY).forGetter(bounds -> bounds.min),
            WorldTempRequirement.CODEC.optionalFieldOf("max", WorldTempRequirement.INFINITY).forGetter(bounds -> bounds.max)
    ).apply(instance, WorldTempBounds::new));

    public static final Codec<WorldTempBounds> CODEC = Codec.either(DIRECT_CODEC, WorldTempRequirement.CODEC).xmap(
            either -> either.map(left -> left, right -> new WorldTempBounds(right, right)),
            bounds -> bounds.max == bounds.min ? Either.right(bounds.min) : Either.left(bounds)
    );

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
}
