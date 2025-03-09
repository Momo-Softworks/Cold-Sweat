package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;

import java.util.Map;
import java.util.function.Supplier;

public record WorldTempRequirement(Either<Double, String> temperature)
{
    public static final WorldTempRequirement INFINITY = new WorldTempRequirement(Double.POSITIVE_INFINITY);
    public static final WorldTempRequirement NEGATIVE_INFINITY = new WorldTempRequirement(Double.NEGATIVE_INFINITY);

    public static final Map<String, Supplier<Double>> VARIABLES = Map.of(
            "temperate", () -> CSMath.average(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get()),
            "warm", () -> CSMath.blend(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), 0.67, 0, 1),
            "hot", () -> CSMath.blend(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), 0.83, 0, 1),
            "burning", ConfigSettings.MAX_TEMP::get,
            "cool", () -> CSMath.blend(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), 0.33, 0, 1),
            "cold", () -> CSMath.blend(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), 0.17, 0, 1),
            "freezing", ConfigSettings.MIN_TEMP::get);

    public static final Codec<WorldTempRequirement> CODEC = Codec.either(Codec.DOUBLE, Codec.STRING)
            .xmap(either -> {
                    if (either.right().isPresent() && !VARIABLES.containsKey(either.right().get()))
                    {   throw new IllegalArgumentException("Unknown temperature variable: " + either.right().get());
                    }
                    return new WorldTempRequirement(either);
                  },
                  WorldTempRequirement::temperature);

    public WorldTempRequirement(double temperature)
    {   this(Either.left(temperature));
    }
    public WorldTempRequirement(String temperature)
    {   this(Either.right(temperature));
    }

    public double get()
    {
        if (temperature.left().isPresent())
        {   return temperature.left().get();
        }
        return VARIABLES.get(temperature.right().get()).get();
    }

    public boolean isConstant()
    {   return temperature.left().isPresent();
    }
}
