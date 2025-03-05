package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Predicate;

public record LocationRequirement(Optional<IntegerBounds> x, Optional<IntegerBounds> y, Optional<IntegerBounds> z,
                                  int xOffset, int yOffset, int zOffset,
                                  Optional<Either<TagKey<Biome>, ResourceKey<Biome>>> biome,
                                  Optional<Either<TagKey<Structure>, ResourceKey<Structure>>> structure,
                                  Optional<Either<TagKey<Level>, ResourceKey<Level>>> dimension,
                                  Optional<IntegerBounds> light, Optional<BlockRequirement> block,
                                  Optional<FluidRequirement> fluid, Optional<Predicate<BlockInWorld>> predicate)
{
    public static final Codec<LocationRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntegerBounds.CODEC.optionalFieldOf("x").forGetter(location -> location.x),
            IntegerBounds.CODEC.optionalFieldOf("y").forGetter(location -> location.y),
            IntegerBounds.CODEC.optionalFieldOf("z").forGetter(location -> location.z),
            Codec.INT.optionalFieldOf("x_offset", 0).forGetter(location -> location.xOffset),
            Codec.INT.optionalFieldOf("y_offset", 0).forGetter(location -> location.yOffset),
            Codec.INT.optionalFieldOf("z_offset", 0).forGetter(location -> location.zOffset),
            ConfigHelper.tagOrResourceKeyCodec(Registries.BIOME).optionalFieldOf("biome").forGetter(location -> location.biome),
            ConfigHelper.tagOrResourceKeyCodec(Registries.STRUCTURE).optionalFieldOf("structure").forGetter(location -> location.structure),
            ConfigHelper.tagOrResourceKeyCodec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(location -> location.dimension),
            IntegerBounds.CODEC.optionalFieldOf("light").forGetter(location -> location.light),
            BlockRequirement.CODEC.optionalFieldOf("block").forGetter(location -> location.block),
            FluidRequirement.CODEC.optionalFieldOf("fluid").forGetter(location -> location.fluid)
    ).apply(instance, LocationRequirement::new));

    public LocationRequirement(Optional<IntegerBounds> x, Optional<IntegerBounds> y, Optional<IntegerBounds> z,
                               int xOffset, int yOffset, int zOffset,
                               Optional<Either<TagKey<Biome>, ResourceKey<Biome>>> biome,
                               Optional<Either<TagKey<Structure>, ResourceKey<Structure>>> structure,
                               Optional<Either<TagKey<Level>, ResourceKey<Level>>> dimension,
                               Optional<IntegerBounds> light, Optional<BlockRequirement> block,
                               Optional<FluidRequirement> fluid)
    {
        this(x, y, z, xOffset, yOffset, zOffset, biome, structure, dimension, light, block, fluid, Optional.empty());
    }

    public LocationRequirement(Predicate<BlockInWorld> predicate)
    {
        this(Optional.empty(), Optional.empty(), Optional.empty(),
             0, 0, 0,
             Optional.empty(), Optional.empty(), Optional.empty(),
             Optional.empty(), Optional.empty(), Optional.empty(),
             Optional.of(predicate));
    }

    public static final LocationRequirement NONE = new LocationRequirement(Optional.empty(), Optional.empty(), Optional.empty(),
                                                                           0, 0, 0,
                                                                           Optional.empty(), Optional.empty(), Optional.empty(),
                                                                           Optional.empty(), Optional.empty(), Optional.empty(),
                                                                           Optional.empty());

    public boolean test(Level level, Vec3 pos)
    {   return this.test(level, BlockPos.containing(pos));
    }

    public boolean test(Level level, BlockPos origin)
    {
        if (this.predicate.isPresent())
        {   return this.predicate.get().test(new BlockInWorld(level, origin, true));
        }

        BlockPos.MutableBlockPos pos = origin.mutable();
        pos.move(this.xOffset, this.yOffset, this.zOffset);

        if (!this.x.map(range -> range.test(pos.getX())).orElse(true)) return false;
        if (!this.y.map(range -> range.test(pos.getY())).orElse(true)) return false;
        if (!this.z.map(range -> range.test(pos.getZ())).orElse(true)) return false;

        if (this.dimension.isPresent()
        && !this.dimension.get().map(tag -> level.dimensionTypeRegistration().is(tag.location()),
                                     key -> level.dimension().equals(key)))
        {   return false;
        }

        if (this.biome.isPresent()
        && !this.biome.get().map(tag -> level.getBiomeManager().getNoiseBiomeAtPosition(pos).is(tag),
                                 key -> level.getBiomeManager().getNoiseBiomeAtPosition(pos).is(key)))
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

    @Override
    public String toString()
    {   return CODEC.encodeStart(JsonOps.INSTANCE, this).result().map(Object::toString).orElse("serialize_failed");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

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
}
