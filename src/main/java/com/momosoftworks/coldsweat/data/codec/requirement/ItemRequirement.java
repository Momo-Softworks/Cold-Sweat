package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public record ItemRequirement(NegatableList<Either<TagKey<Item>, Item>> items,
                              IntegerBounds count, IntegerBounds durability,
                              NegatableList<EnchantmentRequirement> enchantments,
                              Optional<Potion> potion, NbtRequirement nbt, Optional<Predicate<ItemStack>> predicate)
{
    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrBuiltinCodec(Registries.ITEM, ForgeRegistries.ITEMS)).optionalFieldOf("items", new NegatableList<>()).forGetter(predicate -> predicate.items),
            IntegerBounds.CODEC.optionalFieldOf("count", IntegerBounds.NONE).forGetter(predicate -> predicate.count),
            IntegerBounds.CODEC.optionalFieldOf("durability", IntegerBounds.NONE).forGetter(predicate -> predicate.durability),
            NegatableList.listCodec(EnchantmentRequirement.CODEC).optionalFieldOf("enchantments", new NegatableList<>()).forGetter(predicate -> predicate.enchantments),
            ForgeRegistries.POTIONS.getCodec().optionalFieldOf("potion").forGetter(predicate -> predicate.potion),
            NbtRequirement.CODEC.optionalFieldOf("nbt", new NbtRequirement()).forGetter(predicate -> predicate.nbt)
    ).apply(instance, ItemRequirement::new));

    public static final ItemRequirement NONE = new ItemRequirement(new NegatableList<>(), IntegerBounds.NONE, IntegerBounds.NONE,
                                                                   new NegatableList<>(), Optional.empty(), new NbtRequirement());

    public ItemRequirement(NegatableList<Either<TagKey<Item>, Item>> items,
                           IntegerBounds count, IntegerBounds durability,
                           NegatableList<EnchantmentRequirement> enchantments,
                           Optional<Potion> potion, NbtRequirement nbt)
    {
        this(items, count, durability, enchantments, potion, nbt, Optional.empty());
    }

    public ItemRequirement(NegatableList<Either<TagKey<Item>, Item>> items, NbtRequirement nbt)
    {
        this(items, IntegerBounds.NONE, IntegerBounds.NONE, new NegatableList<>(), Optional.empty(), nbt);
    }

    public ItemRequirement(Collection<Item> items, @Nullable Predicate<ItemStack> predicate)
    {
        this(new NegatableList<>(items.stream().map(Either::<TagKey<Item>, Item>right).toList()), IntegerBounds.NONE, IntegerBounds.NONE,
             new NegatableList<>(), Optional.empty(), new NbtRequirement(), Optional.ofNullable(predicate));
    }

    public ItemRequirement(Predicate<ItemStack> predicate)
    {
        this(new NegatableList<>(), IntegerBounds.NONE, IntegerBounds.NONE, new NegatableList<>(), Optional.empty(), new NbtRequirement(), Optional.of(predicate));
    }

    public boolean test(ItemStack stack, boolean ignoreCount)
    {
        if (stack.isEmpty() && !items.isEmpty())
        {   return false;
        }

        if (!items.test(either -> either.map(stack::is, stack::is)))
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
