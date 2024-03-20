package com.momosoftworks.coldsweat.data.codec.requirement;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record ItemRequirement(Optional<TagKey<Item>> tag, Optional<List<Item>> items,
                              Optional<IntegerBounds> count, Optional<IntegerBounds> durability,
                              Optional<List<EnchantmentRequirement>> enchantments, Optional<List<EnchantmentRequirement>> storedEnchantments,
                              Optional<Potion> potion, Optional<NbtRequirement> nbt)
{
    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registry.ITEM_REGISTRY).optionalFieldOf("tag").forGetter(predicate -> predicate.tag),
            ForgeRegistries.ITEMS.getCodec().listOf().optionalFieldOf("items").forGetter(predicate -> predicate.items),
            IntegerBounds.CODEC.optionalFieldOf("count").forGetter(predicate -> predicate.count),
            IntegerBounds.CODEC.optionalFieldOf("durability").forGetter(predicate -> predicate.durability),
            EnchantmentRequirement.CODEC.listOf().optionalFieldOf("enchantments").forGetter(predicate -> predicate.enchantments),
            EnchantmentRequirement.CODEC.listOf().optionalFieldOf("stored_enchantments").forGetter(predicate -> predicate.storedEnchantments),
            ForgeRegistries.POTIONS.getCodec().optionalFieldOf("potion").forGetter(predicate -> predicate.potion),
            NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(predicate -> predicate.nbt)
    ).apply(instance, ItemRequirement::new));

    public boolean test(ItemStack stack)
    {
        if (tag.isPresent() && !stack.is(tag.get()))
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
        else if (potion.isPresent() && !potion.get().getEffects().equals(PotionUtils.getPotion(stack).getEffects()))
        {   return false;
        }
        else if (nbt.isPresent() && !nbt.get().test(stack.getTag()))
        {   return false;
        }
        else if (enchantments.isPresent())
        {
            Map<Enchantment, Integer> stackEnchantments = EnchantmentHelper.deserializeEnchantments(stack.getEnchantmentTags());
            for (EnchantmentRequirement enchantment : enchantments.get())
            {   if (!enchantment.test(stackEnchantments))
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

    public CompoundTag serialize()
    {
        CompoundTag nbt = new CompoundTag();
        tag.ifPresent(tag -> nbt.putString("tag", tag.location().toString()));
        items.ifPresent(items -> nbt.put("items", NBTHelper.listTagOf(items.stream().map(item -> StringTag.valueOf(ForgeRegistries.ITEMS.getKey(item).toString())).collect(Collectors.toList()))));
        count.ifPresent(count -> nbt.put("count", count.serialize()));
        durability.ifPresent(durability -> nbt.put("durability", durability.serialize()));
        enchantments.ifPresent(enchantments -> nbt.put("enchantments", NBTHelper.listTagOf(enchantments.stream().map(EnchantmentRequirement::serialize).collect(Collectors.toList()))));
        storedEnchantments.ifPresent(enchantments -> nbt.put("stored_enchantments", NBTHelper.listTagOf(enchantments.stream().map(EnchantmentRequirement::serialize).collect(Collectors.toList()))));
        potion.ifPresent(potion -> nbt.putString("potion", ForgeRegistries.POTIONS.getKey(potion).toString()));
        this.nbt.ifPresent(requirement -> nbt.put("nbt", requirement.serialize()));
        return nbt;
    }

    public static ItemRequirement deserialize(CompoundTag nbt)
    {
        Optional<TagKey<Item>> tag = nbt.contains("tag") ? Optional.of(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(nbt.getString("tag"))))
                                                         : Optional.empty();

        Optional<List<Item>> items = nbt.contains("items") ? Optional.of(nbt.getList("items", 10)
                                                                         .stream()
                                                                         .map(tg -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(tg.getAsString())))
                                                                         .toList())
                                                           : Optional.empty();

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

        Optional<Potion> potion = nbt.contains("potion") ? Optional.ofNullable(ForgeRegistries.POTIONS.getValue(new ResourceLocation(nbt.getString("potion"))))
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