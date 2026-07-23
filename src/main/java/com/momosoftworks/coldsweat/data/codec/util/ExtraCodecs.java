package com.momosoftworks.coldsweat.data.codec.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
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

    public static final Codec<EquipmentSlotType> EQUIPMENT_SLOT = Codec.STRING.xmap(EquipmentSlotType::byName, EquipmentSlotType::getName);

    private static final Map<String, RegistryKey<?>> REGISTRY_VALUES = Collections.synchronizedMap(Maps.newIdentityHashMap());

    public static <T> Codec<RegistryKey<T>> registry(RegistryKey<? extends Registry<T>> registry)
    {
        return ResourceLocation.CODEC.xmap((p_195979_) -> {
            return create(registry, p_195979_);
        }, RegistryKey::location);
    }

    public static <T> RegistryKey<T> create(RegistryKey<? extends Registry<T>> pRegistryKey, ResourceLocation pLocation)
    {
        String s = (pRegistryKey + ":" + pLocation).intern();
        return (RegistryKey<T>) REGISTRY_VALUES.computeIfAbsent(s, (p_195971_) -> {
            return RegistryKey.create(pRegistryKey, pLocation);
        });
    }

    public static Codec<Object> anyOf(Codec<?>... codecs)
    {
        return new Codec<Object>()
        {
            @Override
            public <T> DataResult<T> encode(Object input, DynamicOps<T> ops, T prefix)
            {
                for (Codec codec : codecs)
                {
                    try
                    {   DataResult<T> result = codec.encode(input, ops, prefix);
                        if (result.result().isPresent())
                        {
                            return result;
                        }
                    }
                    catch (ClassCastException ignored) {}
                }
                return DataResult.error("No codecs could encode input " + input);
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
                return DataResult.error("No codecs could decode input " + input);
            }
        };
    }

    public static <E> Codec<E> orCompressed(final Codec<E> pFirst, final Codec<E> pSecond)
    {
        return new Codec<E>()
        {
            public <T> DataResult<T> encode(E p_184483_, DynamicOps<T> p_184484_, T p_184485_)
            {
                return p_184484_.compressMaps()
                       ? pSecond.encode(p_184483_, p_184484_, p_184485_)
                       : pFirst.encode(p_184483_, p_184484_, p_184485_);
            }

            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> p_184480_, T p_184481_)
            {
                return p_184480_.compressMaps()
                       ? pSecond.decode(p_184480_, p_184481_)
                       : pFirst.decode(p_184480_, p_184481_);
            }

            public String toString()
            {   return pFirst + " orCompressed " + pSecond;
            }
        };
    }

    public static <E> Codec<E> stringResolverCodec(Function<E, String> encoder, Function<String, E> decoder)
    {
        return Codec.STRING.flatXmap((p_184404_) -> {
            return Optional.ofNullable(decoder.apply(p_184404_)).map(DataResult::success).orElseGet(
                    () -> DataResult.error("Unknown element name:" + p_184404_));
        }, (p_184401_) -> {
            return Optional.ofNullable(encoder.apply(p_184401_)).map(DataResult::success).orElseGet(
                    () -> DataResult.error("Element with unknown name: " + p_184401_));
        });
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> encoder, IntFunction<E> decoder, int id)
    {
        return Codec.INT.flatXmap((p_184414_) -> {
            return Optional.ofNullable(decoder.apply(p_184414_)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error("Unknown element id: " + p_184414_);
            });
        }, (p_274850_) -> {
            int i = encoder.applyAsInt(p_274850_);
            return i == id ? DataResult.error("Element with unknown id: " + p_274850_) : DataResult.success(i);
        });
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
        return new Codec<Set<A>>()
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
        return new Codec<T>() {
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

    public static <K extends IForgeRegistryEntry<K>, V> Codec<Map<K, V>> builtinMapCodec(IForgeRegistry<K> keyRegistry, Codec<V> valueCodec)
    {
        return new Codec<Map<K, V>>() {
            @Override
            public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input)
            {
                // Decode data
                Optional<Pair<Map<String, V>, T>> keyMapResult = Codec.unboundedMap(Codec.STRING, valueCodec).decode(ops, input).result();
                Map<String, V> keyMap = keyMapResult.orElseThrow(RuntimeException::new).getFirst();
                // Get registry
                Map<K, V> holderMap = new HashMap<>();
                // Put keys
                for (Map.Entry<String, V> entry : keyMap.entrySet())
                {   ResourceLocation id = new ResourceLocation(entry.getKey());
                    K key = keyRegistry.getValue(id);
                    if (key != null)
                    {   holderMap.put(key, entry.getValue());
                    }
                }
                return DataResult.success(Pair.of(holderMap, keyMapResult.map(Pair::getSecond).orElseThrow(RuntimeException::new)));
            }

            @Override
            public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix)
            {
                Map<String, V> keyMap = new HashMap<>();
                // Put keys
                for (Map.Entry<K, V> entry : input.entrySet())
                {   keyMap.put(keyRegistry.getKey(entry.getKey()).toString(), entry.getValue());
                }
                return Codec.unboundedMap(Codec.STRING, valueCodec).encode(keyMap, ops, prefix);
            }
        };
    }

    public static <K extends IForgeRegistryEntry<K>, V> Codec<Multimap<K, V>> builtinMultimapCodec(IForgeRegistry<K> keyRegistry, Codec<V> valueCodec)
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

    public static <K, V> Codec<Map<K, V>> registryMapCodec(RegistryKey<Registry<K>> keyRegistry, Codec<V> valueCodec)
    {
        return new Codec<Map<K, V>>() {
            @Override
            public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input)
            {
                // Decode data
                Optional<Pair<Map<String, V>, T>> keyMapResult = Codec.unboundedMap(Codec.STRING, valueCodec).decode(ops, input).result();
                Map<String, V> keyMap = keyMapResult.orElseThrow(RuntimeException::new).getFirst();
                // Get registry
                DynamicRegistries registryAccess = RegistryHelper.getDynamicRegistries();
                Registry<K> reg = registryAccess.registryOrThrow(keyRegistry);
                Map<K, V> holderMap = new HashMap<>();
                // Put keys
                for (Map.Entry<String, V> entry : keyMap.entrySet())
                {   RegistryKey<K> key = RegistryKey.create(keyRegistry, new ResourceLocation(entry.getKey()));
                    CSMath.doIfNotNull(reg.get(key), k -> holderMap.put(k, entry.getValue()));
                }
                return DataResult.success(Pair.of(holderMap, keyMapResult.map(Pair::getSecond).orElseThrow(RuntimeException::new)));
            }

            @Override
            public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix)
            {
                DynamicRegistries registryAccess = RegistryHelper.getDynamicRegistries();
                Registry<K> reg = registryAccess.registryOrThrow(keyRegistry);
                Map<String, V> keyMap = new HashMap<>();
                // Put keys
                for (Map.Entry<K, V> entry : input.entrySet())
                {   keyMap.put(reg.getKey(entry.getKey()).toString(), entry.getValue());
                }
                return Codec.unboundedMap(Codec.STRING, valueCodec).encode(keyMap, ops, prefix);
            }
        };
    }

    public static <K, V> Codec<Multimap<K, V>> registryMultimapCodec(RegistryKey<Registry<K>> keyRegistry, Codec<V> valueCodec)
    {
        return registryMapCodec(keyRegistry, valueCodec.listOf()).xmap(
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
