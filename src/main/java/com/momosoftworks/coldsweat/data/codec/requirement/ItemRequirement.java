package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.CommonStreamCodecs;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public record ItemRequirement(List<Either<TagKey<Item>, Item>> items,
                              Optional<IntegerBounds> count, Optional<IntegerBounds> durability,
                              Optional<List<EnchantmentRequirement>> enchantments,
                              Optional<Potion> potion, ItemComponentsRequirement components, Optional<Predicate<ItemStack>> predicate)
{
    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrBuiltinCodec(Registries.ITEM, BuiltInRegistries.ITEM).listOf().optionalFieldOf("items", List.of()).forGetter(predicate -> predicate.items),
            IntegerBounds.CODEC.optionalFieldOf("count").forGetter(predicate -> predicate.count),
            IntegerBounds.CODEC.optionalFieldOf("durability").forGetter(predicate -> predicate.durability),
            EnchantmentRequirement.CODEC.listOf().optionalFieldOf("enchantments").forGetter(predicate -> predicate.enchantments),
            BuiltInRegistries.POTION.byNameCodec().optionalFieldOf("potion").forGetter(predicate -> predicate.potion),
            ItemComponentsRequirement.CODEC.optionalFieldOf("components", new ItemComponentsRequirement()).forGetter(predicate -> predicate.components)
    ).apply(instance, ItemRequirement::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemRequirement> STREAM_CODEC = StreamCodec.of(
    (buf, predicate) ->
    {
        CommonStreamCodecs.writeList(buf, predicate.items, CommonStreamCodecs.tagOrRegistryCodec(Registries.ITEM));
        buf.writeOptional(predicate.count, IntegerBounds.STREAM_CODEC);
        buf.writeOptional(predicate.durability, IntegerBounds.STREAM_CODEC);
        CommonStreamCodecs.writeOptionalList(buf, predicate.enchantments, EnchantmentRequirement.STREAM_CODEC);
        CommonStreamCodecs.writeOptional(buf, predicate.potion, ByteBufCodecs.registry(Registries.POTION));
        ItemComponentsRequirement.STREAM_CODEC.encode(buf, predicate.components);
    },
    (buf) ->
    {
        List<Either<TagKey<Item>, Item>> items = CommonStreamCodecs.readList(buf, CommonStreamCodecs.tagOrRegistryCodec(Registries.ITEM));
        Optional<IntegerBounds> count = buf.readOptional(IntegerBounds.STREAM_CODEC);
        Optional<IntegerBounds> durability = buf.readOptional(IntegerBounds.STREAM_CODEC);
        Optional<List<EnchantmentRequirement>> enchantments = CommonStreamCodecs.readOptionalList(buf, EnchantmentRequirement.STREAM_CODEC);
        Optional<Potion> potion = CommonStreamCodecs.readOptional(buf, ByteBufCodecs.registry(Registries.POTION));
        ItemComponentsRequirement components = ItemComponentsRequirement.STREAM_CODEC.decode(buf);
        return new ItemRequirement(items, count, durability, enchantments, potion, components);
    });

    public static final ItemRequirement NONE = new ItemRequirement(List.of(), Optional.empty(), Optional.empty(),
                                                                   Optional.empty(), Optional.empty(), new ItemComponentsRequirement());

    public ItemRequirement(List<Either<TagKey<Item>, Item>> items,
                           Optional<IntegerBounds> count, Optional<IntegerBounds> durability,
                           Optional<List<EnchantmentRequirement>> enchantments,
                           Optional<Potion> potion, ItemComponentsRequirement components)
    {
        this(items, count, durability, enchantments, potion, components, Optional.empty());
    }

    public ItemRequirement(List<Either<TagKey<Item>, Item>> items, ItemComponentsRequirement components)
    {
        this(items, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), components);
    }

    public ItemRequirement(Collection<Item> items, @Nullable Predicate<ItemStack> predicate)
    {
        this(items.stream().map(Either::<TagKey<Item>, Item>right).toList(), Optional.empty(), Optional.empty(),
             Optional.empty(), Optional.empty(), new ItemComponentsRequirement(), Optional.ofNullable(predicate));
    }

    public ItemRequirement(Predicate<ItemStack> predicate)
    {
        this(List.of(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), new ItemComponentsRequirement(), Optional.of(predicate));
    }

    public boolean test(ItemStack stack, boolean ignoreCount)
    {
        if (stack.isEmpty() && !items.isEmpty())
        {   return false;
        }

        if (!items.isEmpty())
        checkItem:
        {
            for (int i = 0; i < items.size(); i++)
            {
                Either<TagKey<Item>, Item> either = items.get(i);
                if (either.map(stack::is, stack::is))
                {   break checkItem;
                }
            }
            return false;
        }
        if (this.predicate.isPresent())
        {   return this.predicate.get().test(stack);
        }
        if (!this.components.test(stack.getComponents()))
        {   return false;
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
            ItemEnchantments stackEnchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            stackEnchantments.entrySet().addAll(stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet());
            for (EnchantmentRequirement enchantment : enchantments.get())
            {
                if (!enchantment.test(stackEnchantments))
                {   return false;
                }
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
            && components.equals(that.components)
            && predicate.equals(that.predicate);
    }
}
