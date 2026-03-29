package com.momosoftworks.coldsweat.common.capability.soul_lamp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SoulspringLampData(double fuel, boolean lit)
{
    public static final Codec<SoulspringLampData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("fuel").forGetter(SoulspringLampData::fuel),
            Codec.BOOL.fieldOf("lit").forGetter(SoulspringLampData::lit)
    ).apply(instance, SoulspringLampData::new));

    public SoulspringLampData()
    {   this(0, false);
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
