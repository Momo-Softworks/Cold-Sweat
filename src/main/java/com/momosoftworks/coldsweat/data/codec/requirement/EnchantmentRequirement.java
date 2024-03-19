package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;

public record EnchantmentRequirement(Enchantment enchantment, Optional<IntegerBounds> level)
{
    public static final Codec<EnchantmentRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.ENCHANTMENTS.getCodec().fieldOf("enchantment").forGetter(requirement -> requirement.enchantment),
            IntegerBounds.CODEC.optionalFieldOf("levels").forGetter(requirement -> requirement.level)
    ).apply(instance, EnchantmentRequirement::new));

    public boolean test(Enchantment enchantment, int level)
    {   return this.enchantment == enchantment && this.level.map(bounds -> bounds.test(level)).orElse(true);
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

    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("enchantment", ForgeRegistries.ENCHANTMENTS.getKey(enchantment).toString());
        level.ifPresent(bounds -> tag.put("level", bounds.serialize()));
        return tag;
    }

    public static EnchantmentRequirement deserialize(CompoundTag tag)
    {
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new net.minecraft.resources.ResourceLocation(tag.getString("enchantment")));
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
        return "Enchantment" +
                "enchantment=" + enchantment +
                ", level=" + level +
                '}';
    }
}
