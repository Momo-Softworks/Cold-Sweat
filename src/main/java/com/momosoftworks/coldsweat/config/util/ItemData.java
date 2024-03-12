package com.momosoftworks.coldsweat.config.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
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
    private final CompoundNBT nbt;

    public ItemData(Item item, CompoundNBT nbt)
    {   this.item = item;
        this.nbt = nbt;
    }

    public Item getItem()
    {   return item;
    }

    public CompoundNBT getTag()
    {   return nbt;
    }

    public CompoundNBT getOrCreateTag()
    {   return getTag();
    }

    public boolean isEmpty()
    {   return item == Items.AIR;
    }

    public CompoundNBT save(CompoundNBT tag)
    {   tag.putString("Id", ForgeRegistries.ITEMS.getKey(item).toString());
        if (!nbt.isEmpty())
        {   tag.put("Tag", nbt);
        }
        return tag;
    }

    public static ItemData load(CompoundNBT tag)
    {   Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("Id")));
        CompoundNBT nbt = tag.contains("Tag") ? tag.getCompound("Tag") : new CompoundNBT();
        return new ItemData(item, nbt);
    }

    public static ItemData of(ItemStack stack)
    {   return new ItemData(stack.getItem(), stack.getTag() != null ? stack.getTag().copy() : new CompoundNBT());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o instanceof ItemData)
        {
            ItemData other = ((ItemData) o);
            return item == other.item
                && (other.nbt.isEmpty() || other.getOrCreateTag().getAllKeys().stream().allMatch(key -> Objects.equals(other.nbt.get(key), nbt.get(key))));
        }
        return false;
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
