package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

public record EnchantmentRequirement(Either<TagKey<Enchantment>, Holder<Enchantment>> enchantment, Optional<IntegerBounds> level)
{
    public static final Codec<EnchantmentRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrHolderCodec(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(requirement -> requirement.enchantment),
            IntegerBounds.CODEC.optionalFieldOf("levels").forGetter(requirement -> requirement.level)
    ).apply(instance, EnchantmentRequirement::new));

    public boolean test(Holder<Enchantment> enchantment, int level)
    {
        return this.enchantment.map(
                     tag -> enchantment.is(tag),
                     ench -> ench == enchantment)
               && this.level.map(bounds -> bounds.test(level)).orElse(true);
    }

    public boolean test(ItemEnchantments enchantments)
    {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet())
        {
            if (test(entry.getKey(), entry.getValue()))
            {   return true;
            }
        }
        return false;
    }

    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("enchantment", ConfigHelper.serializeTagOrRegistryObject(Registries.ENCHANTMENT, enchantment));
        level.ifPresent(bounds -> tag.put("level", bounds.serialize()));
        return tag;
    }

    public static EnchantmentRequirement deserialize(CompoundTag tag)
    {
        Either<TagKey<Enchantment>, Holder<Enchantment>> enchantment = ConfigHelper.deserializeTagOrRegistryObject(tag.getString("enchantment"), Registries.ENCHANTMENT);
        IntegerBounds level = tag.contains("level") ? IntegerBounds.deserialize(tag.getCompound("level")) : null;
        return new EnchantmentRequirement(enchantment, Optional.ofNullable(level));
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

        EnchantmentRequirement that = (EnchantmentRequirement) obj;

        if (!enchantment.equals(that.enchantment))
        {   return false;
        }
        return level.equals(that.level);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(ConfigHelper.serializeTagOrRegistryObject(Registries.ENCHANTMENT, enchantment));
        level.ifPresent(bounds -> builder.append(bounds));

        return builder.toString();
    }
}
