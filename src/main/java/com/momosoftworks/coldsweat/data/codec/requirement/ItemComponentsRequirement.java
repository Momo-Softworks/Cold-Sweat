package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public record ItemComponentsRequirement(Map<DataComponentType<?>, Object> components)
{
    public static final Codec<ItemComponentsRequirement> CODEC = CompoundTag.CODEC.xmap(ItemComponentsRequirement::deserialize, ItemComponentsRequirement::serialize);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemComponentsRequirement> STREAM_CODEC = StreamCodec.of(
            (buf, requirement) -> buf.writeNbt(requirement.serialize()),
            (buf) -> ItemComponentsRequirement.deserialize(buf.readNbt())
    );

    public ItemComponentsRequirement()
    {   this(new HashMap<>());
    }

    public boolean test(ItemStack pStack)
    {   return this.components().isEmpty() || this.test(pStack.getComponents());
    }

    public boolean test(@Nullable DataComponentMap components)
    {
        if (components == null)
        {   return this.components().isEmpty();
        }
        else return this.components().keySet().stream().allMatch(component ->
        {
            return CSMath.getIfNotNull(components.get(component),
                                       other -> this.compareComponents(this.components().get(component), other),
                                       false);
        });
    }

    /**
     * Compares a predicate (component) with a component (other)
     */
    private boolean compareComponents(Object predicate, Object component)
    {
        if (predicate == component) return true;
        if (predicate == null) return true;
        if (component == null) return false;
        if (predicate.equals(component)) return true;

        // Handle multiple possible values
        if (predicate instanceof CompoundTag compoundTag && !(component instanceof CompoundTag))
        {
            ListTag anyOfValues = (ListTag) compoundTag.get("cs:any_of");
            if (anyOfValues != null && !anyOfValues.isEmpty())
            {
                for (Tag value : anyOfValues)
                    if (compareComponents(value, component))
                    {   return true;
                    }
                return false;
            }
        }
        // Handle numerical range
        else if (component instanceof Number num)
        {
            String range = predicate instanceof String str ? str
                         : predicate instanceof StringTag stringTag ? stringTag.getAsString()
                         : null;
            if (range == null) return false;

            String[] parts = range.split("-");
            if (parts.length != 2) return false;

            double min = Double.parseDouble(parts[0]);
            double max = Double.parseDouble(parts[1]);

            return CSMath.betweenInclusive(num.doubleValue(), min, max);
        }
        return false;
    }

    public static ItemComponentsRequirement parse(String data)
    {
        RegistryAccess registryAccess = RegistryHelper.getRegistryAccess();
        if (registryAccess == null)
        {   return new ItemComponentsRequirement();
        }
        ItemParser parser = new ItemParser(registryAccess);
        Map<DataComponentType<?>, Object> parsedComponents = new HashMap<>();
        try
        {
            parser.parse(new StringReader(data), new ItemParser.Visitor()
            {
                @Override
                public <T> void visitComponent(DataComponentType<T> componentType, T value)
                {   parsedComponents.put(componentType, value);
                }

                @Override
                public <T> void visitRemovedComponent(DataComponentType<T> componentType)
                {   parsedComponents.remove(componentType);
                }
            });
        }
        catch (CommandSyntaxException e)
        {   e.printStackTrace();
        }
        return new ItemComponentsRequirement(parsedComponents);
    }

    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        for (DataComponentType componentType : this.components().keySet())
        {
            String key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType).toString();
            Tag component = serializeComponent(componentType, this.components().get(componentType));

            if (component != null)
            {   tag.put(key, component);
            }
        }
        return tag;
    }

    private Tag serializeComponent(DataComponentType componentType, Object component)
    {
        switch (component)
        {
            case String string ->
            {   return StringTag.valueOf(string);
            }
            case CompoundTag tag ->
            {   return tag;
            }
            default ->
            {
                TypedDataComponent<?> typed = new TypedDataComponent<>(componentType, component);
                return typed.encodeValue(NbtOps.INSTANCE).getOrThrow();
            }
        }
    }

    public static ItemComponentsRequirement deserialize(CompoundTag nbt)
    {
        Map<DataComponentType<?>, Object> components = new HashMap<>();
        for (String key : nbt.getAllKeys())
        {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(key);
            if (resourceLocation == null)
            {   continue;
            }
            DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(resourceLocation);
            if (componentType == null)
            {   continue;
            }
            Tag tag = nbt.get(key);
            Object value = deserializeComponent(componentType, tag);
            components.put(componentType, value);
        }
        return new ItemComponentsRequirement(components);
    }

    private static Object deserializeComponent(DataComponentType<?> componentType, Tag tag)
    {
        switch (tag)
        {
            case StringTag stringTag ->
            {   return stringTag.getAsString();
            }
            case CompoundTag compoundTag ->
            {   return compoundTag;
            }
            default ->
            {   return componentType.codec().decode(NbtOps.INSTANCE, tag).getOrThrow();
            }
        }
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

        ItemComponentsRequirement that = (ItemComponentsRequirement) obj;

        return components.equals(that.components);
    }

    @Override
    public String toString()
    {
        return "ItemComponents{" +
                "components=" + components +
                '}';
    }
}
