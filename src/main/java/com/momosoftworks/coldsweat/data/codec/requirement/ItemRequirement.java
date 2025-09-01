package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public record ItemRequirement(NegatableList<Either<TagKey<Item>, Item>> items,
                              IntegerBounds count, IntegerBounds durability,
                              NegatableList<EnchantmentRequirement> enchantments,
                              Optional<Potion> potion, ItemComponentsRequirement components, Optional<Predicate<ItemStack>> predicate)
{
    public static final Item WILDCARD_ITEM = null;

    private static final Codec<Either<TagKey<Item>, Item>> ITEM_CODEC = Codec.either(ConfigHelper.tagOrBuiltinCodec(Registries.ITEM, BuiltInRegistries.ITEM), Codec.STRING).xmap(
        itemOrString -> {
            if (itemOrString.left().isPresent()) return itemOrString.left().get();
            String itemName = itemOrString.right().get();
            if (itemName.equals("*")) return Either.right(WILDCARD_ITEM);
            throw new IllegalArgumentException("Could not find item: " + itemName);
        },
        tagOrItem -> tagOrItem.map(left -> Either.left(Either.left(left)),
                                   right -> right == WILDCARD_ITEM ? Either.right("*") : Either.left(Either.right(right))));

    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ITEM_CODEC).optionalFieldOf("items", new NegatableList<>()).forGetter(predicate -> predicate.items),
            IntegerBounds.CODEC.optionalFieldOf("count", IntegerBounds.NONE).forGetter(predicate -> predicate.count),
            IntegerBounds.CODEC.optionalFieldOf("durability", IntegerBounds.NONE).forGetter(predicate -> predicate.durability),
            NegatableList.listCodec(EnchantmentRequirement.CODEC).optionalFieldOf("enchantments", new NegatableList<>()).forGetter(predicate -> predicate.enchantments),
            BuiltInRegistries.POTION.byNameCodec().optionalFieldOf("potion").forGetter(predicate -> predicate.potion),
            ItemComponentsRequirement.CODEC.optionalFieldOf("components", new ItemComponentsRequirement()).forGetter(predicate -> predicate.components)
    ).apply(instance, ItemRequirement::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemRequirement> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final ItemRequirement NONE = new ItemRequirement(new NegatableList<>(), IntegerBounds.NONE, IntegerBounds.NONE,
                                                                   new NegatableList<>(), Optional.empty(), new ItemComponentsRequirement());

    public ItemRequirement(NegatableList<Either<TagKey<Item>, Item>> items,
                           IntegerBounds count, IntegerBounds durability,
                           NegatableList<EnchantmentRequirement> enchantments,
                           Optional<Potion> potion, ItemComponentsRequirement components)
    {
        this(items, count, durability, enchantments, potion, components, Optional.empty());
    }

    public ItemRequirement(NegatableList<Either<TagKey<Item>, Item>> items, ItemComponentsRequirement components)
    {
        this(items, IntegerBounds.NONE, IntegerBounds.NONE, new NegatableList<>(), Optional.empty(), components);
    }

    public ItemRequirement(Collection<Item> items, @Nullable Predicate<ItemStack> predicate)
    {
        this(new NegatableList<>(items.stream().map(Either::<TagKey<Item>, Item>right).toList()), IntegerBounds.NONE, IntegerBounds.NONE,
             new NegatableList<>(), Optional.empty(), new ItemComponentsRequirement(), Optional.ofNullable(predicate));
    }

    public ItemRequirement(Predicate<ItemStack> predicate)
    {
        this(new NegatableList<>(), IntegerBounds.NONE, IntegerBounds.NONE, new NegatableList<>(), Optional.empty(), new ItemComponentsRequirement(), Optional.of(predicate));
    }

    public boolean test(ItemStack stack, boolean ignoreCount)
    {
        if (stack.isEmpty() && !items.isEmpty())
        {   return false;
        }

        if (!items.test(either -> either.map(stack::is, right -> right == WILDCARD_ITEM || stack.is(right))))
        {   return false;
        }
        if (this.predicate.isPresent())
        {   return this.predicate.get().test(stack);
        }
        if (!this.components.test(stack))
        {   return false;
        }
        if (!ignoreCount && !count.test(stack.getCount()))
        {   return false;
        }
        if (!durability.test(stack.getMaxDamage() - stack.getDamageValue()))
        {   return false;
        }
        if (potion.isPresent() && !potion.get().getEffects().equals(stack.getOrDefault(DataComponents.POTION_CONTENTS, new PotionContents(Potions.AWKWARD)).potion().get().value().getEffects()))
        {   return false;
        }
        if (!enchantments.isEmpty())
        {
            ItemEnchantments stackEnchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            stackEnchantments.entrySet().addAll(stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet());
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
            && components.equals(that.components)
            && predicate.equals(that.predicate);
    }
}
