package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.ITag;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public class MountData
{
    public List<Either<ITag<EntityType<?>>, EntityType<?>>> entities;
    public double coldInsulation;
    public double heatInsulation;
    public EntityRequirement requirement;
    public Optional<List<String>> requiredMods;

    public MountData(List<Either<ITag<EntityType<?>>, EntityType<?>>> entities, double coldInsulation, double heatInsulation,
                     EntityRequirement requirement, Optional<List<String>> requiredMods)
    {   this.entities = entities;
        this.coldInsulation = coldInsulation;
        this.heatInsulation = heatInsulation;
        this.requirement = requirement;
        this.requiredMods = requiredMods;
    }
    public static Codec<MountData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryHelper.createForgeTagCodec(ForgeRegistries.ENTITIES, Registry.ENTITY_TYPE).listOf().fieldOf("entities").forGetter(data -> data.entities),
            Codec.DOUBLE.fieldOf("cold_insulation").forGetter(data -> data.coldInsulation),
            Codec.DOUBLE.fieldOf("heat_insulation").forGetter(data -> data.heatInsulation),
            EntityRequirement.getCodec().fieldOf("requirement").forGetter(data -> data.requirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, MountData::new));

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MountData{entities=[");
        for (Either<ITag<EntityType<?>>, EntityType<?>> entity : entities)
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
