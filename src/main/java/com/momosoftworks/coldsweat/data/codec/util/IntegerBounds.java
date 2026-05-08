package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record IntegerBounds(int min, int max)
{
    public static final Codec<IntegerBounds> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("min", -Integer.MAX_VALUE).forGetter(bounds -> bounds.min),
            Codec.INT.optionalFieldOf("max", Integer.MAX_VALUE).forGetter(bounds -> bounds.max)
    ).apply(instance, IntegerBounds::new));

    public static final Codec<IntegerBounds> CODEC = Codec.either(DIRECT_CODEC, Codec.INT).xmap(
            either -> either.map(left -> left, right -> new IntegerBounds(right, right)),
            bounds -> bounds.max == bounds.min ? Either.right(bounds.min) : Either.left(bounds)
    );

    public static final StreamCodec<ByteBuf, IntegerBounds> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static IntegerBounds NONE = new IntegerBounds(-Integer.MAX_VALUE, Integer.MAX_VALUE);

    public IntegerBounds(Integer[] bounds)
    {   this(bounds[0], bounds[1]);
    }

    public boolean test(int value)
    {   return CSMath.betweenInclusive(value, min, max);
    }

    public boolean contains(IntegerBounds bounds)
    {   return test(bounds.min()) && test(bounds.max());
    }

    public int getRandom()
    {
        int realMin = Math.min(min, max);
        int realMax = Math.max(min, max);
        return realMin + (int) (Math.random() * (realMax - realMin + 1));
    }

    public int clamp(int value)
    {
        int realMin = Math.min(min, max);
        int realMax = Math.max(min, max);
        return CSMath.clamp(value, realMin, realMax);
    }

    public String toString()
    {   return String.format("[%d~%d]", this.min, this.max);
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

        IntegerBounds that = (IntegerBounds) obj;

        if (!Objects.equals(min, that.min))
        {   return false;
        }
        return Objects.equals(max, that.max);
    }
}
