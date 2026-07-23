package com.momosoftworks.coldsweat.data.codec.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ExtraCodecs
{
    /**
     * A more lenient Double codec that can take in Integer values without exploding
     */
    public static final Codec<Double> DOUBLE = new Codec<Number>()
    {
        @Override
        public <T> DataResult<Pair<Number, T>> decode(final DynamicOps<T> ops, final T input)
        {   return ops.getNumberValue(input).map(n -> Pair.of(n, input));
        }

        @Override
        public <T> DataResult<T> encode(Number input, DynamicOps<T> ops, T prefix)
        {   return ops.mergeToPrimitive(prefix, ops.createNumeric(input));
        }

        @Override
        public String toString()
        {   return "Double";
        }
    }.xmap(Number::doubleValue, n -> n);

    public static Codec<Object> anyOf(Codec<?>... codecs)
    {
        return new Codec<>()
        {
            @Override
            public <T> DataResult<T> encode(Object input, DynamicOps<T> ops, T prefix)
            {
                for (Codec codec : codecs)
                {
                    try
                    {   DataResult<T> result = codec.encode(input, ops, prefix);
                        if (result.result().isPresent())
                        {   return result;
                        }
                    }
                    catch (ClassCastException ignored) {}
                }
                return DataResult.error(() -> "No codecs could encode input " + input);
            }

            @Override
            public <T> DataResult<Pair<Object, T>> decode(DynamicOps<T> ops, T input)
            {
                for (Codec codec : codecs)
                {
                    DataResult<Pair<Object, T>> result = codec.decode(ops, input);
                    if (result.result().isPresent())
                    {   return result;
                    }
                }
                return DataResult.error(() -> "No codecs could decode input " + input);
            }
        };
    }

    public static <F, S> Codec<Pair<F, S>> pair(Codec<F> firstCodec, Codec<S> secondCodec)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                firstCodec.fieldOf("first").forGetter(Pair::getFirst),
                secondCodec.fieldOf("second").forGetter(Pair::getSecond)
        ).apply(instance, Pair::of));
    }

    public static <A> Codec<Set<A>> setOf(Codec<A> elementCodec)
    {
        return new Codec<>()
        {
            @Override
            public <T> DataResult<Pair<Set<A>, T>> decode(DynamicOps<T> ops, T input)
            {
                return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream ->
                {
                    final ImmutableSet.Builder<A> read = ImmutableSet.builder();
                    final Stream.Builder<T> failed = Stream.builder();
                    final MutableObject<DataResult<Unit>> result = new MutableObject<>(DataResult.success(Unit.INSTANCE, Lifecycle.stable()));

                    stream.accept(t ->
                    {
                        final DataResult<Pair<A, T>> element = elementCodec.decode(ops, t);
                        element.error().ifPresent(e -> failed.add(t));
                        result.setValue(result.getValue().apply2stable((r, v) ->
                        {   read.add(v.getFirst());
                            return r;
                        }, element));
                    });

                    final ImmutableSet<A> elements = read.build();
                    final T errors = ops.createList(failed.build());

                    final Pair<Set<A>, T> pair = Pair.of(elements, errors);

                    return result.getValue().map(unit -> pair).setPartial(pair);
                });
            }

            @Override
            public <T> DataResult<T> encode(Set<A> input, DynamicOps<T> ops, T prefix)
            {
                final ListBuilder<T> builder = ops.listBuilder();

                for (final A a : input)
                {   builder.add(elementCodec.encodeStart(ops, a));
                }
                return builder.build(prefix);
            }
        };
    }

    public static <T> Codec<T> deferred(Supplier<Codec<T>> codecSupplier)
    {
        return new Codec<>() {
            @Override
            public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> ops, U input)
            {   return codecSupplier.get().decode(ops, input);
            }

            @Override
            public <U> DataResult<U> encode(T input, DynamicOps<U> ops, U prefix)
            {   return codecSupplier.get().encode(input, ops, prefix);
            }
        };
    }

    public static <K, V> Codec<Map<K, V>> builtinMapCodec(Registry<K> keyRegistry, Codec<V> valueCodec)
    {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input)
            {
                // Decode data
                DynamicOps<T> decoderOps = RegistryOps.create(ops, RegistryHelper.getRegistryAccess());
                Optional<Pair<Map<String, V>, T>> keyMapResult = Codec.unboundedMap(Codec.STRING, valueCodec).decode(decoderOps, input).result();
                Map<String, V> keyMap = keyMapResult.orElseThrow().getFirst();
                // Get registry
                Map<K, V> holderMap = new HashMap<>();
                // Put keys
                for (Map.Entry<String, V> entry : keyMap.entrySet())
                {   ResourceLocation id = ResourceLocation.parse(entry.getKey());
                    K key = keyRegistry.get(id);
                    if (key != null)
                    {   holderMap.put(key, entry.getValue());
                    }
                }
                return DataResult.success(Pair.of(holderMap, keyMapResult.map(Pair::getSecond).orElseThrow()));
            }

            @Override
            public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix)
            {
                DynamicOps<T> encoderOps = RegistryOps.create(ops, RegistryHelper.getRegistryAccess());
                Map<String, V> keyMap = new HashMap<>();
                // Put keys
                for (Map.Entry<K, V> entry : input.entrySet())
                {   keyMap.put(keyRegistry.getKey(entry.getKey()).toString(), entry.getValue());
                }
                return Codec.unboundedMap(Codec.STRING, valueCodec).encode(keyMap, encoderOps, prefix);
            }
        };
    }

    public static <K, V> Codec<Multimap<K, V>> builtinMultimapCodec(Registry<K> keyRegistry, Codec<V> valueCodec)
    {
        return builtinMapCodec(keyRegistry, valueCodec.listOf()).xmap(
                map -> {
                    Multimap<K, V> multimap = HashMultimap.create();
                    for (Map.Entry<K, List<V>> entry : map.entrySet())
                    {   multimap.putAll(entry.getKey(), entry.getValue());
                    }
                    return multimap;
                },
                multimap -> {
                    Map<K, List<V>> fastMultiMap = new HashMap<>();
                    for (Map.Entry<K, Collection<V>> entry : multimap.asMap().entrySet())
                    {   fastMultiMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                    }
                    return fastMultiMap;
                }
        );
    }

    public static <K, V> Codec<Map<Holder<K>, V>> registryMapCodec(ResourceKey<Registry<K>> keyRegistry, Codec<V> valueCodec)
    {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<Map<Holder<K>, V>, T>> decode(DynamicOps<T> ops, T input)
            {
                // Get registry
                RegistryAccess registryAccess = RegistryHelper.getRegistryAccess();
                Registry<K> reg = registryAccess.registryOrThrow(keyRegistry);
                // Decode data
                DynamicOps<T> decoderOps = registryAccess.createSerializationContext(ops);
                Optional<Pair<Map<String, V>, T>> keyMapResult = Codec.unboundedMap(Codec.STRING, valueCodec).decode(decoderOps, input).result();
                Map<String, V> keyMap = keyMapResult.orElseThrow().getFirst();
                // Put keys
                Map<Holder<K>, V> holderMap = new HashMap<>();
                for (Map.Entry<String, V> entry : keyMap.entrySet())
                {   ResourceKey<K> key = ResourceKey.create(keyRegistry, ResourceLocation.parse(entry.getKey()));
                    reg.getHolder(key).ifPresent(k -> holderMap.put(k, entry.getValue()));
                }
                return DataResult.success(Pair.of(holderMap, keyMapResult.map(Pair::getSecond).orElseThrow()));
            }

            @Override
            public <T> DataResult<T> encode(Map<Holder<K>, V> input, DynamicOps<T> ops, T prefix)
            {
                RegistryOps<T> encoderOps = RegistryHelper.getRegistryAccess().createSerializationContext(ops);
                Map<String, V> keyMap = new HashMap<>();
                // Put keys
                for (Map.Entry<Holder<K>, V> entry : input.entrySet())
                {   entry.getKey().unwrapKey().ifPresent(k -> keyMap.put(k.location().toString(), entry.getValue()));
                }
                return Codec.unboundedMap(Codec.STRING, valueCodec).encode(keyMap, encoderOps, prefix);
            }
        };
    }

    public static <K, V> Codec<Multimap<Holder<K>, V>> registryMultimapCodec(ResourceKey<Registry<K>> keyRegistry, Codec<V> valueCodec)
    {
        return registryMapCodec(keyRegistry, valueCodec.listOf()).xmap(
                map -> {
                    Multimap<Holder<K>, V> multimap = HashMultimap.create();
                    for (Map.Entry<Holder<K>, List<V>> entry : map.entrySet())
                    {   multimap.putAll(entry.getKey(), entry.getValue());
                    }
                    return multimap;
                },
                multimap -> {
                    Map<Holder<K>, List<V>> fastMultiMap = new HashMap<>();
                    for (Map.Entry<Holder<K>, Collection<V>> entry : multimap.asMap().entrySet())
                    {   fastMultiMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                    }
                    return fastMultiMap;
                }
        );
    }

    public static <T extends Enum<T> & StringRepresentable> Codec<T> enumIgnoreCase(T[] values)
    {
        return Codec.STRING.xmap(
            str -> {
                if (values.length == 0) throw new IllegalArgumentException("Enum has no values");
                for (T value : values)
                {   if (value.getSerializedName().equalsIgnoreCase(str))
                    {   return value;
                    }
                }
                throw new IllegalArgumentException(String.format("Unknown %s value: %s", values[0].getClass().getSimpleName(), str));
            },
            T::getSerializedName
        );
    }
}
