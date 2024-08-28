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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.connection.ConnectionType;

import javax.annotation.Nullable;

public record ItemComponentsRequirement(DataComponentMap components)
{
    public static final Codec<ItemComponentsRequirement> CODEC = CompoundTag.CODEC.xmap(ItemComponentsRequirement::deserialize, ItemComponentsRequirement::serialize);

    public ItemComponentsRequirement()
    {   this(DataComponentMap.builder().build());
    }

    public boolean test(ItemStack pStack)
    {   return this.components().isEmpty() || this.test(pStack.getComponents());
    }

    public boolean test(@Nullable DataComponentMap components)
    {
        if (components == null)
        {   return this.components().isEmpty();
        }
        else
        {   return this.components().keySet().stream().allMatch(component -> CSMath.getIfNotNull(components.get(component),
                                                                                                 other -> other.equals(this.components().get(component)),
                                                                                                 false));
        }
    }

    public DataComponentPatch getAsPatch()
    {   return new PatchedDataComponentMap(this.components()).asPatch();
    }

    public static ItemComponentsRequirement parse(String data)
    {
        RegistryAccess registryAccess = RegistryHelper.getRegistryAccess();
        if (registryAccess == null)
        {   return new ItemComponentsRequirement();
        }
        ItemParser parser = new ItemParser(registryAccess);
        PatchedDataComponentMap parsedComponents = new PatchedDataComponentMap(DataComponentMap.builder().build());
        try
        {
            parser.parse(new StringReader(data), new ItemParser.Visitor()
            {
                @Override
                public <T> void visitComponent(DataComponentType<T> componentType, T value)
                {   parsedComponents.set(componentType, value);
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

    public String write()
    {
        return DataComponentMap.CODEC.encodeStart(JsonOps.INSTANCE, this.components).getOrThrow().toString();
    }

    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        for (DataComponentType<?> componentType : this.components().keySet())
        {
            String key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType).toString();
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryHelper.getRegistryAccess(), ConnectionType.NEOFORGE);
            TypedDataComponent.STREAM_CODEC.encode(buf, this.components().getTyped(componentType));
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);

            tag.putByteArray(key.toString(), bytes);
        }
        return tag;
    }

    public static ItemComponentsRequirement deserialize(CompoundTag nbt)
    {
        PatchedDataComponentMap tag = new PatchedDataComponentMap(DataComponentMap.builder().build());
        for (String key : nbt.getAllKeys())
        {
            DataComponentType componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse(key));
            if (componentType != null)
            {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(nbt.getByteArray(key)), RegistryHelper.getRegistryAccess(), ConnectionType.NEOFORGE);
                TypedDataComponent<?> component = TypedDataComponent.STREAM_CODEC.decode(buf);
                tag.set(componentType, component);
            }
        }
        return new ItemComponentsRequirement(tag);
    }

    /**
     * It is assumed that the first tag is a predicate, and the second tag is the tag to compare.
     */
    public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag other, boolean compareListTag)
    {
        if (tag == other)
        {   return true;
        }
        else if (tag == null)
        {   return true;
        }
        else if (other == null)
        {   return false;
        }
        else if (tag instanceof CompoundTag)
        {
            CompoundTag compoundNbt1 = (CompoundTag) tag;
            CompoundTag compoundNbt2 = (CompoundTag) other;

            for (String s : compoundNbt1.getAllKeys())
            {
                Tag tag1 = compoundNbt1.get(s);
                Tag tag2 = compoundNbt2.get(s);
                if (!compareNbt(tag1, tag2, compareListTag))
                {   return false;
                }
            }

            return true;
        }
        else if (tag instanceof ListTag && compareListTag)
        {
            ListTag listtag = (ListTag) tag;
            ListTag listtag1 = (ListTag) other;
            if (listtag.isEmpty())
            {   return listtag1.isEmpty();
            }
            else
            {
                for (int i = 0; i < listtag.size(); ++i)
                {
                    Tag element = listtag.get(i);
                    boolean flag = false;

                    for (int j = 0; j < listtag1.size(); ++j)
                    {
                        if (compareNbt(element, listtag1.get(j), compareListTag))
                        {   flag = true;
                            break;
                        }
                    }

                    if (!flag)
                    {   return false;
                    }
                }

                return true;
            }
        }
        // Compare a number range (represented as a string "min-max") in the predicate tag to a number in the compared tag
        else if (tag instanceof StringTag string && other instanceof NumericTag otherNumber)
        {
            try
            {
                // Parse value ranges in to min and max values
                String[] ranges = string.getAsString().split("-");
                Double[] range = new Double[ranges.length];
                for (int i = 0; i < ranges.length; i++)
                {   range[i] = Double.parseDouble(ranges[i]);
                }
                if (range.length == 2)
                {   return CSMath.betweenInclusive(otherNumber.getAsDouble(), range[0], range[1]);
                }
            }
            catch (Exception e)
            {   return false;
            }
        }
        return tag.equals(other);
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

    /*@Override
    public String toString()
    {
        return "ItemComponents{" +
                "tag=" + components +
                '}';
    }*/
}
