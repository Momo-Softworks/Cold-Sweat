package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Optional;

public record MountData(List<Either<TagKey<EntityType<?>>, EntityType<?>>> entities, double coldInsulation, double heatInsulation, EntityRequirement requirement, Optional<List<String>> requiredMods) implements IForgeRegistryEntry<MountData>
{
    public static Codec<MountData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.createForgeTagCodec(ForgeRegistries.ENTITIES, Registry.ENTITY_TYPE_REGISTRY).listOf().fieldOf("entities").forGetter(MountData::entities),
            Codec.DOUBLE.fieldOf("cold_insulation").forGetter(MountData::coldInsulation),
            Codec.DOUBLE.fieldOf("heat_insulation").forGetter(MountData::heatInsulation),
            EntityRequirement.getCodec().fieldOf("requirement").forGetter(MountData::requirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(MountData::requiredMods)
    ).apply(instance, MountData::new));

    @Override
    public MountData setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<MountData> getRegistryType()
    {
        return MountData.class;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MountData{entities=[");
        for (Either<TagKey<EntityType<?>>, EntityType<?>> entity : entities)
        {
            if (entity.left().isPresent())
            {   builder.append("#").append(entity.left().get().toString());
            }
            else
            {   builder.append(entity.right().get().toString());
            }
            builder.append(", ");
        }
        builder.append("], coldInsulation=").append(coldInsulation).append(", heatInsulation=").append(heatInsulation).append(", requirement=").append(requirement);
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");
        return builder.toString();
    }
}
