package com.momosoftworks.coldsweat.data.codec.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.*;

public class StreamCodecs
{
    public static final StreamCodec<FriendlyByteBuf, Double> DOUBLE = StreamCodec.of(
            FriendlyByteBuf::writeDouble,
            FriendlyByteBuf::readDouble
    );

    public static <A, B, F extends FriendlyByteBuf> StreamCodec<F, Either<A, B>> either(StreamCodec<F, A> leftCodec, StreamCodec<F, B> rightCodec)
    {
        return StreamCodec.of((buf, either) ->
        {
            buf.writeBoolean(either.left().isPresent());
            either.ifLeft(left -> leftCodec.encode(buf, left))
                  .ifRight(right -> rightCodec.encode(buf, right));
        },
        buf ->
        {
            if (buf.readBoolean())
            {   return Either.left(leftCodec.decode(buf));
            }
            else
            {   return Either.right(rightCodec.decode(buf));
            }
        });
    }

    public static <A, B, F extends FriendlyByteBuf> StreamCodec<F, Pair<A, B>> pair(StreamCodec<F, A> leftCodec, StreamCodec<F, B> rightCodec)
    {
        return StreamCodec.of((buf, pair) ->
        {
            leftCodec.encode(buf, pair.getFirst());
            rightCodec.encode(buf, pair.getSecond());
        },
        buf -> Pair.of(leftCodec.decode(buf), rightCodec.decode(buf)));
    }

    public static <T, B extends FriendlyByteBuf> StreamCodec<B, Optional<T>> optional(StreamCodec<B, T> codec)
    {
        return StreamCodec.of((buf, optional) ->
        {
            if (optional.isPresent())
            {   buf.writeBoolean(true);
                codec.encode(buf, optional.get());
            }
            else
            {   buf.writeBoolean(false);
            }
        },
        buf ->
        {
            if (buf.readBoolean())
            {   return Optional.of(codec.decode(buf));
            }
            else
            {   return Optional.empty();
            }
        });
    }

    public static <T, B extends FriendlyByteBuf> StreamCodec<B, List<T>> list(StreamCodec<B, T> elementCodec)
    {
        return StreamCodec.of((buf, list) ->
        {
            buf.writeVarInt(list.size());
            for (T element : list)
            {   elementCodec.encode(buf, element);
            }
        },
        buf ->
        {
            int size = buf.readVarInt();
            List<T> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++)
            {   list.add(elementCodec.decode(buf));
            }
            return list;
        });
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, TagKey<T>> tagKey(ResourceKey<? extends Registry<T>> key)
    {
        return StreamCodec.of((buf, tagkey) ->
                              {
                                  buf.writeResourceKey(tagkey.registry());
                                  buf.writeResourceLocation(tagkey.location());
                              },
                              buf ->
                              {
                                  ResourceKey<? extends Registry<T>> registry = buf.readResourceKey(key).registryKey();
                                  ResourceLocation location = buf.readResourceLocation();
                                  return TagKey.create(registry, location);
                              });
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, Either<TagKey<T>, T>> tagOrRegistry(ResourceKey<? extends Registry<T>> key)
    {
        return StreamCodec.of((buf, either) ->
                              {
                                  buf.writeBoolean(either.left().isPresent());
                                  either.ifLeft(tag -> buf.writeUtf(tag.location().toString()))
                                        .ifRight(reg -> ByteBufCodecs.registry(key).encode(buf, reg));
                              },
                              buf ->
                              {
                                    if (buf.readBoolean())
                                    {   return Either.left(TagKey.create(key, ResourceLocation.parse(buf.readUtf())));
                                    }
                                    else
                                    {   return Either.right(ByteBufCodecs.registry(key).decode(buf));
                                    }
                              });
    }

    public static <K, V, U extends ByteBuf> StreamCodec<U, Map<K, V>> map(StreamCodec<U, K> keyCodec, StreamCodec<U, V> valueCodec)
    {
        return StreamCodec.of((buf, map) ->
        {
            buf.writeInt(map.size());
            for (Map.Entry<K, V> entry : map.entrySet())
            {
                keyCodec.encode(buf, entry.getKey());
                valueCodec.encode(buf, entry.getValue());
            }
        },
        buf ->
        {
            int size = buf.readInt();
            Map<K, V> map = new HashMap<>(size);
            for (int i = 0; i < size; i++)
            {
                K key = keyCodec.decode(buf);
                V value = valueCodec.decode(buf);
                map.put(key, value);
            }
            return map;
        });
    }

    public static <K, V, U extends ByteBuf> StreamCodec<U, Multimap<K, V>> multimap(StreamCodec<U, K> keyCodec, StreamCodec<U, V> valueCodec)
    {
        return StreamCodec.of((buf, multimap) ->
        {
            buf.writeInt(multimap.size());
            for (Map.Entry<K, V> entry : multimap.entries())
            {
                keyCodec.encode(buf, entry.getKey());
                valueCodec.encode(buf, entry.getValue());
            }
        },
        buf ->
        {
            int size = buf.readInt();
            Multimap<K, V> multimap = HashMultimap.create();
            for (int i = 0; i < size; i++)
            {
                K key = keyCodec.decode(buf);
                V value = valueCodec.decode(buf);
                multimap.put(key, value);
            }
            return multimap;
        });
    }
}
