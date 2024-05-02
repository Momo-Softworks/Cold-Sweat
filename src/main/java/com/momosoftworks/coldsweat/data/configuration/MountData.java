package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public record MountData(List<Either<TagKey<EntityType<?>>, EntityType<?>>> entities, double coldInsulation, double heatInsulation, EntityRequirement requirement, Optional<List<String>> requiredMods)
{
    public static Codec<MountData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.createForgeTagCodec(ForgeRegistries.ENTITY_TYPES, Registry.ENTITY_TYPE_REGISTRY).listOf().fieldOf("entities").forGetter(MountData::entities),
            Codec.DOUBLE.fieldOf("cold_insulation").forGetter(MountData::coldInsulation),
            Codec.DOUBLE.fieldOf("heat_insulation").forGetter(MountData::heatInsulation),
            EntityRequirement.getCodec().fieldOf("requirement").forGetter(MountData::requirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(MountData::requiredMods)
    ).apply(instance, MountData::new));
}
