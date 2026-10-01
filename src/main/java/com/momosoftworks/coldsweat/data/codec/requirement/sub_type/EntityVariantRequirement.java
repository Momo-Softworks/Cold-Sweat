package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record EntityVariantRequirement(String variant) implements EntitySubRequirement
{
    public static final MapCodec<EntityVariantRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("variant").forGetter(requirement -> requirement.variant)
    ).apply(instance, EntityVariantRequirement::new));

    @Override
    public MapCodec<? extends EntitySubRequirement> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean test(Entity entity, Level level, @Nullable Vec3 position)
    {
        // Entity variants are stored as data components (i.e. "minecraft:fox/variant")
        for (DataComponentType<?> type : BuiltInRegistries.DATA_COMPONENT_TYPE)
        {
            Identifier typeId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
            if (typeId == null || !typeId.getPath().endsWith("/variant")) continue;

            Object value = entity.get(type);
            if (value == null) continue;
            if (value instanceof StringRepresentable variantType && variantType.getSerializedName().equals(this.variant))
            {   return true;
            }
            if (value instanceof Holder<?> holder
            && holder.unwrapKey().map(key -> key.identifier().toString().equals(this.variant) || key.identifier().getPath().equals(this.variant)).orElse(false))
            {   return true;
            }
        }
        return false;
    }
}
