package com.momosoftworks.coldsweat.data.codec.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;

public class AttributeModifierMap
{
    public static final Codec<AttributeModifierMap> CODEC = Codec.unboundedMap(Attribute.CODEC, AttributeCodecs.MODIFIER_CODEC.listOf())
            .xmap(AttributeModifierMap::new,
                  map -> map.getMap().asMap().entrySet()
                            .stream()
                            .collect(HashMap::new,
                                     (mp, ent) -> mp.put(ent.getKey(), new ArrayList<>(ent.getValue())),
                                     HashMap::putAll));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeModifierMap> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private final Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();

    public AttributeModifierMap()
    {
    }

    public AttributeModifierMap(Map<Holder<Attribute>, ?> attributeListMap)
    {   attributeListMap.forEach((attribute, list) ->
        {   if (list instanceof Collection)
            {   map.putAll(attribute, (Collection<AttributeModifier>) list);
            }
            else if (list instanceof AttributeModifier)
            {   map.put(attribute, (AttributeModifier) list);
            }
        });
    }

    public AttributeModifierMap(Multimap<Holder<Attribute>, AttributeModifier> map)
    {   this.map.putAll(map);
    }

    public AttributeModifierMap(AttributeModifierMap original)
    {   this.map.putAll(original.map);
    }

    public void put(Holder<Attribute> attribute, AttributeModifier modifier)
    {   map.put(attribute, modifier);
    }

    public Collection<AttributeModifier> get(Holder<Attribute> attribute)
    {   return map.get(attribute);
    }

    public Multimap<Holder<Attribute>, AttributeModifier> getMap()
    {   return map;
    }

    public AttributeModifierMap putAll(AttributeModifierMap other)
    {   map.putAll(other.map);
        return this;
    }

    public AttributeModifierMap putAll(Holder<Attribute> attribute, Collection<AttributeModifier> modifiers)
    {   map.putAll(attribute, modifiers);
        return this;
    }

    public boolean isEmpty()
    {   return map.isEmpty();
    }

    public void clear()
    {   map.clear();
    }

    public static AttributeModifierMap deserialize(CompoundTag tag)
    {   return CODEC.decode(NbtOps.INSTANCE, tag).result().orElseThrow(() -> new IllegalArgumentException("Could not deserialize AttributeModifierMap")).getFirst();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AttributeModifierMap that = (AttributeModifierMap) obj;
        for (Map.Entry<Holder<Attribute>, Collection<AttributeModifier>> entry : map.asMap().entrySet())
        {
            if (!that.map.containsKey(entry.getKey())) return false;

            Collection<AttributeModifier> other = that.map.get(entry.getKey());
            if (entry.getValue().size() != other.size()) return false;

            Iterator<AttributeModifier> thatIterator = other.iterator();
            for (AttributeModifier modifier : entry.getValue())
            {
                if (!thatIterator.hasNext()) return false;
                AttributeModifier thatModifier = thatIterator.next();
                if (!(Double.compare(modifier.amount(), thatModifier.amount()) == 0
                      && modifier.operation() == thatModifier.operation()
                      && modifier.id().equals(thatModifier.id())))
                {
                    return false;
                }
            }
        }
        return true;
    }
}
