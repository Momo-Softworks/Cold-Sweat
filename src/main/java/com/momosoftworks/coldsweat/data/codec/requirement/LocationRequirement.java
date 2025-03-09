package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.BlockInWorld;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.WorldTempBounds;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;

import java.util.Optional;
import java.util.function.Predicate;

public class LocationRequirement
{
    private final Optional<IntegerBounds> x;
    private final Optional<IntegerBounds> y;
    private final Optional<IntegerBounds> z;
    int xOffset;
    int yOffset;
    int zOffset;
    private final Optional<RegistryKey<Biome>> biome;
    private final Optional<RegistryKey<Structure<?>>> structure;
    private final Optional<RegistryKey<World>> dimension;
    private final Optional<IntegerBounds> light;
    private final Optional<BlockRequirement> block;
    private final Optional<FluidRequirement> fluid;
    private final Optional<WorldTempBounds> temperature;
    private final Optional<Predicate<BlockInWorld>> predicate;

    public LocationRequirement(Optional<IntegerBounds> x, Optional<IntegerBounds> y, Optional<IntegerBounds> z,
                               int xOffset, int yOffset, int zOffset,
                               Optional<RegistryKey<Biome>> biome, Optional<RegistryKey<Structure<?>>> structure,
                               Optional<RegistryKey<World>> dimension, Optional<IntegerBounds> light,
                               Optional<BlockRequirement> block, Optional<FluidRequirement> fluid,
                               Optional<WorldTempBounds> temperature,
                               Optional<Predicate<BlockInWorld>> predicate)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.biome = biome;
        this.structure = structure;
        this.dimension = dimension;
        this.light = light;
        this.block = block;
        this.fluid = fluid;
        this.temperature = temperature;
        this.predicate = predicate;
    }

    public static final Codec<LocationRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntegerBounds.CODEC.optionalFieldOf("x").forGetter(location -> location.x),
            IntegerBounds.CODEC.optionalFieldOf("y").forGetter(location -> location.y),
            IntegerBounds.CODEC.optionalFieldOf("z").forGetter(location -> location.z),
            Codec.INT.optionalFieldOf("x_offset", 0).forGetter(location -> location.xOffset),
            Codec.INT.optionalFieldOf("y_offset", 0).forGetter(location -> location.yOffset),
            Codec.INT.optionalFieldOf("z_offset", 0).forGetter(location -> location.zOffset),
            ExtraCodecs.codec(Registry.BIOME_REGISTRY).optionalFieldOf("biome").forGetter(location -> location.biome),
            ExtraCodecs.codec(Registry.STRUCTURE_FEATURE_REGISTRY).optionalFieldOf("structure").forGetter(location -> location.structure),
            ExtraCodecs.codec(Registry.DIMENSION_REGISTRY).optionalFieldOf("dimension").forGetter(location -> location.dimension),
            IntegerBounds.CODEC.optionalFieldOf("light").forGetter(location -> location.light),
            BlockRequirement.CODEC.optionalFieldOf("block").forGetter(location -> location.block),
            FluidRequirement.CODEC.optionalFieldOf("fluid").forGetter(location -> location.fluid),
            WorldTempBounds.CODEC.optionalFieldOf("temperature").forGetter(location -> location.temperature)
    ).apply(instance, LocationRequirement::new));

    public LocationRequirement(Optional<IntegerBounds> x, Optional<IntegerBounds> y, Optional<IntegerBounds> z,
                               int xOffset, int yOffset, int zOffset,
                               Optional<RegistryKey<Biome>> biome,
                               Optional<RegistryKey<Structure<?>>> structure,
                               Optional<RegistryKey<World>> dimension,
                               Optional<IntegerBounds> light, Optional<BlockRequirement> block,
                               Optional<FluidRequirement> fluid, Optional<WorldTempBounds> temperature)
    {
        this(x, y, z, xOffset, yOffset, zOffset, biome, structure, dimension, light, block, fluid, temperature, Optional.empty());
    }

    public LocationRequirement(Predicate<BlockInWorld> predicate)
    {
        this(Optional.empty(), Optional.empty(), Optional.empty(),
             0, 0, 0,
             Optional.empty(), Optional.empty(), Optional.empty(),
             Optional.empty(), Optional.empty(), Optional.empty(),
             Optional.empty(), Optional.of(predicate));
    }

    public static final LocationRequirement NONE = new LocationRequirement(Optional.empty(), Optional.empty(), Optional.empty(),
                                                                           0, 0, 0,
                                                                           Optional.empty(), Optional.empty(), Optional.empty(),
                                                                           Optional.empty(), Optional.empty(), Optional.empty(),
                                                                           Optional.empty(), Optional.empty());

    public Optional<IntegerBounds> x()
    {   return x;
    }
    public Optional<IntegerBounds> y()
    {   return y;
    }
    public Optional<IntegerBounds> z()
    {   return z;
    }
    public int xOffset()
    {   return xOffset;
    }
    public int yOffset()
    {   return yOffset;
    }
    public int zOffset()
    {   return zOffset;
    }
    public Optional<RegistryKey<Biome>> biome()
    {   return biome;
    }
    public Optional<RegistryKey<Structure<?>>> structure()
    {   return structure;
    }
    public Optional<RegistryKey<World>> dimension()
    {   return dimension;
    }
    public Optional<IntegerBounds> light()
    {   return light;
    }
    public Optional<BlockRequirement> block()
    {   return block;
    }
    public Optional<FluidRequirement> fluid()
    {   return fluid;
    }
    public Optional<WorldTempBounds> temperature()
    {   return temperature;
    }

    public boolean test(World level, Vector3d pos)
    {   return this.test(level, new BlockPos(pos));
    }

    public boolean test(World level, BlockPos origin)
    {
        if (this.predicate.isPresent())
        {   return this.predicate.get().test(new BlockInWorld(level, origin, true));
        }

        BlockPos.Mutable pos = origin.mutable();
        pos.move(this.xOffset, this.yOffset, this.zOffset);

        if (!this.x.map(range -> range.test(pos.getX())).orElse(true)) return false;
        if (!this.y.map(range -> range.test(pos.getY())).orElse(true)) return false;
        if (!this.z.map(range -> range.test(pos.getZ())).orElse(true)) return false;

        if (this.dimension.isPresent()
        && !level.dimension().equals(this.dimension.get()))
        {   return false;
        }

        if (this.biome.isPresent()
        && !this.biome.get().location().equals(level.getBiome(pos).getRegistryName()))
        {   return false;
        }

        if (this.structure.isPresent()
        && WorldHelper.getServerLevel(level).structureFeatureManager().getStructureAt(pos, false,
                                                                                      level.registryAccess().registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY).get(this.structure.get())) == StructureStart.INVALID_START)
        {   return false;
        }

        if (this.light.isPresent())
        {
            int light = level.getMaxLocalRawBrightness(pos);
            if (light < this.light.get().min || light > this.light.get().max)
            {   return false;
            }
        }
        if (this.block.isPresent() && !this.block.get().test(level, pos))
        {   return false;
        }
        if (this.fluid.isPresent() && !this.fluid.get().test(level, pos))
        {   return false;
        }
        if (this.temperature.isPresent() && !this.temperature.get().test(WorldHelper.getRoughTemperatureAt(level, pos)))
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
