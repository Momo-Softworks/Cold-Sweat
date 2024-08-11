package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;

public class DepthTempData implements IForgeRegistryEntry<DepthTempData>
{
    public static final Codec<DepthTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TempRegion.CODEC.listOf().fieldOf("regions").forGetter(data -> data.temperatures)
    ).apply(instance, DepthTempData::new));

    public final List<TempRegion> temperatures;

    public DepthTempData(List<TempRegion> temperatures)
    {   this.temperatures = temperatures;
    }

    public boolean withinBounds(World level, BlockPos pos)
    {
        for (TempRegion region : temperatures)
        {
            if (region.withinBounds(level, pos))
            {   return true;
            }
        }
        return false;
    }

    public Double getTemperature(double temperature, BlockPos pos, World level)
    {
        for (TempRegion region : temperatures)
        {
            if (region.withinBounds(level, pos))
            {   return region.getTemperature(temperature, pos, level);
            }
        }
        return null;
    }

    public static class TempRegion
    {
        public static final Codec<TempRegion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RampType.CODEC.optionalFieldOf("type", RampType.LINEAR).forGetter(region -> region.rampType),
                VerticalBound.CODEC.fieldOf("top").forGetter(region -> region.top),
                VerticalBound.CODEC.fieldOf("bottom").forGetter(region -> region.bottom)
        ).apply(instance, (type, top, bottom) ->
        {
            if (type == RampType.CONSTANT && top.temperature != bottom.temperature)
            {   throw new IllegalArgumentException("Constant temperature ramp type must have a single temperature value");
            }
            return new TempRegion(type, top, bottom);
        }));

        RampType rampType;
        VerticalBound top;
        VerticalBound bottom;

        public TempRegion(RampType rampType, VerticalBound top, VerticalBound bottom)
        {
            this.rampType = rampType;
            this.top = top;
            this.bottom = bottom;
        }

        public boolean withinBounds(World level, BlockPos pos)
        {
            return pos.getY() <= top.getHeight(pos, level)
                && pos.getY() >= bottom.getHeight(pos, level);
        }

        public double getTemperature(double temperature, BlockPos pos, World level)
        {
            double topTemp = Temperature.convert(top.getTemperature(temperature), top.units, Temperature.Units.MC, true);
            double bottomTemp = Temperature.convert(bottom.getTemperature(temperature), bottom.units, Temperature.Units.MC, true);
            switch (rampType)
            {
                case CONSTANT : return pos.getY() <= bottom.getHeight(pos, level) ? bottomTemp : topTemp;
                case LINEAR : return CSMath.blend(bottomTemp,
                                                  topTemp,
                                                  pos.getY(),
                                                  bottom.getHeight(pos, level),
                                                  top.getHeight(pos, level));
                case EXPONENTIAL : return CSMath.blendExp(bottomTemp,
                                                          topTemp,
                                                          pos.getY(),
                                                          bottom.getHeight(pos, level),
                                                          top.getHeight(pos, level));
                case LOGARITHMIC : return CSMath.blendLog(bottomTemp,
                                                          topTemp,
                                                          pos.getY(),
                                                          bottom.getHeight(pos, level),
                                                          top.getHeight(pos, level));
            }
            return topTemp;
        }
    }

    public static class VerticalBound
    {
        public static final TempContainer DEFAULT_PASSTHROUGH = new TempContainer(0, ContainerType.PASSTHROUGH, 1);
        public static final TempContainer DEFAULT_MIDPOINT = new TempContainer(0, ContainerType.MIDPOINT, 1);

        public static final Codec<VerticalBound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                VerticalAnchor.CODEC.optionalFieldOf("anchor", VerticalAnchor.CONSTANT).forGetter(bound -> bound.anchor),
                Codec.INT.fieldOf("depth").forGetter(bound -> bound.depth),
                Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(bound -> bound.units),
                TempContainer.CODEC.fieldOf("temperature").forGetter(bound -> bound.temperature)
        ).apply(instance, VerticalBound::new));

        public final VerticalAnchor anchor;
        public final int depth;
        public final Temperature.Units units;
        public final TempContainer temperature;

        public VerticalBound(VerticalAnchor anchor, Integer depth, Temperature.Units units, TempContainer temperature)
        {
            this.anchor = anchor;
            this.depth = depth;
            this.units = units;
            this.temperature = temperature;
        }

        public static class TempContainer
        {
            public static final Codec<TempContainer> CODEC = Codec.either(Codec.DOUBLE, Codec.STRING).flatXmap(
            either ->
            {
                if (either.left().isPresent())
                {   return DataResult.success(new TempContainer(either.left().get(), ContainerType.NONE, 1));
                }
                else
                {
                    String[] value = either.right().get().split(",");
                    double strength = value.length == 2 ? Double.parseDouble(value[1]) : 1;
                    if (value[0].equals("passthrough"))
                    {   return DataResult.success(new TempContainer(0, ContainerType.PASSTHROUGH, strength));
                    }
                    else if (value[0].equals("midpoint"))
                    {   return DataResult.success(new TempContainer(0, ContainerType.MIDPOINT, strength));
                    }
                    else
                    {   return DataResult.error("Unknown temperature value: " + value);
                    }
                }
            },
            value ->
            {
                String strength = value.type == ContainerType.NONE ? "" : "," + value.strength;
                if (value.type == ContainerType.PASSTHROUGH)
                {   return DataResult.success(Either.right("passthrough" + strength));
                }
                else if (value.type == ContainerType.MIDPOINT)
                {   return DataResult.success(Either.right("midpoint" + strength));
                }
                else return DataResult.success(Either.left(value.temperature));
            });

            public final double temperature;
            public final ContainerType type;
            public final double strength;

            public TempContainer(double temperature, ContainerType type, double strength)
            {
                this.temperature = temperature;
                this.type = type;
                this.strength = strength;
            }
        }

        public int getHeight(BlockPos checkPos, World level)
        {
            switch (anchor)
            {
                case CONSTANT : return depth;
                case WORLD_TOP : return level.getMaxBuildHeight();
                case WORLD_BOTTOM : return 0;
                case GROUND_LEVEL : return WorldHelper.getHeight(checkPos, level) + depth;
            }
            return depth;
        }

        public double getTemperature(double temperature)
        {
            switch (this.temperature.type)
            {
                case NONE : return this.temperature.temperature;
                case PASSTHROUGH : return temperature;
                case MIDPOINT : return  (ConfigSettings.MIN_TEMP.get() + ConfigSettings.MAX_TEMP.get()) / 2;
            }
            return this.temperature.temperature;
        }

        public enum ContainerType implements StringRepresentable
        {
            NONE("none"),
            PASSTHROUGH("passthrough"),
            MIDPOINT("midpoint");

            private final String name;

            ContainerType(String name)
            {   this.name = name;
            }

            public static final Codec<ContainerType> CODEC = Codec.STRING.xmap(ContainerType::byName, ContainerType::getSerializedName);

            @Override
            public String getSerializedName()
            {   return name;
            }

            public static ContainerType byName(String name)
            {   for (ContainerType type : values())
                {   if (type.name.equals(name))
                    {   return type;
                    }
                }
                throw new IllegalArgumentException("Unknown special temperature value: " + name);
            }
        }
    }

    public enum RampType implements StringRepresentable
    {
        CONSTANT("constant"),
        LINEAR("linear"),
        EXPONENTIAL("exponential"),
        LOGARITHMIC("logarithmic");

        private final String name;

        RampType(String name)
        {   this.name = name;
        }

        public static final Codec<RampType> CODEC = Codec.STRING.xmap(RampType::byName, RampType::getSerializedName);

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static RampType byName(String name)
        {   for (RampType type : values())
            {   if (type.name.equals(name))
                {   return type;
                }
            }
            throw new IllegalArgumentException("Unknown ramp type: " + name);
        }
    }

    public enum VerticalAnchor implements StringRepresentable
    {
        CONSTANT("constant"),
        WORLD_TOP("world_top"),
        WORLD_BOTTOM("world_bottom"),
        GROUND_LEVEL("ground_level");

        private final String name;

        VerticalAnchor(String name)
        {   this.name = name;
        }

        public static final Codec<VerticalAnchor> CODEC = Codec.STRING.xmap(VerticalAnchor::byName, VerticalAnchor::getSerializedName);

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static VerticalAnchor byName(String name)
        {   for (VerticalAnchor type : values())
            {   if (type.name.equals(name))
                {   return type;
                }
            }
            throw new IllegalArgumentException("Unknown vertical anchor: " + name);
        }
    }

    @Override
    public DepthTempData setRegistryName(ResourceLocation name)
    {   return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName()
    {   return null;
    }

    @Override
    public Class<DepthTempData> getRegistryType()
    {   return DepthTempData.class;
    }
}
