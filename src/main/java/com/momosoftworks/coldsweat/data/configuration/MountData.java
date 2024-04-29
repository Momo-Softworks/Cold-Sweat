package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Optional;

public class MountData implements IForgeRegistryEntry<MountData>
{
    public List<Either<ITag<EntityType<?>>, EntityType<?>>> entities;
    public double coldInsulation;
    public double heatInsulation;
    public EntityRequirement requirement;
    public Optional<List<String>> requiredMods;

    public MountData(List<Either<ITag<EntityType<?>>, ResourceLocation>> entities, double coldInsulation, double heatInsulation,
                     EntityRequirement requirement, Optional<List<String>> requiredMods)
    {   this.entities = entities;
        this.coldInsulation = coldInsulation;
        this.heatInsulation = heatInsulation;
        this.requirement = requirement;
        this.requiredMods = requiredMods;
    }
    public static Codec<MountData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(ITag.codec(EntityTypeTags::getAllTags), Registry.ENTITY_TYPE).listOf().fieldOf("entities").forGetter(data -> data.entities),
            Codec.DOUBLE.fieldOf("cold_insulation").forGetter(data -> data.coldInsulation),
            Codec.DOUBLE.fieldOf("heat_insulation").forGetter(data -> data.heatInsulation),
            EntityRequirement.getCodec().fieldOf("requirement").forGetter(data -> data.requirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
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
        return null;
    }
}
