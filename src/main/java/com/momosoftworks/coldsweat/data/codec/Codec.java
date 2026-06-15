package com.momosoftworks.coldsweat.data.codec;

import com.momosoftworks.coldsweat.data.ops.CodecOps;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface Codec<T> extends Encoder<T>, Decoder<T>
{
    static <T> Codec<T> create(Encoder<T> encoder, Decoder<T> decoder)
    {   return new Codec<T>()
        {
            @Override
            public <E> Optional<E> encode(CodecOps<E> ops, T obj)
            {   return encoder.encode(ops, obj);
            }

            @Override
            public <E> Optional<T> decode(CodecOps<E> ops, E obj)
            {   return decoder.decode(ops, obj);
            }
        };
    }

    /**
     * Derives a new codec that maps this codec's type {@code T} to/from another type {@code U}.<br>
     * Useful for building codecs out of {@link #STRING} (ResourceLocation, enums, etc.).
     */
    default <U> Codec<U> xmap(Function<? super T, ? extends U> to, Function<? super U, ? extends T> from)
    {
        Codec<T> self = this;
        return create(new Encoder<U>()
        {
            @Override
            public <E> Optional<E> encode(CodecOps<E> ops, U obj)
            {   return self.encode(ops, from.apply(obj));
            }
        }, new Decoder<U>()
        {
            @Override
            public <E> Optional<U> decode(CodecOps<E> ops, E obj)
            {   return self.decode(ops, obj).map(to);
            }
        });
    }

    /**
     * Builds a codec for a {@link List} of {@code T}, serialized as a list in the target format.
     */
    static <T> Codec<List<T>> list(Codec<T> elementCodec)
    {
        return create(new Encoder<List<T>>()
        {
            @Override
            public <E> Optional<E> encode(CodecOps<E> ops, List<T> obj)
            {
                E list = ops.createList();
                for (T element : obj)
                {
                    Optional<E> encoded = elementCodec.encode(ops, element);
                    if (!encoded.isPresent()) return Optional.empty();
                    ops.add(list, encoded.get());
                }
                return Optional.of(list);
            }
        }, new Decoder<List<T>>()
        {
            @Override
            public <E> Optional<List<T>> decode(CodecOps<E> ops, E obj)
            {
                if (obj == null) return Optional.empty();
                List<T> result = new ArrayList<T>();
                for (E element : ops.getList(obj))
                {
                    Optional<T> decoded = elementCodec.decode(ops, element);
                    if (!decoded.isPresent()) return Optional.empty();
                    result.add(decoded.get());
                }
                return Optional.of(result);
            }
        });
    }

    Codec<Boolean> BOOL = create(CodecOps::fromBool, new Decoder<Boolean>()
    {
        @Override
        public <E> Optional<Boolean> decode(CodecOps<E> ops, E obj)
        {   return ops.toBool(obj);
        }
    });
    Codec<String> STRING = create(CodecOps::fromString, new Decoder<String>()
    {
        @Override
        public <E> Optional<String> decode(CodecOps<E> ops, E obj)
        {   return ops.toString(obj);
        }
    });
    // Numeric decoders all route through toNumber() so a value authored as a different
    // numeric type (e.g. a double in JSON for an int field) still decodes correctly.
    Codec<Byte> BYTE = create(CodecOps::fromByte, new Decoder<Byte>()
    {
        @Override
        public <E> Optional<Byte> decode(CodecOps<E> ops, E obj)
        {   return ops.toNumber(obj).map(Number::byteValue);
        }
    });
    Codec<Short> SHORT = create(CodecOps::fromShort, new Decoder<Short>()
    {
        @Override
        public <E> Optional<Short> decode(CodecOps<E> ops, E obj)
        {   return ops.toNumber(obj).map(Number::shortValue);
        }
    });
    Codec<Integer> INT = create(CodecOps::fromInt, new Decoder<Integer>()
    {
        @Override
        public <E> Optional<Integer> decode(CodecOps<E> ops, E obj)
        {   return ops.toNumber(obj).map(Number::intValue);
        }
    });
    Codec<Long> LONG = create(CodecOps::fromLong, new Decoder<Long>()
    {
        @Override
        public <E> Optional<Long> decode(CodecOps<E> ops, E obj)
        {   return ops.toNumber(obj).map(Number::longValue);
        }
    });
    Codec<Double> DOUBLE = create(CodecOps::fromDouble, new Decoder<Double>()
    {
        @Override
        public <E> Optional<Double> decode(CodecOps<E> ops, E obj)
        {   return ops.toNumber(obj).map(Number::doubleValue);
        }
    });
}
