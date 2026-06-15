package com.momosoftworks.coldsweat.data.codec;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.ops.CodecOps;
import com.momosoftworks.coldsweat.util.math.Pair;
import com.momosoftworks.coldsweat.util.math.Triplet;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reusable codecs for the types Cold Sweat's config settings hold (registry objects, pairs, triplets,
 * maps, arrays). Built on the manual {@link Codec} system so config values serialize/sync uniformly,
 * the way 1.16 uses Mojang codecs.
 */
public class ConfigCodecs
{
    private ConfigCodecs() {}

    public static final Codec<ResourceLocation> RESOURCE_LOCATION =
            Codec.STRING.xmap(ResourceLocation::new, ResourceLocation::toString);

    public static final Codec<Temperature.Units> UNITS =
            Codec.STRING.xmap(s -> Temperature.Units.valueOf(s.toUpperCase()), Enum::name);

    /** Lenient item codec: encodes to the registry id string, decodes back (empty if the item is missing). */
    public static final Codec<Item> ITEM = Codec.create(new Encoder<Item>()
    {
        @Override
        public <E> Optional<E> encode(CodecOps<E> ops, Item obj)
        {   return ConfigHelper.getItemID(obj).flatMap(id -> ops.fromString(id.toString()));
        }
    }, new Decoder<Item>()
    {
        @Override
        public <E> Optional<Item> decode(CodecOps<E> ops, E obj)
        {   return ops.toString(obj).flatMap(ConfigHelper::getItem);
        }
    });

    /** Lenient block codec. */
    public static final Codec<Block> BLOCK = Codec.create(new Encoder<Block>()
    {
        @Override
        public <E> Optional<E> encode(CodecOps<E> ops, Block obj)
        {   return ConfigHelper.getBlockID(obj).flatMap(id -> ops.fromString(id.toString()));
        }
    }, new Decoder<Block>()
    {
        @Override
        public <E> Optional<Block> decode(CodecOps<E> ops, E obj)
        {   return ops.toString(obj).flatMap(ConfigHelper::getBlock);
        }
    });

    /**
     * A {@link Pair} serialized as {@code {first, second}}.
     */
    public static <A, B> Codec<Pair<A, B>> pair(final Codec<A> firstCodec, final Codec<B> secondCodec)
    {
        return Codec.create(new Encoder<Pair<A, B>>()
        {
            @Override
            public <E> Optional<E> encode(CodecOps<E> ops, Pair<A, B> obj)
            {
                Optional<E> first = firstCodec.encode(ops, obj.getFirst());
                Optional<E> second = secondCodec.encode(ops, obj.getSecond());
                if (!first.isPresent() || !second.isPresent()) return Optional.empty();
                E map = ops.createMap();
                ops.put(map, "first", first.get());
                ops.put(map, "second", second.get());
                return Optional.of(map);
            }
        }, new Decoder<Pair<A, B>>()
        {
            @Override
            public <E> Optional<Pair<A, B>> decode(CodecOps<E> ops, E obj)
            {
                if (obj == null) return Optional.empty();
                Optional<A> first = firstCodec.decode(ops, ops.get(obj, "first"));
                Optional<B> second = secondCodec.decode(ops, ops.get(obj, "second"));
                if (!first.isPresent() || !second.isPresent()) return Optional.empty();
                return Optional.of(Pair.of(first.get(), second.get()));
            }
        });
    }

    /**
     * A {@link Triplet} serialized as {@code {first, second, third}}.
     */
    public static <A, B, C> Codec<Triplet<A, B, C>> triplet(final Codec<A> firstCodec, final Codec<B> secondCodec, final Codec<C> thirdCodec)
    {
        return Codec.create(new Encoder<Triplet<A, B, C>>()
        {
            @Override
            public <E> Optional<E> encode(CodecOps<E> ops, Triplet<A, B, C> obj)
            {
                Optional<E> first = firstCodec.encode(ops, obj.getFirst());
                Optional<E> second = secondCodec.encode(ops, obj.getSecond());
                Optional<E> third = thirdCodec.encode(ops, obj.getThird());
                if (!first.isPresent() || !second.isPresent() || !third.isPresent()) return Optional.empty();
                E map = ops.createMap();
                ops.put(map, "first", first.get());
                ops.put(map, "second", second.get());
                ops.put(map, "third", third.get());
                return Optional.of(map);
            }
        }, new Decoder<Triplet<A, B, C>>()
        {
            @Override
            public <E> Optional<Triplet<A, B, C>> decode(CodecOps<E> ops, E obj)
            {
                if (obj == null) return Optional.empty();
                Optional<A> first = firstCodec.decode(ops, ops.get(obj, "first"));
                Optional<B> second = secondCodec.decode(ops, ops.get(obj, "second"));
                Optional<C> third = thirdCodec.decode(ops, ops.get(obj, "third"));
                if (!first.isPresent() || !second.isPresent() || !third.isPresent()) return Optional.empty();
                return Optional.of(new Triplet<A, B, C>(first.get(), second.get(), third.get()));
            }
        });
    }

    /**
     * A {@link Map} serialized as a list of {@code {key, value}} entries.<br>
     * Decoding is lenient: entries that fail to decode (e.g. an item from an absent mod) are skipped.
     */
    public static <K, V> Codec<Map<K, V>> map(final Codec<K> keyCodec, final Codec<V> valueCodec)
    {
        return Codec.create(new Encoder<Map<K, V>>()
        {
            @Override
            public <E> Optional<E> encode(CodecOps<E> ops, Map<K, V> obj)
            {
                E list = ops.createList();
                for (Map.Entry<K, V> entry : obj.entrySet())
                {
                    Optional<E> key = keyCodec.encode(ops, entry.getKey());
                    Optional<E> value = valueCodec.encode(ops, entry.getValue());
                    if (!key.isPresent() || !value.isPresent()) continue;
                    E entryMap = ops.createMap();
                    ops.put(entryMap, "key", key.get());
                    ops.put(entryMap, "value", value.get());
                    ops.add(list, entryMap);
                }
                return Optional.of(list);
            }
        }, new Decoder<Map<K, V>>()
        {
            @Override
            public <E> Optional<Map<K, V>> decode(CodecOps<E> ops, E obj)
            {
                Map<K, V> result = new LinkedHashMap<K, V>();
                if (obj == null) return Optional.of(result);
                for (E element : ops.getList(obj))
                {
                    Optional<K> key = keyCodec.decode(ops, ops.get(element, "key"));
                    Optional<V> value = valueCodec.decode(ops, ops.get(element, "value"));
                    if (key.isPresent() && value.isPresent())
                    {   result.put(key.get(), value.get());
                    }
                }
                return Optional.of(result);
            }
        });
    }

    /**
     * An array codec built from an element codec, serialized as a list.
     */
    public static <T> Codec<T[]> array(Codec<T> elementCodec, final T[] empty)
    {
        return Codec.list(elementCodec).xmap(
            list -> list.toArray(Arrays.copyOf(empty, 0)),
            arr -> new ArrayList<T>(Arrays.asList(arr)));
    }

    public static final Codec<Integer[]> INT_ARRAY = array(Codec.INT, new Integer[0]);
    public static final Codec<Double[]> DOUBLE_ARRAY = array(Codec.DOUBLE, new Double[0]);
}
