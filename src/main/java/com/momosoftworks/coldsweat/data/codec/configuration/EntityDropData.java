package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class EntityDropData
{
    private final int interval;
    private final int cooldown;
    private final double chance;

    public EntityDropData(int interval, int cooldown, double chance)
    {
        this.interval = interval;
        this.cooldown = cooldown;
        this.chance = chance;
    }
    public static final Codec<EntityDropData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("interval").forGetter(EntityDropData::interval),
            Codec.INT.fieldOf("cooldown").forGetter(EntityDropData::cooldown),
            Codec.DOUBLE.fieldOf("chance").forGetter(EntityDropData::chance)
    ).apply(builder, EntityDropData::new));

    public int interval()
    {   return interval;
    }
    public int cooldown()
    {   return cooldown;
    }
    public double chance()
    {   return chance;
    }
}
