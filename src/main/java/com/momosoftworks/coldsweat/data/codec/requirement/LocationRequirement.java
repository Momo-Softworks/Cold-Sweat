package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.WorldTempBounds;
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

public record LocationRequirement(IntegerBounds x, IntegerBounds y, IntegerBounds z,
                                  int xOffset, int yOffset, int zOffset,
                                  NegatableList<Either<TagKey<Biome>, ResourceKey<Biome>>> biome,
                                  NegatableList<Either<TagKey<Structure>, ResourceKey<Structure>>> structure,
                                  NegatableList<Either<TagKey<Level>, ResourceKey<Level>>> dimension,
                                  IntegerBounds light, BlockRequirement block,
                                  FluidRequirement fluid, WorldTempBounds temperature,
                                  Optional<Predicate<BlockInWorld>> predicate)
{
    public static final Codec<LocationRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntegerBounds.CODEC.optionalFieldOf("x", IntegerBounds.NONE).forGetter(location -> location.x),
            IntegerBounds.CODEC.optionalFieldOf("y", IntegerBounds.NONE).forGetter(location -> location.y),
            IntegerBounds.CODEC.optionalFieldOf("z", IntegerBounds.NONE).forGetter(location -> location.z),
            Codec.INT.optionalFieldOf("x_offset", 0).forGetter(location -> location.xOffset),
            Codec.INT.optionalFieldOf("y_offset", 0).forGetter(location -> location.yOffset),
            Codec.INT.optionalFieldOf("z_offset", 0).forGetter(location -> location.zOffset),
            NegatableList.listCodec(ConfigHelper.tagOrResourceKeyCodec(Registries.BIOME)).optionalFieldOf("biome", new NegatableList<>()).forGetter(location -> location.biome),
            NegatableList.listCodec(ConfigHelper.tagOrResourceKeyCodec(Registries.STRUCTURE)).optionalFieldOf("structure", new NegatableList<>()).forGetter(location -> location.structure),
            NegatableList.listCodec(ConfigHelper.tagOrResourceKeyCodec(Registries.DIMENSION)).optionalFieldOf("dimension", new NegatableList<>()).forGetter(location -> location.dimension),
            IntegerBounds.CODEC.optionalFieldOf("light", IntegerBounds.NONE).forGetter(location -> location.light),
            BlockRequirement.CODEC.optionalFieldOf("block", BlockRequirement.NONE).forGetter(location -> location.block),
            FluidRequirement.CODEC.optionalFieldOf("fluid", FluidRequirement.NONE).forGetter(location -> location.fluid),
            WorldTempBounds.CODEC.optionalFieldOf("temperature", WorldTempBounds.NONE).forGetter(location -> location.temperature)
    ).apply(instance, LocationRequirement::new));

    public LocationRequirement(IntegerBounds x, IntegerBounds y, IntegerBounds z,
                               int xOffset, int yOffset, int zOffset,
                               NegatableList<Either<TagKey<Biome>, ResourceKey<Biome>>> biome,
                               NegatableList<Either<TagKey<Structure>, ResourceKey<Structure>>> structure,
                               NegatableList<Either<TagKey<Level>, ResourceKey<Level>>> dimension,
                               IntegerBounds light, BlockRequirement block,
                               FluidRequirement fluid, WorldTempBounds temperature)
    {
        this(x, y, z, xOffset, yOffset, zOffset, biome, structure, dimension, light, block, fluid, temperature, Optional.empty());
    }

    public LocationRequirement(Predicate<BlockInWorld> predicate)
    {
        this(IntegerBounds.NONE, IntegerBounds.NONE, IntegerBounds.NONE,
             0, 0, 0,
             new NegatableList<>(), new NegatableList<>(), new NegatableList<>(),
             IntegerBounds.NONE, BlockRequirement.NONE,
             FluidRequirement.NONE, WorldTempBounds.NONE,
             Optional.of(predicate));
    }

    public static final LocationRequirement NONE = new LocationRequirement(IntegerBounds.NONE, IntegerBounds.NONE, IntegerBounds.NONE,
                                                                           0, 0, 0,
                                                                           new NegatableList<>(), new NegatableList<>(), new NegatableList<>(),
                                                                           IntegerBounds.NONE, BlockRequirement.NONE,
                                                                           FluidRequirement.NONE, WorldTempBounds.NONE);

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

        if (!this.x.test(pos.getX())) return false;
        if (!this.y.test(pos.getY())) return false;
        if (!this.z.test(pos.getZ())) return false;

        if (!this.dimension.test(either -> either.map(tag -> level.dimensionTypeRegistration().is(tag.location()),
                                                      key -> level.dimension().equals(key))))
        {   return false;
        }

        if (this.biome.test(either -> either.map(tag -> level.getBiomeManager().getNoiseBiomeAtPosition(pos).is(tag),
                                                 key -> level.getBiomeManager().getNoiseBiomeAtPosition(pos).is(key))))
        {   return false;
        }

        if (!this.structure.isEmpty())
        {
            StructureManager structureManager = WorldHelper.getServerLevel(level).structureManager();
            if (!this.structure.test(either ->
            {
                StructureStart structureStart = either.map(tag -> structureManager.getStructureWithPieceAt(pos, tag),
                                                           key -> structureManager.getStructureWithPieceAt(pos, key));
                return structureStart != null && structureStart != StructureStart.INVALID_START;
            }))
            {   return false;
            }
        }

        if (!this.light.test(level.getMaxLocalRawBrightness(pos)))
        {   return false;
        }
        if (!this.block.test(level, pos))
        {   return false;
        }
        if (!this.fluid.test(level, pos))
        {   return false;
        }
        if (!this.temperature.test(WorldHelper.getRoughTemperatureAt(level, pos)))
        {   return false;
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
