package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.advancements.criterion.NBTPredicate.getEntityTagToCompare;

public class NbtRequirement
{
    private final CompoundNBT tag;

    public NbtRequirement(CompoundNBT tag)
    {   this.tag = tag;
    }

    public static final Codec<NbtRequirement> CODEC = CompoundNBT.CODEC.xmap(NbtRequirement::new, req -> req.tag);

    public static final NbtRequirement NONE = new NbtRequirement();

    public NbtRequirement()
    {   this(new CompoundNBT());
    }

    public CompoundNBT tag()
    {   return tag;
    }

    public boolean test(ItemStack stack)
    {   return this.tag.isEmpty() || this.test(stack.getTag());
    }

    public boolean test(Entity entity)
    {   return this.tag.isEmpty() || this.test(getEntityTagToCompare(entity));
    }

    public boolean test(@Nullable CompoundNBT nbt)
    {
        if (nbt == null)
        {   return this.tag.isEmpty();
        }
        else
        {   return compareNbt(this.tag, nbt);
        }
    }

    public boolean isEmpty()
    {   return this.tag.isEmpty();
    }

    /**
     * It is assumed that the first tag is a predicate, and the second tag is the tag to compare.
     */
    public static boolean compareNbt(@Nullable INBT tag, @Nullable INBT other)
    {
        if (tag == other) return true;
        if (tag == null) return true;
        if (other == null) return false;
        if (tag.equals(other)) return true;

        // Handle CompoundTag comparison
        if (tag instanceof CompoundNBT)
        {   return handleCompoundTagComparison((CompoundNBT) tag, other);
        }

        // Handle ListTag comparison
        if (tag instanceof ListNBT && other instanceof ListNBT)
        {   return compareListTags((ListNBT) tag, (ListNBT) other);
        }

        // Handle numeric range comparison
        if (tag instanceof StringNBT && other instanceof NumberNBT)
        {   return compareNumericRange((StringNBT) tag, (NumberNBT) other);
        }

        // Handle numeric comparison
        if (tag instanceof NumberNBT && other instanceof NumberNBT)
        {   return compareNumbers((NumberNBT) tag, (NumberNBT) other);
        }

        return false;
    }

    private static boolean handleCompoundTagComparison(CompoundNBT compoundTag, INBT other)
    {
        // Case 1: Compare with another CompoundNBT
        if (other instanceof CompoundNBT)
        {
            CompoundNBT otherCompound = (CompoundNBT) other;
            for (String key : compoundTag.getAllKeys())
            {
                if (!compareNbt(compoundTag.get(key), otherCompound.get(key)))
                {   return false;
                }
            }
            return true;
        }

        // Case 2: Special comparison with cs:contains or cs:any_of
        if (compoundTag.getAllKeys().size() != 1)
            return false;

        ListNBT anyOfValues = (ListNBT) compoundTag.get("cs:any_of");
        if (anyOfValues != null && !anyOfValues.isEmpty())
        {
            for (int i = 0; i < anyOfValues.size(); i++)
            {
                INBT value = anyOfValues.get(i);
                if (compareNbt(value, other))
                {   return true;
                }
            }
            return false;
        }

        ListNBT containsAnyValues = (ListNBT) compoundTag.get("cs:contains_any");
        if (containsAnyValues != null && !containsAnyValues.isEmpty() && other instanceof ListNBT)
        {
            ListNBT otherList = (ListNBT) other;
            for (int i = 0; i < containsAnyValues.size(); i++)
            {
                INBT value = containsAnyValues.get(i);
                for (int i1 = 0; i1 < otherList.size(); i1++)
                {
                    INBT otherValue = otherList.get(i1);
                    if (compareNbt(value, otherValue))
                    {   return true;
                    }
                }
            }
        }

        ListNBT containsAllValues = (ListNBT) compoundTag.get("cs:contains_all");
        if (containsAllValues != null && !containsAllValues.isEmpty() && other instanceof ListNBT)
        {
            ListNBT otherList = (ListNBT) other;
            for (int i = 0; i < containsAllValues.size(); i++)
            {
                INBT value = containsAllValues.get(i);
                find:
                {
                    for (int i1 = 0; i1 < otherList.size(); i1++)
                    {
                        INBT otherValue = otherList.get(i1);
                        if (compareNbt(value, otherValue))
                        {   break find;
                        }
                    }
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private static boolean compareListTags(ListNBT list1, ListNBT list2)
    {
        if (list1.size() != list2.size()) return false;

        List<INBT> sortedList1 = new ArrayList<>(list1);
        List<INBT> sortedList2 = new ArrayList<>(list2);
        sortedList1.sort(Comparator.comparing(INBT::toString));
        sortedList2.sort(Comparator.comparing(INBT::toString));

        for (int i = 0; i < sortedList1.size(); i++)
        {
            if (!compareNbt(sortedList1.get(i), sortedList2.get(i)))
            {   return false;
            }
        }
        return true;
    }

    private static boolean compareNumericRange(StringNBT rangeTag, NumberNBT numberTag)
    {
        try
        {
            String numberString = rangeTag.getAsString();
            String[] parts = numberString.split(":");
            int readIndex = 0;
            if (parts.length == 0 || parts.length > 2) return false;

            double value = numberTag.getAsDouble();
            double min = numberString.startsWith(":") ? -Double.MAX_VALUE : Double.parseDouble(parts[readIndex++]);
            double max = numberString.endsWith(":") ? Double.MAX_VALUE : Double.parseDouble(parts[readIndex]);

            if (min == -Double.MAX_VALUE) return value <= max;
            if (max == Double.MAX_VALUE) return value >= min;

            return CSMath.betweenInclusive(value, min, max);
        }
        catch (Exception e)
        {   return false;
        }
    }

    private static boolean compareNumbers(NumberNBT tag, NumberNBT other)
    {   return tag.getAsDouble() == other.getAsDouble();
    }

    @Override
    public String toString()
    {
        return "NbtRequirement{" +
                "tag=" + tag +
                '}';
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

        NbtRequirement that = (NbtRequirement) obj;
        return tag.equals(that.tag);
    }
}
