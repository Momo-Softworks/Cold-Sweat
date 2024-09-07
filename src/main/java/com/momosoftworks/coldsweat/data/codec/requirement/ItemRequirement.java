package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
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
    public final Optional<List<Either<ITag<Item>, Item>>> items;
    public final Optional<ITag<Item>> tag;
    public final Optional<IntegerBounds> count;
    public final Optional<IntegerBounds> durability;
    public final Optional<List<EnchantmentRequirement>> enchantments;
    public final Optional<List<EnchantmentRequirement>> storedEnchantments;
    public final Optional<Potion> potion;
    public final NbtRequirement nbt;

    public ItemRequirement(Optional<List<Either<ITag<Item>, Item>>> items, Optional<ITag<Item>> tag,
                           Optional<IntegerBounds> count, Optional<IntegerBounds> durability,
                           Optional<List<EnchantmentRequirement>> enchantments, Optional<List<EnchantmentRequirement>> storedEnchantments,
                           Optional<Potion> potion, NbtRequirement nbt)
    {
        this.items = items;
        this.tag = tag;
        this.count = count;
        this.durability = durability;
        this.enchantments = enchantments;
        this.storedEnchantments = storedEnchantments;
        this.potion = potion;
        this.nbt = nbt;
    }

    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrBuiltinCodec(Registry.ITEM_REGISTRY, Registry.ITEM).listOf().optionalFieldOf("items").forGetter(predicate -> predicate.items),
            ITag.codec(ItemTags::getAllTags).optionalFieldOf("tag").forGetter(predicate -> predicate.tag),
            IntegerBounds.CODEC.optionalFieldOf("count").forGetter(predicate -> predicate.count),
            IntegerBounds.CODEC.optionalFieldOf("durability").forGetter(predicate -> predicate.durability),
            EnchantmentRequirement.CODEC.listOf().optionalFieldOf("enchantments").forGetter(predicate -> predicate.enchantments),
            EnchantmentRequirement.CODEC.listOf().optionalFieldOf("stored_enchantments").forGetter(predicate -> predicate.storedEnchantments),
            Registry.POTION.optionalFieldOf("potion").forGetter(predicate -> predicate.potion),
            NbtRequirement.CODEC.optionalFieldOf("nbt", new NbtRequirement(new CompoundNBT())).forGetter(predicate -> predicate.nbt)
    ).apply(instance, ItemRequirement::new));

    public boolean test(ItemStack stack, boolean ignoreCount)
    {
        if (stack.isEmpty() && items.isPresent() && !items.get().isEmpty())
        {   return false;
        }

        if (this.nbt.tag.isEmpty())
        {   return true;
        }
        if (items.isPresent())
        {
            for (int i = 0; i < items.get().size(); i++)
            {
                Either<ITag<Item>, Item> either = items.get().get(i);
                if (either.map(tag -> tag.contains(stack.getItem()), item -> stack.getItem() == item))
                {   break;
                }
                if (i == items.get().size() - 1)
                {   return false;
                }
            }
        }
        if (tag.isPresent() && !tag.get().contains(stack.getItem()))
        {   return false;
        }
        if (!ignoreCount && count.isPresent() && !count.get().test(stack.getCount()))
        {   return false;
        }
        else if (durability.isPresent() && !durability.get().test(stack.getMaxDamage() - stack.getDamageValue()))
        {   return false;
        }
        else if (potion.isPresent() && !potion.get().getEffects().equals(PotionUtils.getPotion(stack).getEffects()))
        {   return false;
        }
        else if (!nbt.test(stack.getTag()))
        {   return false;
        }
        else if (enchantments.isPresent())
        {
            Map<Enchantment, Integer> stackEnchantments = EnchantmentHelper.deserializeEnchantments(stack.getEnchantmentTags());
            for (EnchantmentRequirement enchantment : enchantments.get())
            {
                if (!enchantment.test(stackEnchantments))
                {   return false;
                }
            }
        }
        else if (storedEnchantments.isPresent())
        {
            Map<Enchantment, Integer> stackEnchantments = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack));
            for (EnchantmentRequirement enchantment : storedEnchantments.get())
            {   if (!enchantment.test(stackEnchantments))
                {   return false;
                }
            }
        }
        return true;
    }

    public CompoundNBT serialize()
    {
        CompoundNBT nbt = new CompoundNBT();
        items.ifPresent(itemList -> nbt.put("items", NBTHelper.listTagOf(itemList.stream().map(either -> StringNBT.valueOf(either.map(tag -> "#" + ItemTags.getAllTags().getId(tag),
                                                                                                                       item -> ForgeRegistries.ITEMS.getKey(item).toString())))
                                                                     .collect(Collectors.toList()))));
        count.ifPresent(count -> nbt.put("count", count.serialize()));
        durability.ifPresent(durability -> nbt.put("durability", durability.serialize()));
        enchantments.ifPresent(enchantments -> nbt.put("enchantments", NBTHelper.listTagOf(enchantments.stream().map(EnchantmentRequirement::serialize).collect(Collectors.toList()))));
        storedEnchantments.ifPresent(enchantments -> nbt.put("stored_enchantments", NBTHelper.listTagOf(enchantments.stream().map(EnchantmentRequirement::serialize).collect(Collectors.toList()))));
        potion.ifPresent(potion -> nbt.putString("potion", ForgeRegistries.POTION_TYPES.getKey(potion).toString()));
        if (!this.nbt.tag.isEmpty()) nbt.put("nbt", this.nbt.serialize());
        return nbt;
    }

    public static ItemRequirement deserialize(CompoundNBT nbt)
    {
        Optional<List<Either<ITag<Item>, Item>>> items = Optional.of(nbt.getList("items", 8)
                                                           .stream()
                                                           .map(tg ->
                                                           {
                                                                 String string = tg.getAsString();
                                                                 ResourceLocation location = new ResourceLocation(string.replace("#", ""));
                                                                 if (!string.contains("#"))
                                                                 {   return Either.<ITag<Item>, Item>right(ForgeRegistries.ITEMS.getValue(location));
                                                                 }

                                                                 return Either.<ITag<Item>, Item>left(ItemTags.getAllTags().getTag(location));
                                                           })
                                                           .collect(Collectors.toList()));

        Optional<ITag<Item>> tag = nbt.contains("tag") ? Optional.ofNullable(ItemTags.getAllTags().getTag(new ResourceLocation(nbt.getString("tag"))))
                                                         : Optional.empty();

        Optional<IntegerBounds> count = nbt.contains("count") ? Optional.of(IntegerBounds.deserialize(nbt.getCompound("count")))
                                                              : java.util.Optional.empty();

        Optional<IntegerBounds> durability = nbt.contains("durability") ? Optional.of(IntegerBounds.deserialize(nbt.getCompound("durability")))
                                                                        : java.util.Optional.empty();

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

        NbtRequirement nbtReq = nbt.contains("nbt") ? NbtRequirement.deserialize(nbt.getCompound("nbt"))
                                                      : new NbtRequirement(new CompoundNBT());

        return new ItemRequirement(items, tag, count, durability, enchantments, storedEnchantments, potion, nbtReq);
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
        StringBuilder builder = new StringBuilder();
        builder.append("ItemRequirement{");
        items.ifPresent(itemList -> itemList.forEach(either -> builder.append(either.map(tag -> "#" + ItemTags.getAllTags().getId(tag),
                                                                                         item -> ForgeRegistries.ITEMS.getKey(item)).toString())
                                                                      .append(", ")));
        count.ifPresent(bounds -> builder.append(bounds.toString()).append(", "));
        durability.ifPresent(bounds -> builder.append(bounds.toString()).append(", "));
        enchantments.ifPresent(enchantments -> builder.append("Enchantments: {").append(enchantments.stream().map(EnchantmentRequirement::toString).collect(Collectors.joining(", "))).append("}, "));
        storedEnchantments.ifPresent(enchantments -> builder.append("Stored Enchantments: {").append(enchantments.stream().map(EnchantmentRequirement::toString).collect(Collectors.joining(", "))).append("}, "));
        potion.ifPresent(potion -> builder.append("Potion: ").append(ForgeRegistries.POTION_TYPES.getKey(potion).toString()));
        builder.append("NBT: ").append(nbt.toString()).append(", ");
        builder.append("}");

        return builder.toString();
    }
}
