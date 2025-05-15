package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public record EnchantmentRequirement(Either<TagKey<Enchantment>, Enchantment> enchantment, IntegerBounds level)
{
    public static final Codec<EnchantmentRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrBuiltinCodec(Registry.ENCHANTMENT_REGISTRY, ForgeRegistries.ENCHANTMENTS).fieldOf("enchantment").forGetter(requirement -> requirement.enchantment),
            IntegerBounds.CODEC.optionalFieldOf("levels", IntegerBounds.NONE).forGetter(requirement -> requirement.level)
    ).apply(instance, EnchantmentRequirement::new));

    public boolean test(Enchantment enchantment, int level)
    {
        return this.enchantment.map(tag -> ForgeRegistries.ENCHANTMENTS.tags().getTag(tag).contains(enchantment),
                                    enchantment::equals)
            && this.level.test(level);
    }

    public boolean test(Map<Enchantment, Integer> enchantments)
    {
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
        {
            if (test(entry.getKey(), entry.getValue()))
            {   return true;
            }
        }
        return false;
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

        EnchantmentRequirement that = (EnchantmentRequirement) obj;
        return enchantment.equals(that.enchantment) && level.equals(that.level);
    }
}
