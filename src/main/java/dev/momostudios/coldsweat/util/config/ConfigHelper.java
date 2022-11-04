package dev.momostudios.coldsweat.util.config;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.*;

public class ConfigHelper
{
    public static CompoundTag writeConfigSettingsToNBT(ConfigSettings config)
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("difficulty", config.difficulty);
        tag.putDouble("minTemp", config.minTemp);
        tag.putDouble("maxTemp", config.maxTemp);
        tag.putDouble("rate", config.rate);
        tag.putBoolean("fireResistance", config.fireRes);
        tag.putBoolean("iceResistance", config.iceRes);
        tag.putBoolean("damageScaling", config.damageScaling);
        tag.putBoolean("requireThermometer", config.requireThermometer);
        tag.putInt("graceLength", config.graceLength);
        tag.putBoolean("graceEnabled", config.graceEnabled);
        return tag;
    }

    public static ConfigSettings readConfigSettingsFromNBT(CompoundTag tag)
    {
        ConfigSettings config = new ConfigSettings();
        if (tag == null)
        {
            ColdSweat.LOGGER.error("Failed to read config settings!");
            return config;
        }

        config.difficulty = tag.getInt("difficulty");
        config.minTemp = tag.getDouble("minTemp");
        config.maxTemp = tag.getDouble("maxTemp");
        config.rate = tag.getDouble("rate");
        config.fireRes = tag.getBoolean("fireResistance");
        config.iceRes = tag.getBoolean("iceResistance");
        config.damageScaling = tag.getBoolean("damageScaling");
        config.requireThermometer = tag.getBoolean("requireThermometer");
        config.graceLength = tag.getInt("graceLength");
        config.graceEnabled = tag.getBoolean("graceEnabled");
        return config;
    }

    public static List<Block> getBlocks(String... ids)
    {
        List<Block> blocks = new ArrayList<>();
        for (String id : ids)
        {
            if (id.startsWith("#"))
            {
                final String tagID = id.replace("#", "");
                Optional<ITag<Block>> optionalTag = ForgeRegistries.BLOCKS.tags().stream().filter(tag ->
                                                    tag.getKey().location().toString().equals(tagID)).findFirst();
                optionalTag.ifPresent(blockITag -> blocks.addAll(blockITag.stream().toList()));
            }
            else blocks.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id)));
        }
        return blocks;
    }

    public static Map<Block, Number> getBlocksWithValues(List<? extends List<?>> source)
    {
        Map<Block, Number> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String blockID = (String) entry.get(0);

            if (blockID.startsWith("#"))
            {
                final String tagID = blockID.replace("#", "");
                Optional<ITag<Block>> optionalTag = ForgeRegistries.BLOCKS.tags().stream().filter(tag ->
                                                    tag.getKey().location().toString().equals(tagID)).findFirst();
                optionalTag.ifPresent(blockITag ->
                {
                    for (Block block : optionalTag.get().stream().toList())
                    {
                        map.put(block, (Number) entry.get(1));
                    }
                });
            }
            else
            {
                Block newBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockID));

                if (newBlock != null) map.put(newBlock, (Number) entry.get(1));
            }
        }
        return map;
    }

    public static List<Item> getItems(String... ids)
    {
        List<Item> items = new ArrayList<>();
        for (String id : ids)
        {
            if (id.startsWith("#"))
            {
                final String tagID = id.replace("#", "");
                Optional<ITag<Item>> optionalTag = ForgeRegistries.ITEMS.tags().stream().filter(tag ->
                        tag.getKey().location().toString().equals(tagID)).findFirst();
                optionalTag.ifPresent(itemITag -> items.addAll(itemITag.stream().toList()));
            }
            else items.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)));
        }
        return items;
    }


    public static Map<Item, Double> getItemsWithValues(List<? extends List<?>> source)
    {
        Map<Item, Double> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String itemID = (String) entry.get(0);

            if (itemID.startsWith("#"))
            {
                final String tagID = itemID.replace("#", "");
                Optional<ITag<Item>> optionalTag = ForgeRegistries.ITEMS.tags().stream().filter(tag ->
                                                   tag.getKey().location().toString().equals(tagID)).findFirst();
                optionalTag.ifPresent(itemITag ->
                {
                    for (Item item : optionalTag.get().stream().toList())
                    {
                        map.put(item, ((Number) entry.get(1)).doubleValue());
                    }
                });
            }
            else
            {
                Item newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID));

                if (newItem != null) map.put(newItem, ((Number) entry.get(1)).doubleValue());
            }
        }
        return map;
    }

    public static Map<ResourceLocation, Pair<Double, Double>> getBiomesWithValues(List<? extends List<?>> source, boolean absolute)
    {
        Map<ResourceLocation, Pair<Double, Double>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                String biomeID = (String) entry.get(0);

                Collection<Biome> biomes;
                if (biomeID.startsWith("#"))
                {
                    Optional<ITag<Biome>> tagBiomes = ForgeRegistries.BIOMES.tags().stream().filter(tag -> tag.getKey().location().toString().equals(biomeID.replace("#", ""))).findFirst();
                    biomes = tagBiomes.map(biomeITag -> biomeITag.stream().toList()).orElse(Collections.emptyList());
                }
                else biomes = Collections.singletonList(ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeID)));

                // Iterate through all biomes
                for (Biome biome : biomes)
                {
                    double min;
                    double max;
                    // The config defines a min and max value, with optional unit conversion
                    if (entry.size() > 2)
                    {
                        Temperature.Units units = entry.size() == 4 ? Temperature.Units.valueOf(((String) entry.get(3)).toUpperCase()) : Temperature.Units.MC;
                        min = CSMath.convertUnits(((Number) entry.get(1)).doubleValue(), units, Temperature.Units.MC, absolute);
                        max = CSMath.convertUnits(((Number) entry.get(2)).doubleValue(), units, Temperature.Units.MC, absolute);
                    }
                    // The config only defines a mid-temperature
                    else
                    {
                        double mid = ((Number) entry.get(1)).doubleValue();
                        double variance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
                        min = mid - variance;
                        max = mid + variance;
                    }

                    // Maps the biome ID to the temperature (and variance if present)
                    map.put(biome.getRegistryName(), Pair.of(min, max));
                }
            } catch (Exception ignored) {}
        }
        return map;
    }
}
