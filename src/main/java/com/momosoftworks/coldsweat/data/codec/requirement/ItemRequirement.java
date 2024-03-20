package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.potion.Potion;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemRequirement
{
    private final Optional<ITag<Item>> tag;
    private final Optional<List<Item>> items;
    private final Optional<IntegerBounds> count;
    private final Optional<IntegerBounds> durability;
    private final Optional<List<EnchantmentRequirement>> enchantments;
    private final Optional<List<EnchantmentRequirement>> storedEnchantments;
    private final Optional<Potion> potion;
    private final Optional<NbtRequirement> nbt;
    
    public ItemRequirement(Optional<ITag<Item>> tag, Optional<List<Item>> items,
                           Optional<IntegerBounds> count, Optional<IntegerBounds> durability,
                           Optional<List<EnchantmentRequirement>> enchantments, Optional<List<EnchantmentRequirement>> storedEnchantments,
                           Optional<Potion> potion, Optional<NbtRequirement> nbt)
    {
        this.tag = tag;
        this.items = items;
        this.count = count;
        this.durability = durability;
        this.enchantments = enchantments;
        this.storedEnchantments = storedEnchantments;
        this.potion = potion;
        this.nbt = nbt;
    }
    
    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ITag.codec(ItemTags::getAllTags).optionalFieldOf("tag").forGetter(predicate -> predicate.tag),
            Registry.ITEM.listOf().optionalFieldOf("items").forGetter(predicate -> predicate.items),
            IntegerBounds.CODEC.optionalFieldOf("count").forGetter(predicate -> predicate.count),
            IntegerBounds.CODEC.optionalFieldOf("durability").forGetter(predicate -> predicate.durability),
            EnchantmentRequirement.CODEC.listOf().optionalFieldOf("enchantments").forGetter(predicate -> predicate.enchantments),
            EnchantmentRequirement.CODEC.listOf().optionalFieldOf("stored_enchantments").forGetter(predicate -> predicate.storedEnchantments),
            Registry.POTION.optionalFieldOf("potion").forGetter(predicate -> predicate.potion),
            NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(predicate -> predicate.nbt)
    ).apply(instance, ItemRequirement::new));

    public boolean test(ItemStack stack)
    {
        if (tag.isPresent() && !tag.get().contains(stack.getItem()))
        {   return false;
        }
        else if (items.isPresent() && !items.get().contains(stack.getItem()))
        {   return false;
        }
        else if (count.isPresent() && !count.get().test(stack.getCount()))
        {   return false;
        }
        else if (durability.isPresent() && !durability.get().test(stack.getMaxDamage() - stack.getDamageValue()))
        {   return false;
        }
        else if (potion.isPresent() && !potion.get().equals(stack.getItem()))
        {   return false;
        }
        else if (nbt.isPresent() && !nbt.get().test(stack.getTag()))
        {   return false;
        }
        else if (enchantments.isPresent())
        {
            for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet())
            {
                for (EnchantmentRequirement enchantment : enchantments.get())
                {   if (enchantment.test(entry.getKey(), entry.getValue()))
                    {   return true;
                    }
                }
            }
        }
        else if (storedEnchantments.isPresent())
        {
            for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet())
            {
                for (EnchantmentRequirement enchantment : storedEnchantments.get())
                {   if (enchantment.test(entry.getKey(), entry.getValue()))
                    {   return true;
                    }
                }
            }
        }
        return true;
    }

    public CompoundNBT serialize()
    {
        CompoundNBT nbt = new CompoundNBT();
        tag.ifPresent(tag -> nbt.putString("tag", ItemTags.getAllTags().getId(tag).toString()));
        items.ifPresent(items -> nbt.put("items", NBTHelper.listTagOf(items.stream().map(item -> StringNBT.valueOf(ForgeRegistries.ITEMS.getKey(item).toString())).collect(Collectors.toList()))));
        count.ifPresent(count -> nbt.put("count", count.serialize()));
        durability.ifPresent(durability -> nbt.put("durability", durability.serialize()));
        enchantments.ifPresent(enchantments -> nbt.put("enchantments", NBTHelper.listTagOf(enchantments.stream().map(EnchantmentRequirement::serialize).collect(Collectors.toList()))));
        storedEnchantments.ifPresent(enchantments -> nbt.put("stored_enchantments", NBTHelper.listTagOf(enchantments.stream().map(EnchantmentRequirement::serialize).collect(Collectors.toList()))));
        potion.ifPresent(potion -> nbt.putString("potion", ForgeRegistries.POTION_TYPES.getKey(potion).toString()));
        this.nbt.ifPresent(requirement -> nbt.put("nbt", requirement.serialize()));
        return nbt;
    }

    public static ItemRequirement deserialize(CompoundNBT nbt)
    {
        Optional<ITag<Item>> tag = nbt.contains("tag") ? Optional.of(ItemTags.getAllTags().getTag(new ResourceLocation(nbt.getString("tag"))))
                                                         : Optional.empty();

        Optional<List<Item>> items = nbt.contains("items") ? Optional.of(nbt.getList("items", 10)
                                                                         .stream()
                                                                         .map(tg -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(tg.getAsString())))
                                                                         .collect(Collectors.toList()))
                                                           : Optional.empty();

        Optional<IntegerBounds> count = nbt.contains("count") ? Optional.of(IntegerBounds.deserialize(nbt.getCompound("count")))
                                                              : Optional.empty();

        Optional<IntegerBounds> durability = nbt.contains("durability") ? Optional.of(IntegerBounds.deserialize(nbt.getCompound("durability")))
                                                                        : Optional.empty();

        Optional<List<EnchantmentRequirement>> enchantments = nbt.contains("enchantments") ? Optional.of(nbt.getList("enchantments", 10)
                                                                                                         .stream()
                                                                                                         .map(tg -> EnchantmentRequirement.deserialize(((CompoundNBT) tg)))
                                                                                                         .collect(Collectors.toList()))
                                                                                           : Optional.empty();

        Optional<List<EnchantmentRequirement>> storedEnchantments = nbt.contains("stored_enchantments") ? Optional.of(nbt.getList("stored_enchantments", 10)
                                                                                                                      .stream()
                                                                                                                      .map(tg -> EnchantmentRequirement.deserialize(((CompoundNBT) tg)))
                                                                                                                      .collect(Collectors.toList()))
                                                                                                        : Optional.empty();

        Optional<Potion> potion = nbt.contains("potion") ? Optional.ofNullable(ForgeRegistries.POTION_TYPES.getValue(new ResourceLocation(nbt.getString("potion"))))
                                                         : Optional.empty();

        Optional<NbtRequirement> compound = nbt.contains("nbt") ? Optional.of(NbtRequirement.deserialize(nbt.getCompound("nbt")))
                                                                : Optional.empty();

        return new ItemRequirement(tag, items, count, durability, enchantments, storedEnchantments, potion, compound);
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

        ItemRequirement that = (ItemRequirement) obj;

        if (!tag.equals(that.tag))
        {   return false;
        }
        if (!items.equals(that.items))
        {   return false;
        }
        if (!count.equals(that.count))
        {   return false;
        }
        if (!durability.equals(that.durability))
        {   return false;
        }
        if (!enchantments.equals(that.enchantments))
        {   return false;
        }
        if (!storedEnchantments.equals(that.storedEnchantments))
        {   return false;
        }
        if (!potion.equals(that.potion))
        {   return false;
        }
        return nbt.equals(that.nbt);
    }

    @Override
    public String toString()
    {
        return "Item{" +
                "tag=" + tag +
                ", items=" + items +
                ", count=" + count +
                ", durability=" + durability +
                ", enchantments=" + enchantments +
                ", storedEnchantments=" + storedEnchantments +
                ", potion=" + potion +
                ", nbt=" + nbt +
                '}';
    }
}
