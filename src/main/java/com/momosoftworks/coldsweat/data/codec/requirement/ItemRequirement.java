package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tags.ITag;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ItemRequirement
{
    private final NegatableList<Either<ITag<Item>, Item>> items;
    private final IntegerBounds count;
    private final IntegerBounds durability;
    private final NegatableList<EnchantmentRequirement> enchantments;
    private final Optional<Potion> potion;
    private final NbtRequirement nbt;
    private final Optional<Predicate<ItemStack>> predicate;

    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrBuiltinCodec(Registry.ITEM_REGISTRY, Registry.ITEM)).optionalFieldOf("items", new NegatableList<>()).forGetter(predicate -> predicate.items),
            IntegerBounds.CODEC.optionalFieldOf("count", IntegerBounds.NONE).forGetter(predicate -> predicate.count),
            IntegerBounds.CODEC.optionalFieldOf("durability", IntegerBounds.NONE).forGetter(predicate -> predicate.durability),
            NegatableList.listCodec(EnchantmentRequirement.CODEC).optionalFieldOf("enchantments", new NegatableList<>()).forGetter(predicate -> predicate.enchantments),
            Registry.POTION.optionalFieldOf("potion").forGetter(predicate -> predicate.potion),
            NbtRequirement.CODEC.optionalFieldOf("nbt", new NbtRequirement()).forGetter(predicate -> predicate.nbt)
    ).apply(instance, ItemRequirement::new));

    public static final ItemRequirement NONE = new ItemRequirement(new NegatableList<>(), IntegerBounds.NONE, IntegerBounds.NONE,
                                                                   new NegatableList<>(), Optional.empty(), new NbtRequirement());

    public ItemRequirement(NegatableList<Either<ITag<Item>, Item>> items,
                           IntegerBounds count, IntegerBounds durability,
                           NegatableList<EnchantmentRequirement> enchantments,
                           Optional<Potion> potion, NbtRequirement nbt, Optional<Predicate<ItemStack>> predicate)
    {
        this.items = items;
        this.count = count;
        this.durability = durability;
        this.enchantments = enchantments;
        this.potion = potion;
        this.nbt = nbt;
        this.predicate = predicate;
    }

    public ItemRequirement(NegatableList<Either<ITag<Item>, Item>> items,
                           IntegerBounds count, IntegerBounds durability,
                           NegatableList<EnchantmentRequirement> enchantments,
                           Optional<Potion> potion, NbtRequirement nbt)
    {
        this(items, count, durability, enchantments, potion, nbt, Optional.empty());
    }

    public ItemRequirement(NegatableList<Either<ITag<Item>, Item>> items, NbtRequirement nbt)
    {
        this(items, IntegerBounds.NONE, IntegerBounds.NONE, new NegatableList<>(), Optional.empty(), nbt);
    }

    public ItemRequirement(Collection<Item> items, Predicate<ItemStack> predicate)
    {
        this(new NegatableList<>(items.stream().map(Either::<ITag<Item>, Item>right).collect(Collectors.toList())), IntegerBounds.NONE, IntegerBounds.NONE,
             new NegatableList<>(), Optional.empty(), new NbtRequirement(), Optional.ofNullable(predicate));
    }

    public ItemRequirement(Predicate<ItemStack> predicate)
    {
        this(new NegatableList<>(), IntegerBounds.NONE, IntegerBounds.NONE, new NegatableList<>(), Optional.empty(), new NbtRequirement(), Optional.of(predicate));
    }

    public NegatableList<Either<ITag<Item>, Item>> items()
    {   return items;
    }
    public IntegerBounds count()
    {   return count;
    }
    public IntegerBounds durability()
    {   return durability;
    }
    public NegatableList<EnchantmentRequirement> enchantments()
    {   return enchantments;
    }
    public Optional<Potion> potion()
    {   return potion;
    }
    public NbtRequirement nbt()
    {   return nbt;
    }
    public Optional<Predicate<ItemStack>> predicate()
    {   return predicate;
    }

    public boolean test(ItemStack stack, boolean ignoreCount)
    {
        if (stack.isEmpty() && !items.isEmpty())
        {   return false;
        }

        if (!items.test(either -> either.map(tag -> tag.contains(stack.getItem()), item -> stack.getItem() == item)))
        {   return false;
        }
        if (this.predicate.isPresent())
        {   return this.predicate.get().test(stack);
        }
        if (!this.nbt.test(stack.getTag()))
        {   return false;
        }
        if (!ignoreCount && !count.test(stack.getCount()))
        {   return false;
        }
        else if (!durability.test(stack.getMaxDamage() - stack.getDamageValue()))
        {   return false;
        }
        else if (potion.isPresent() && !potion.get().getEffects().equals(PotionUtils.getPotion(stack).getEffects()))
        {   return false;
        }
        else if (!nbt.test(stack.getTag()))
        {   return false;
        }
        else if (!enchantments.isEmpty())
        {
            Map<Enchantment, Integer> stackEnchantments = EnchantmentHelper.deserializeEnchantments(stack.getEnchantmentTags());
            stackEnchantments.putAll(EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack)));
            if (!enchantments.test(enchantment -> enchantment.test(stackEnchantments)))
            {   return false;
            }
        }
        return true;
    }

    @Override
    public String toString()
    {   return CODEC.encodeStart(JsonOps.INSTANCE, this).result().map(Object::toString).orElse("serialize_failed");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ItemRequirement that = (ItemRequirement) obj;
        return items.equals(that.items)
            && count.equals(that.count)
            && durability.equals(that.durability)
            && enchantments.equals(that.enchantments)
            && potion.equals(that.potion)
            && nbt.equals(that.nbt)
            && predicate.equals(that.predicate);
    }
}
