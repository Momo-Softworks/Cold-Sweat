package com.momosoftworks.coldsweat.data.codec.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
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
        CompoundTag tag = new CompoundTag();
        map.asMap().forEach((attribute, modifier) ->
        {   String key = BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString();
            ListTag list = new ListTag();
            modifier.forEach(mod -> list.add(mod.save()));
            tag.put(key, list);
        });
        return tag;
    }

    public static AttributeModifierMap deserialize(CompoundTag tag)
    {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        tag.getAllKeys().forEach(key ->
        {   Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(key));
            ListTag list = tag.getList(key, 10);
            list.forEach(mod -> map.put(attribute, AttributeModifier.load(((CompoundTag) mod))));
        });
        return new AttributeModifierMap(map);
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
