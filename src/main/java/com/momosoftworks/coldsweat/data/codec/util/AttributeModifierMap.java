package com.momosoftworks.coldsweat.data.codec.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.math.FastMultiMap;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;

public class AttributeModifierMap implements NbtSerializable
{
    public static final Codec<AttributeModifierMap> CODEC = Codec.unboundedMap(AttributeCodecs.ATTRIBUTE_CODEC, AttributeCodecs.MODIFIER_CODEC.listOf())
            .xmap(AttributeModifierMap::new,
                  map -> map.getMap().asMap().entrySet()
                            .stream()
                            .collect(HashMap::new,
                                     (mp, ent) -> mp.put(ent.getKey(), new ArrayList<>(ent.getValue())),
                                     HashMap::putAll));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeModifierMap> STREAM_CODEC = StreamCodec.of(
            (buf, map) ->
            {
                buf.writeVarInt(map.getMap().size());
                for (Attribute attribute : map.getMap().keySet())
                {
                    buf.writeResourceLocation(BuiltInRegistries.ATTRIBUTE.getKey(attribute));
                    buf.writeCollection(map.get(attribute), AttributeCodecs.MODIFIER_STREAM_CODEC);
                }
            },
            (buf) ->
            {
                Multimap<Attribute, AttributeModifier> map = new FastMultiMap<>();
                int size = buf.readVarInt();
                for (int i = 0; i < size; i++)
                {
                    Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(buf.readResourceLocation());
                    List<AttributeModifier> list = buf.readCollection(ArrayList::new, AttributeCodecs.MODIFIER_STREAM_CODEC);
                    map.putAll(attribute, list);
                }
                return new AttributeModifierMap(map);
            }
    );

    private final Multimap<Attribute, AttributeModifier> map = HashMultimap.create();

    public AttributeModifierMap()
    {
    }

    public AttributeModifierMap(Map<Attribute, ?> attributeListMap)
    {   attributeListMap.forEach((attribute, list) ->
        {   if (list instanceof Collection)
            {   map.putAll(attribute, (Collection<AttributeModifier>) list);
            }
            else if (list instanceof AttributeModifier)
            {   map.put(attribute, (AttributeModifier) list);
            }
        });
    }

    public AttributeModifierMap(Multimap<Attribute, AttributeModifier> map)
    {   this.map.putAll(map);
    }

    public void put(Attribute attribute, AttributeModifier modifier)
    {   map.put(attribute, modifier);
    }

    public Collection<AttributeModifier> get(Attribute attribute)
    {   return map.get(attribute);
    }

    public Multimap<Attribute, AttributeModifier> getMap()
    {   return map;
    }

    public AttributeModifierMap putAll(AttributeModifierMap other)
    {   map.putAll(other.map);
        return this;
    }

    public AttributeModifierMap putAll(Attribute attribute, Collection<AttributeModifier> modifiers)
    {   map.putAll(attribute, modifiers);
        return this;
    }

    public boolean isEmpty()
    {   return map.isEmpty();
    }

    @Override
    public CompoundTag serialize()
    {
        return ((CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(new CompoundTag()));
    }

    public static AttributeModifierMap deserialize(CompoundTag tag)
    {
        return CODEC.decode(NbtOps.INSTANCE, tag).result().orElseThrow(() -> new IllegalArgumentException("Could not deserialize AttributeModifierMap")).getFirst();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AttributeModifierMap that = (AttributeModifierMap) obj;
        return map.equals(that.map);
    }
}
