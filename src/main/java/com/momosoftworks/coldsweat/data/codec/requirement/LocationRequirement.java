package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record LocationRequirement(Optional<Integer> x, Optional<Integer> y, Optional<Integer> z,
                                  Optional<Either<TagKey<Biome>, ResourceKey<Biome>>> biome,
                                  Optional<Either<TagKey<Structure>, ResourceKey<Structure>>> structure,
                                  Optional<Either<TagKey<Level>, ResourceKey<Level>>> dimension,
                                  Optional<IntegerBounds> light, Optional<BlockRequirement> block,
                                  Optional<FluidRequirement> fluid)
{
    public static final Codec<LocationRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("x").forGetter(location -> location.x),
            Codec.INT.optionalFieldOf("y").forGetter(location -> location.y),
            Codec.INT.optionalFieldOf("z").forGetter(location -> location.z),
            ConfigHelper.tagOrResourceKeyCodec(Registry.BIOME_REGISTRY).optionalFieldOf("biome").forGetter(location -> location.biome),
            ConfigHelper.tagOrResourceKeyCodec(Registry.STRUCTURE_REGISTRY).optionalFieldOf("structure").forGetter(location -> location.structure),
            ConfigHelper.tagOrResourceKeyCodec(Registry.DIMENSION_REGISTRY).optionalFieldOf("dimension").forGetter(location -> location.dimension),
            IntegerBounds.CODEC.optionalFieldOf("light").forGetter(location -> location.light),
            BlockRequirement.CODEC.optionalFieldOf("block").forGetter(location -> location.block),
            FluidRequirement.CODEC.optionalFieldOf("fluid").forGetter(location -> location.fluid)
    ).apply(instance, LocationRequirement::new));

    public boolean test(Level level, Vec3 pos)
    {   return this.test(level, new BlockPos(pos));
    }

    public boolean test(Level level, BlockPos origin)
    {
        BlockPos.MutableBlockPos pos = origin.mutable();
        this.x.ifPresent(x -> pos.move(x, 0, 0));
        this.y.ifPresent(y -> pos.move(0, y, 0));
        this.z.ifPresent(z -> pos.move(0, 0, z));

        if (this.dimension.isPresent()
        && !level.dimension().equals(this.dimension.get()))
        {   return false;
        }

        if (this.biome.isPresent()
        && !level.getBiome(pos).unwrapKey().get().equals(this.biome.get()))
        {   return false;
        }

        if (this.structure.isPresent())
        {
            StructureManager structureManager = WorldHelper.getServerLevel(level).structureManager();
            StructureStart structureStart = this.structure.get().map(tag -> structureManager.getStructureWithPieceAt(pos, tag),
                                                                     key -> structureManager.getStructureWithPieceAt(pos, key));
            if (structureStart == null || structureStart == StructureStart.INVALID_START)
            {   return false;
            }
        }

        if (this.light.isPresent())
        {
            int light = level.getMaxLocalRawBrightness(pos);
            if (light < this.light.get().min() || light > this.light.get().max())
            {   return false;
            }
        }
        if (this.block.isPresent() && !this.block.get().test(level, pos))
        {   return false;
        }
        if (this.fluid.isPresent() && !this.fluid.get().test(level, pos))
        {
            return false;
        }
        return true;
    }

    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        x.ifPresent(value -> tag.putInt("x", value));
        y.ifPresent(value -> tag.putInt("y", value));
        z.ifPresent(value -> tag.putInt("z", value));
        biome.ifPresent(value ->
        {   tag.putString("biome", ConfigHelper.serializeTagOrResourceKey(value));
        });
        structure.ifPresent(value ->
        {   tag.putString("structure", ConfigHelper.serializeTagOrResourceKey(value));
        });
        dimension.ifPresent(value ->
        {   tag.putString("dimension", ConfigHelper.serializeTagOrResourceKey(value));
        });
        light.ifPresent(bounds -> tag.put("light", bounds.serialize()));
        block.ifPresent(predicate -> tag.put("block", predicate.serialize()));
        fluid.ifPresent(predicate -> tag.put("fluid", predicate.serialize()));
        return tag;
    }

    public static LocationRequirement deserialize(CompoundTag tag)
    {
        Optional<Integer> x = tag.contains("x") ? Optional.of(tag.getInt("x")) : Optional.empty();
        Optional<Integer> y = tag.contains("y") ? Optional.of(tag.getInt("y")) : Optional.empty();
        Optional<Integer> z = tag.contains("z") ? Optional.of(tag.getInt("z")) : Optional.empty();
        Optional<Either<TagKey<Biome>, ResourceKey<Biome>>> biome = tag.contains("biome")
                                                                    ? Optional.of(ConfigHelper.deserializeTagOrResourceKey(Registry.BIOME_REGISTRY, tag.getString("biome")))
                                                                    : Optional.empty();
        Optional<Either<TagKey<Structure>, ResourceKey<Structure>>> structure = tag.contains("structure")
                                                                                ? Optional.of(ConfigHelper.deserializeTagOrResourceKey(Registry.STRUCTURE_REGISTRY, tag.getString("structure")))
                                                                                : Optional.empty();
        Optional<Either<TagKey<Level>, ResourceKey<Level>>> dimension = tag.contains("dimension")
                                                                        ? Optional.of(ConfigHelper.deserializeTagOrResourceKey(Registry.DIMENSION_REGISTRY, tag.getString("dimension")))
                                                                        : Optional.empty();
        Optional<IntegerBounds> light = tag.contains("light") ? Optional.of(IntegerBounds.deserialize(tag.getCompound("light"))) : Optional.empty();
        Optional<BlockRequirement> block = tag.contains("block") ? Optional.of(BlockRequirement.deserialize(tag.getCompound("block"))) : Optional.empty();
        Optional<FluidRequirement> fluid = tag.contains("fluid") ? Optional.of(FluidRequirement.deserialize(tag.getCompound("fluid"))) : Optional.empty();
        return new LocationRequirement(x, y, z, biome, structure, dimension, light, block, fluid);
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

        LocationRequirement that = (LocationRequirement) obj;

        return x.equals(that.x)
            && y.equals(that.y)
            && z.equals(that.z)
            && biome.equals(that.biome)
            && structure.equals(that.structure)
            && dimension.equals(that.dimension)
            && light.equals(that.light)
            && block.equals(that.block)
            && fluid.equals(that.fluid);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("LocationRequirement{");
        x.ifPresent(value -> builder.append("x=").append(value).append(", "));
        y.ifPresent(value -> builder.append("y=").append(value).append(", "));
        z.ifPresent(value -> builder.append("z=").append(value).append(", "));
        biome.ifPresent(value -> builder.append("biome=").append(value).append(", "));
        structure.ifPresent(value -> builder.append("structure=").append(value).append(", "));
        dimension.ifPresent(value -> builder.append("dimension=").append(value).append(", "));
        light.ifPresent(value -> builder.append("light=").append(value).append(", "));
        block.ifPresent(value -> builder.append("block=").append(value).append(", "));
        fluid.ifPresent(value -> builder.append("fluid=").append(value).append(", "));
        builder.append("}");

        return builder.toString();
    }
}
