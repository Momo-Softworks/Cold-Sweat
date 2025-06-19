package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public record EntityDropData(int interval, int cooldown, double chance)
{
    public static final Codec<EntityDropData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("interval").forGetter(EntityDropData::interval),
            Codec.INT.fieldOf("cooldown").forGetter(EntityDropData::cooldown),
            Codec.DOUBLE.fieldOf("chance").forGetter(EntityDropData::chance)
    ).apply(builder, EntityDropData::new));
}
