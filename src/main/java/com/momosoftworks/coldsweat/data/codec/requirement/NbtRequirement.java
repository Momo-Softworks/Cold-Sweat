package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

import java.util.*;

import static net.minecraft.advancements.critereon.NbtPredicate.getEntityTagToCompare;

public record NbtRequirement(CompoundTag tag)
{
    public static final Codec<NbtRequirement> CODEC = CompoundTag.CODEC.xmap(NbtRequirement::new, NbtRequirement::tag);

    public static final NbtRequirement NONE = new NbtRequirement();

    public NbtRequirement()
    {   this(new CompoundTag());
    }

    public boolean test(Entity entity)
    {   return this.tag().isEmpty() || this.test(getEntityTagToCompare(entity));
    }

    public boolean test(@Nullable CompoundTag nbt)
    {
        if (nbt == null)
        {   return this.tag().isEmpty();
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
    public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag other)
    {
        if (tag == other) return true;
        if (tag == null) return true;
        if (other == null) return false;
        if (tag.equals(other)) return true;

        // Handle CompoundTag comparison
        if (tag instanceof CompoundTag compoundTag)
        {   return handleCompoundTagComparison(compoundTag, other);
        }

        // Handle ListTag comparison
        if (tag instanceof ListTag && other instanceof ListTag)
        {   return compareListTags((ListTag) tag, (ListTag) other);
        }

        // Handle numeric range comparison
        if (tag instanceof StringTag string && other instanceof NumericTag numericTag)
        {   return compareNumericRange(string, numericTag);
        }

        // Handle numeric comparison
        if (tag instanceof NumericTag numericTag && other instanceof NumericTag otherNumeric)
        {   return compareNumbers(numericTag, otherNumeric);
        }

        return false;
    }

    private static boolean handleCompoundTagComparison(CompoundTag compoundTag, Tag other)
    {
        // Case 1: Compare with another CompoundTag
        if (other instanceof CompoundTag otherCompound)
        {
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

        ListTag anyOfValues = (ListTag) compoundTag.get("cs:any_of");
        if (anyOfValues != null && !anyOfValues.isEmpty())
        {
            for (int i = 0; i < anyOfValues.size(); i++)
            {
                Tag value = anyOfValues.get(i);
                if (compareNbt(value, other))
                {   return true;
                }
            }
            return false;
        }

        ListTag containsAnyValues = (ListTag) compoundTag.get("cs:contains_any");
        if (containsAnyValues != null && !containsAnyValues.isEmpty() && other instanceof ListTag otherList)
        {
            for (int i = 0; i < containsAnyValues.size(); i++)
            {
                Tag value = containsAnyValues.get(i);
                for (int i1 = 0; i1 < otherList.size(); i1++)
                {
                    Tag otherValue = otherList.get(i1);
                    if (compareNbt(value, otherValue))
                    {   return true;
                    }
                }
            }
        }

        ListTag containsAllValues = (ListTag) compoundTag.get("cs:contains_all");
        if (containsAllValues != null && !containsAllValues.isEmpty() && other instanceof ListTag otherList)
        {
            for (int i = 0; i < containsAllValues.size(); i++)
            {
                Tag value = containsAllValues.get(i);
                find:
                {
                    for (int i1 = 0; i1 < otherList.size(); i1++)
                    {
                        Tag otherValue = otherList.get(i1);
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

    private static boolean compareListTags(ListTag list1, ListTag list2)
    {
        if (list1.size() != list2.size()) return false;

        List<Tag> sortedList1 = new ArrayList<>(list1);
        List<Tag> sortedList2 = new ArrayList<>(list2);
        sortedList1.sort(Comparator.comparing(Tag::toString));
        sortedList2.sort(Comparator.comparing(Tag::toString));

        for (int i = 0; i < sortedList1.size(); i++)
        {
            if (!compareNbt(sortedList1.get(i), sortedList2.get(i)))
            {   return false;
            }
        }
        return true;
    }

    private static boolean compareNumericRange(StringTag rangeTag, NumericTag numberTag)
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

    private static boolean compareNumbers(NumericTag tag, NumericTag other)
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
