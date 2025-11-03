package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.*;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class BlockTempData extends ConfigData
{
    final NegatableList<BlockRequirement> block;
    final double temperature;
    final double range;
    final double maxEffect;
    final boolean fade;
    final WorldTempRequirement maxTemp;
    final WorldTempRequirement minTemp;
    final Temperature.Units units;
    final NegatableList<LocationRequirement> location;
    final NegatableList<EntityRequirement> entity;
    final boolean logarithmic;
    final Optional<TagKey<BlockTempData>> effectGroup;

    public BlockTempData(NegatableList<BlockRequirement> block, double temperature, double range,
                         double maxEffect, boolean fade, WorldTempRequirement maxTemp, WorldTempRequirement minTemp,
                         Temperature.Units units, NegatableList<LocationRequirement> location,
                         NegatableList<EntityRequirement> entity, boolean logarithmic, Optional<TagKey<BlockTempData>> effectGroup, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.block = block;
        this.temperature = temperature;
        this.range = range;
        this.maxEffect = maxEffect;
        this.fade = fade;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.units = units;
        this.location = location;
        this.entity = entity;
        this.logarithmic = logarithmic;
        this.effectGroup = effectGroup;
    }

    public BlockTempData(NegatableList<BlockRequirement> block, double temperature, double range,
                         double maxEffect, boolean fade, WorldTempRequirement maxTemp, WorldTempRequirement minTemp,
                         Temperature.Units units, NegatableList<LocationRequirement> location, NegatableList<EntityRequirement> entity,
                         boolean logarithmic, Optional<TagKey<BlockTempData>> effectGroup)
    {
        this(block, temperature, range, maxEffect, fade, maxTemp, minTemp, units, location, entity, logarithmic, effectGroup, new NegatableList<>());
    }

    /**
     * Creates a BlockTempData from a Java BlockTemp.<br>
     * <br>
     * !! The resulting BlockTempData <b>WILL NOT</b> have a temperature, as it is defined solely in the BlockTemp's getTemperature() method.
     */
    public BlockTempData(BlockTemp blockTemp)
    {
        this(new NegatableList<>(new BlockRequirement(blockTemp.getAffectedBlocks().stream().map(Either::<TagKey<Block>, Block>right).toList())),
             0, blockTemp.range(), blockTemp.maxEffect(),
             true, new WorldTempRequirement(blockTemp.maxTemperature()), new WorldTempRequirement(blockTemp.minTemperature()), Temperature.Units.MC,
             new NegatableList<>(), new NegatableList<>(), blockTemp.logarithmic(), Optional.empty());
    }

    public static final Codec<BlockTempData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(BlockRequirement.CODEC).fieldOf("block").forGetter(BlockTempData::block),
            Codec.DOUBLE.fieldOf("temperature").forGetter(BlockTempData::temperature),
            Codec.DOUBLE.optionalFieldOf("range", Double.POSITIVE_INFINITY).forGetter(BlockTempData::range),
            Codec.DOUBLE.optionalFieldOf("max_effect", Double.POSITIVE_INFINITY).forGetter(BlockTempData::maxEffect),
            Codec.BOOL.optionalFieldOf("fade", true).forGetter(BlockTempData::fade),
            WorldTempRequirement.CODEC.optionalFieldOf("max_temp", WorldTempRequirement.INFINITY).forGetter(BlockTempData::maxTemp),
            WorldTempRequirement.CODEC.optionalFieldOf("min_temp", WorldTempRequirement.NEGATIVE_INFINITY).forGetter(BlockTempData::minTemp),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(BlockTempData::units),
            NegatableList.codec(LocationRequirement.CODEC).optionalFieldOf("location", new NegatableList<>()).forGetter(BlockTempData::location),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(BlockTempData::entity),
            Codec.BOOL.optionalFieldOf("logarithmic", false).forGetter(BlockTempData::logarithmic),
            ExtraCodecs.deferred(() -> TagKey.codec(ModRegistries.BLOCK_TEMP_DATA.key())).optionalFieldOf("effect_group").forGetter(BlockTempData::effectGroup)
    ).apply(instance, BlockTempData::new)));

    public NegatableList<BlockRequirement> block()
    {   return block;
    }
    public double temperature()
    {   return temperature;
    }
    public double range()
    {   return range;
    }
    public double maxEffect()
    {   return maxEffect;
    }
    public boolean fade()
    {   return fade;
    }
    public WorldTempRequirement maxTemp()
    {   return maxTemp;
    }
    public WorldTempRequirement minTemp()
    {   return minTemp;
    }
    public Temperature.Units units()
    {   return units;
    }
    public NegatableList<LocationRequirement> location()
    {   return location;
    }
    public NegatableList<EntityRequirement> entity()
    {   return entity;
    }
    public boolean logarithmic()
    {   return logarithmic;
    }
    public Optional<TagKey<BlockTempData>> effectGroup()
    {   return effectGroup;
    }

    public double getTemperature()
    {   return Temperature.convert(temperature, units, Temperature.Units.MC, false);
    }
    public double getMaxEffect()
    {   return Temperature.convert(maxEffect, units, Temperature.Units.MC, false);
    }
    public double getMaxTemp()
    {   return maxTemp.get(this.units);
    }
    public double getMinTemp()
    {   return minTemp.get(this.units);
    }

    @Nullable
    public static BlockTempData fromToml(List<?> entry)
    {
        if (entry.size() < 3)
        {   ColdSweat.LOGGER.error("Error parsing block config: not enough arguments");
            return null;
        }
        NegatableList<Either<TagKey<Block>, Block>> blocks = ConfigHelper.getBlocks((String) entry.get(0));
        if (blocks.isEmpty()) return null;

        // Parse block IDs into blocks
        Block[] effectBlocks = RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.BLOCKS, blocks).toArray(new Block[0]);

        // Temp of block
        final double blockTemp = ((Number) entry.get(1)).doubleValue();
        // Range of effect
        final double blockRange = ((Number) entry.get(2)).doubleValue();

        final Temperature.Units units = entry.size() > 3 && entry.get(3) instanceof String
                                         ? Temperature.Units.fromID((String) entry.get(3))
                                         : Temperature.Units.MC;

        // Get min/max effect
        final double maxEffect = entry.size() > 4 && entry.get(4) instanceof Number
                                 ? ((Number) entry.get(4)).doubleValue()
                                 : Double.POSITIVE_INFINITY;

        // Get block predicate
        BlockRequirement.StateRequirement blockPredicates = entry.size() > 5 && entry.get(5) instanceof String str && !str.isBlank()
                                                            ? BlockRequirement.StateRequirement.fromToml(str.split(","), effectBlocks[0])
                                                            : BlockRequirement.StateRequirement.NONE;

        NbtRequirement nbtRequirement = entry.size() > 6 && entry.get(6) instanceof String str && !str.isBlank()
                                        ? new NbtRequirement(NBTHelper.parseCompoundNbt(str))
                                        : NbtRequirement.NONE;

        double tempLimit = entry.size() > 7
                           ? ((Number) entry.get(7)).doubleValue()
                           : Double.POSITIVE_INFINITY;

        boolean logarithmic = entry.size() > 8 && entry.get(8) instanceof Boolean
                              ? (Boolean) entry.get(8)
                              : false;

        double maxTemperature = blockTemp > 0 ? tempLimit : Double.POSITIVE_INFINITY;
        double minTemperature = blockTemp < 0 ? tempLimit : Double.NEGATIVE_INFINITY;

        BlockRequirement blockRequirement = new BlockRequirement(blocks, blockPredicates, nbtRequirement, List.of(), Optional.empty());

        return new BlockTempData(new NegatableList<>(blockRequirement), blockTemp, blockRange, maxEffect, true,
                                 new WorldTempRequirement(maxTemperature), new WorldTempRequirement(minTemperature),
                                 units, new NegatableList<>(), new NegatableList<>(), logarithmic, Optional.empty());
    }

    @Override
    public Codec<BlockTempData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BlockTempData that = (BlockTempData) obj;
        return super.equals(obj)
            && Double.compare(that.temperature, temperature) == 0
            && Double.compare(that.range, range) == 0
            && Double.compare(that.maxEffect, maxEffect) == 0
            && maxTemp.equals(that.maxTemp)
            && minTemp.equals(that.minTemp)
            && fade == that.fade
            && block.equals(that.block);
    }
}
