package com.momosoftworks.coldsweat.config.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * An item stack with no count, and has a more lenient equals method.
 */
public class ItemData
{
    @Nonnull
    private final Item item;
    @Nonnull
    private final CompoundTag nbt;

    public ItemData(Item item, CompoundTag nbt)
    {   this.item = item;
        this.nbt = nbt;
    }

    public Item getItem()
    {   return item;
    }

    public CompoundTag getTag()
    {   return nbt;
    }

    public CompoundTag getOrCreateTag()
    {   return getTag();
    }

    public boolean isEmpty()
    {   return item == Items.AIR;
    }

    public CompoundTag save(CompoundTag tag)
    {   tag.putString("Id", ForgeRegistries.ITEMS.getKey(item).toString());
        if (!nbt.isEmpty())
        {   tag.put("Tag", nbt);
        }
        return tag;
    }

    public static ItemData load(CompoundTag tag)
    {   Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("Id")));
        CompoundTag nbt = tag.contains("Tag") ? tag.getCompound("Tag") : new CompoundTag();
        return new ItemData(item, nbt);
    }

    public static ItemData of(ItemStack stack)
    {   return new ItemData(stack.getItem(), stack.getTag() != null ? stack.getTag().copy() : new CompoundTag());
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o
            || o instanceof ItemData other
            && item == other.item
            && (other.nbt.isEmpty() || other.getOrCreateTag().getAllKeys().stream().allMatch(key -> Objects.equals(other.nbt.get(key), nbt.get(key))));
    }

    @Override
    public int hashCode()
    {   return 31 * item.hashCode();
    }

    @Override
    public String toString()
    {   return "ItemData{" + "item=" + item + ", nbt=" + nbt + '}';
    }
}
