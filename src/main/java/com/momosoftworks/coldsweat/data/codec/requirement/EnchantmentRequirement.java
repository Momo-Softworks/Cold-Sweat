package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.OptionalHolder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;


public record EnchantmentRequirement(Either<TagKey<Enchantment>, OptionalHolder<Enchantment>> enchantment, IntegerBounds level)
{
    public static final Codec<EnchantmentRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrHolderCodec(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(requirement -> requirement.enchantment),
            IntegerBounds.CODEC.optionalFieldOf("levels", IntegerBounds.NONE).forGetter(requirement -> requirement.level)
    ).apply(instance, EnchantmentRequirement::new));

    public static StreamCodec<RegistryFriendlyByteBuf, EnchantmentRequirement> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public boolean test(Holder<Enchantment> enchantment, int level)
    {
        return this.enchantment.map(tag -> enchantment.is(tag),
                                    opt -> opt.is(enchantment))
            && this.level.test(level);
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
