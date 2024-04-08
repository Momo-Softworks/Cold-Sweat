package com.momosoftworks.coldsweat.config.util;

import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents an item and its NBT data, as well as an optional entity predicate.
 */
public class ItemData
{
    @Nonnull
    private final Item item;
    @Nonnull
    private final CompoundTag nbt;
    @Nullable
    private final EntityRequirement predicate;

    public ItemData(Item item, CompoundTag nbt, EntityRequirement predicate)
    {   this.item = item;
        this.nbt = nbt;
        this.predicate = predicate;
    }

    public ItemData(Item item, CompoundTag nbt)
    {   this.item = item;
        this.nbt = nbt;
        this.predicate = null;
    }

    public Item getItem()
    {   return item;
    }

    public CompoundTag getTag()
    {   return nbt;
    }

    @Nullable
    public EntityRequirement getPredicate()
    {   return predicate;
    }

    public boolean testEntity(Entity entity)
    {   return predicate == null || predicate.test(entity);
    }

    public boolean isEmpty()
    {   return item == Items.AIR;
    }

    public CompoundTag save(CompoundTag tag)
    {   tag.putString("Id", ForgeRegistries.ITEMS.getKey(item).toString());
        if (!nbt.isEmpty())
        {   tag.put("Tag", nbt);
        }
        if (predicate != null)
        {   tag.put("Predicate", NBTHelper.writeEntityRequirement(predicate));
        }
        return tag;
    }

    public static ItemData load(CompoundTag tag)
    {   Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("Id")));
        CompoundTag nbt = tag.contains("Tag", 10)
                          ? tag.getCompound("Tag")
                          : new CompoundTag();
        EntityRequirement predicate = tag.contains("Predicate", 10)
                                      ? NBTHelper.readEntityPredicate(tag.getCompound("Predicate"))
                                      : null;
        return new ItemData(item, nbt, predicate);
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
            && (other.nbt.isEmpty() || other.getTag().getAllKeys().stream().allMatch(key -> Objects.equals(other.nbt.get(key), nbt.get(key))))
            && (other.predicate == null || predicate == null
                || EntityRequirement.ANY.equals(other.predicate)
                || predicate.equals(other.predicate));
    }

    @Override
    public int hashCode()
    {   return 31 * item.hashCode();
    }

    @Override
    public String toString()
    {   StringBuilder builder = new StringBuilder("ItemData{item=").append(item);
        if (!nbt.isEmpty())
        {   builder.append(", nbt=").append(nbt);
        }
        if (predicate != null)
        {   builder.append(", predicate=").append(predicate.serialize());
        }
        return builder.append('}').toString();
    }
}
