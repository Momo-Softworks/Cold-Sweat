package com.momosoftworks.coldsweat.data.item_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SoulspringLampData(double fuel, boolean lit)
{
    public static final Codec<SoulspringLampData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("fuel", 64.0).forGetter(SoulspringLampData::fuel),
            Codec.BOOL.optionalFieldOf("lit", false).forGetter(SoulspringLampData::lit)
    ).apply(instance, SoulspringLampData::new));

    public SoulspringLampData()
    {   this(64, false);
    }

    public double fuel()
    {   return this.fuel;
    }
    public boolean lit()
    {   return this.lit;
    }

    public SoulspringLampData setFuel(double fuel)
    {   return new SoulspringLampData(fuel, this.lit);
    }
    public SoulspringLampData setLit(boolean lit)
    {   return new SoulspringLampData(this.fuel, lit);
    }
}
