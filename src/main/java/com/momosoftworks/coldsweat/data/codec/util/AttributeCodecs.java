package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeCodecs
{
    public static Codec<AttributeModifier.Operation> OPERATION_CODEC = Codec.STRING.xmap(
            string -> AttributeModifier.Operation.valueOf(string.toUpperCase()),
            operation -> operation.name().toLowerCase()
    );

    public static Codec<AttributeModifier> MODIFIER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("name").forGetter(AttributeModifier::id),
                    Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::amount),
                    OPERATION_CODEC.fieldOf("operation").forGetter(AttributeModifier::operation)
            ).apply(instance, AttributeModifier::new)
    );

    public static Codec<Attribute> ATTRIBUTE_CODEC = ResourceLocation.CODEC.xmap(
            BuiltInRegistries.ATTRIBUTE::get,
            BuiltInRegistries.ATTRIBUTE::getKey
    );
}
