package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class WorldTempRequirement
{
    public static final WorldTempRequirement INFINITY = new WorldTempRequirement(Double.POSITIVE_INFINITY);
    public static final WorldTempRequirement NEGATIVE_INFINITY = new WorldTempRequirement(Double.NEGATIVE_INFINITY);

    public static final Map<String, Supplier<Double>> VARIABLES = CSMath.mapOf(
            "temperate", () -> CSMath.average(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get()),
            "warm", () -> CSMath.blend(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), 0.67, 0, 1),
            "hot", () -> CSMath.blend(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), 0.83, 0, 1),
            "burning", () -> ConfigSettings.MAX_TEMP.get(),
            "cool", () -> CSMath.blend(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), 0.33, 0, 1),
            "cold", () -> CSMath.blend(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), 0.17, 0, 1),
            "freezing", () -> ConfigSettings.MAX_TEMP.get());

    public static final Codec<WorldTempRequirement> CODEC = Codec.either(Codec.DOUBLE, Codec.STRING)
            .xmap(either -> {
                      if (either.right().isPresent() && !VARIABLES.containsKey(either.right().get()))
                      {   throw new IllegalArgumentException("Unknown temperature variable: " + either.right().get());
                      }
                      return new WorldTempRequirement(either);
                  },
                  WorldTempRequirement::temperature);

    private final Either<Double, String> temperature;

    public WorldTempRequirement(Either<Double, String> temperature)
    {   this.temperature = temperature;
    }

    public WorldTempRequirement(double temperature)
    {   this(Either.left(temperature));
    }

    public WorldTempRequirement(String temperature)
    {   this(Either.right(temperature));
    }

    public Either<Double, String> temperature()
    {   return temperature;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldTempRequirement that = (WorldTempRequirement) o;
        return Objects.equals(temperature, that.temperature);
    }
}