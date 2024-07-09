package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record ItemRequirement(List<Either<TagKey<Item>, Item>> items,
                              Optional<IntegerBounds> count, Optional<IntegerBounds> durability,
                              Optional<List<EnchantmentRequirement>> enchantments, Optional<List<EnchantmentRequirement>> storedEnchantments,
                              Optional<Potion> potion, ItemComponentsRequirement components)
{
    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(TagKey.codec(Registries.ITEM), BuiltInRegistries.ITEM.byNameCodec()).listOf().fieldOf("items").forGetter(ItemRequirement::items),
            IntegerBounds.CODEC.optionalFieldOf("count").forGetter(predicate -> predicate.count),
            IntegerBounds.CODEC.optionalFieldOf("durability").forGetter(predicate -> predicate.durability),
            EnchantmentRequirement.CODEC.listOf().optionalFieldOf("enchantments").forGetter(predicate -> predicate.enchantments),
            EnchantmentRequirement.CODEC.listOf().optionalFieldOf("stored_enchantments").forGetter(predicate -> predicate.storedEnchantments),
            BuiltInRegistries.POTION.byNameCodec().optionalFieldOf("potion").forGetter(predicate -> predicate.potion),
            ItemComponentsRequirement.CODEC.optionalFieldOf("nbt", new ItemComponentsRequirement()).forGetter(predicate -> predicate.components)
    ).apply(instance, ItemRequirement::new));

    public boolean test(ItemStack stack, boolean ignoreCount)
    {
        if (stack.isEmpty() && !items.isEmpty())
        {   return false;
        }

        if (this.components.components().isEmpty())
        {   return true;
        }
        for (int i = 0; i < items.size(); i++)
        {
            Either<TagKey<Item>, Item> either = items.get(i);
            if (either.left().isPresent() && stack.is(either.left().get())
            || either.right().isPresent() && stack.is(either.right().get()))
            {
                break;
            }
            if (i == items.size() - 1)
            {   return false;
            }
        }
        if (!ignoreCount && count.isPresent() && !count.get().test(stack.getCount()))
        {   return false;
        }
        else if (durability.isPresent() && !durability.get().test(stack.getMaxDamage() - stack.getDamageValue()))
        {   return false;
        }
        else if (potion.isPresent() && !potion.get().getEffects().equals(stack.getOrDefault(DataComponents.POTION_CONTENTS, new PotionContents(Potions.AWKWARD)).potion().get().value().getEffects()))
        {   return false;
        }
        else if (!components.test(stack.getComponents()))
        {   return false;
        }
        else if (enchantments.isPresent())
        {
            ItemEnchantments stackEnchantments = stack.get(DataComponents.ENCHANTMENTS);
            if (stackEnchantments == null)
            {   return false;
            }
            for (EnchantmentRequirement enchantment : enchantments.get())
            {
                if (!enchantment.test(stackEnchantments))
                {   return false;
                }
            }
        }
        else if (storedEnchantments.isPresent())
        {
            ItemEnchantments stackEnchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
            if (stackEnchantments == null)
            {   return false;
            }
            for (EnchantmentRequirement enchantment : storedEnchantments.get())
            {   if (!enchantment.test(stackEnchantments))
                {   return false;
                }
            }
        }
        return true;
    }

    public CompoundTag serialize()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.put("items", NBTHelper.listTagOf(items.stream().map(either -> StringTag.valueOf(either.map(tag -> "#" + tag.location(),
                                                                                                       item -> BuiltInRegistries.ITEM.getKey(item).toString())))
                                                           .collect(Collectors.toList())));
        count.ifPresent(count -> nbt.put("count", count.serialize()));
        durability.ifPresent(durability -> nbt.put("durability", durability.serialize()));
        enchantments.ifPresent(enchantments -> nbt.put("enchantments", NBTHelper.listTagOf(enchantments.stream().map(EnchantmentRequirement::serialize).collect(Collectors.toList()))));
        storedEnchantments.ifPresent(enchantments -> nbt.put("stored_enchantments", NBTHelper.listTagOf(enchantments.stream().map(EnchantmentRequirement::serialize).collect(Collectors.toList()))));
        potion.ifPresent(potion -> nbt.putString("potion", BuiltInRegistries.POTION.getKey(potion).toString()));
        if (!this.components.components().isEmpty()) nbt.put("nbt", this.components.serialize());
        return nbt;
    }

    public static ItemRequirement deserialize(CompoundTag nbt)
    {
        List<Either<TagKey<Item>, Item>> items = nbt.getList("items", 8)
                                                     .stream()
                                                     .map(tg ->
                                                     {
                                                           String string = tg.getAsString();
                                                           ResourceLocation location = ResourceLocation.parse(string.replace("#", ""));
                                                           if (!string.contains("#"))
                                                           {   return Either.<TagKey<Item>, Item>right(BuiltInRegistries.ITEM.get(location));
                                                           }

                                                           return Either.<TagKey<Item>, Item>left(TagKey.create(Registries.ITEM, location));
                                                     })
                                                     .toList();

        Optional<IntegerBounds> count = nbt.contains("count") ? Optional.of(IntegerBounds.deserialize(nbt.getCompound("count")))
                                                              : Optional.empty();

        Optional<IntegerBounds> durability = nbt.contains("durability") ? Optional.of(IntegerBounds.deserialize(nbt.getCompound("durability")))
                                                                        : Optional.empty();

        Optional<List<EnchantmentRequirement>> enchantments = nbt.contains("enchantments") ? Optional.of(nbt.getList("enchantments", 10)
                                                                                                         .stream()
                                                                                                         .map(tg -> EnchantmentRequirement.deserialize(((CompoundTag) tg)))
                                                                                                         .toList())
                                                                                           : Optional.empty();

        Optional<List<EnchantmentRequirement>> storedEnchantments = nbt.contains("stored_enchantments") ? Optional.of(nbt.getList("stored_enchantments", 10)
                                                                                                                      .stream()
                                                                                                                      .map(tg -> EnchantmentRequirement.deserialize(((CompoundTag) tg)))
                                                                                                                      .toList())
                                                                                                        : Optional.empty();

        Optional<Potion> potion = nbt.contains("potion") ? Optional.ofNullable(BuiltInRegistries.POTION.get(ResourceLocation.parse(nbt.getString("potion"))))
                                                         : Optional.empty();

        ItemComponentsRequirement nbtReq = nbt.contains("nbt") ? ItemComponentsRequirement.deserialize(nbt.getCompound("nbt"))
                                                               : new ItemComponentsRequirement();

        return new ItemRequirement(items, count, durability, enchantments, storedEnchantments, potion, nbtReq);
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
        return components.equals(that.components);
    }

    /*@Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ItemRequirement{");
        items.forEach(either -> builder.append(either.map(tag -> "#" + tag.location(),
                                                          item -> BuiltInRegistries.ITEM.getKey(item)).toString()).append(", "));
        count.ifPresent(bounds -> builder.append(bounds.toString()).append(", "));
        durability.ifPresent(bounds -> builder.append(bounds.toString()).append(", "));
        enchantments.ifPresent(enchantments -> builder.append("Enchantments: {").append(enchantments.stream().map(EnchantmentRequirement::toString).collect(Collectors.joining(", "))).append("}, "));
        storedEnchantments.ifPresent(enchantments -> builder.append("Stored Enchantments: {").append(enchantments.stream().map(EnchantmentRequirement::toString).collect(Collectors.joining(", "))).append("}, "));
        potion.ifPresent(potion -> builder.append("Potion: ").append(BuiltInRegistries.POTION.getKey(potion).toString()));
        builder.append("NBT: ").append(components.toString()).append(", ");
        builder.append("}");

        return builder.toString();
    }*/
}
