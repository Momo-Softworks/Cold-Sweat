package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConfigHelper
{
    private ConfigHelper() {}

    public static List<Block> getBlocks(String... ids)
    {
        List<Block> blocks = new ArrayList<>();
        for (String id : ids)
        {
            if (id.startsWith("#"))
            {
                final String tagID = id.replace("#", "");
                ITag<Block> blockTag = BlockTags.getAllTags().getTag(new ResourceLocation(tagID));
                if (blockTag != null)
                {   blocks.addAll(blockTag.getValues());
                }
            }
            else
            {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
                if (block != Blocks.AIR)
                    blocks.add(block);
            }
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
                ITag<Block> blockTag = BlockTags.getAllTags().getTag(new ResourceLocation(tagID));
                if (blockTag != null)
                {
                    for (Block block : blockTag.getValues())
                    {   map.put(block, (Number) entry.get(1));
                    }
                }
            }
            else
            {   Block newBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockID));
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
                ITag<Item> itemTag = ItemTags.getAllTags().getTag(new ResourceLocation(tagID));
                if (itemTag != null)
                {   items.addAll(itemTag.getValues());
                }
            }
            else
            {   Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
                if (item != Items.AIR)
                    items.add(item);
            }
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
                ITag<Item> itemTag = ItemTags.getAllTags().getTag(new ResourceLocation(tagID));
                if (itemTag != null)
                {
                    for (Item item : itemTag.getValues())
                    {   map.put(item, ((Number) entry.get(1)).doubleValue());
                    }
                }
            }
            else
            {
                Item newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID));

                if (newItem != null) map.put(newItem, ((Number) entry.get(1)).doubleValue());
            }
        }
        return map;
    }

    public static Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> getBiomesWithValues(List<? extends List<?>> source, boolean absolute)
    {
        Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                ResourceLocation biomeID = new ResourceLocation((String) entry.get(0));
                Biome biome = WorldHelper.getBiome(biomeID);

                double min;
                double max;
                Temperature.Units units;
                // The config defines a min and max value, with optional unit conversion
                if (entry.size() > 2)
                {   units = entry.size() == 4 ? Temperature.Units.valueOf(((String) entry.get(3)).toUpperCase()) : Temperature.Units.MC;
                    min = Temperature.convertUnits(((Number) entry.get(1)).doubleValue(), units, Temperature.Units.MC, absolute);
                    max = Temperature.convertUnits(((Number) entry.get(2)).doubleValue(), units, Temperature.Units.MC, absolute);
                }
                // The config only defines a mid-temperature
                else
                {   double mid = ((Number) entry.get(1)).doubleValue();
                    double variance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
                    min = mid - variance;
                    max = mid + variance;
                    units = Temperature.Units.MC;
                }

                // Maps the biome ID to the temperature (and variance if present)
                map.put(biomeID, new Triplet<>(min, max, units));
            }
            catch (Exception e)
            {   ColdSweat.LOGGER.error("Error parsing biome temp config: " + entry.toString() + ". The biome may not be loaded yet or the mod is not present.");
            }
        }
        return map;
    }

    public static Map<ResourceLocation, Pair<Double, Temperature.Units>> getDimensionsWithValues(List<? extends List<?>> source, boolean absolute)
    {
        Map<ResourceLocation, Pair<Double, Temperature.Units>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                ResourceLocation dimensionID = new ResourceLocation((String) entry.get(0));
                double temp = ((Number) entry.get(1)).doubleValue();
                Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(((String) entry.get(2)).toUpperCase()) : Temperature.Units.MC;
                map.put(dimensionID, Pair.of(Temperature.convertUnits(temp, units, Temperature.Units.MC, absolute), units));
            }
            catch (Exception e)
            {   ColdSweat.LOGGER.error("Error parsing dimension temp config: " + entry.toString() + ".");
            }
        }
        return map;
    }

    public static Map<String, Predicate<BlockState>> getBlockStatePredicates(Block block, String predicates)
    {
        Map<String, Predicate<BlockState>> blockPredicates = new HashMap<>();
        // Separate comma-delineated predicates
        String[] predicateList = predicates.split(",");

        // Iterate predicates
        for (String predicate : predicateList)
        {
            // Split predicate into key-value pairs separated by "="
            String[] pair = predicate.split("=");
            String key = pair[0];
            String value = pair[1];

            // Get the property with the given name
            Property<?> property = block.getStateDefinition().getProperty(key);
            if (property != null)
            {
                // Parse the desired value for this property
                property.getValue(value).ifPresent(propertyValue ->
                {
                    // Add a new predicate to the list
                    blockPredicates.put(predicate, state ->
                    {   // If the value matches, this predicate returns true
                        return state.getValue(property).equals(propertyValue);
                    });
                });
            }
        }
        return blockPredicates;
    }

    public static List<Biome> getBiomes(List<? extends String> ids)
    {   return ids.stream().map(id -> WorldHelper.getBiome(new ResourceLocation(id))).collect(Collectors.toList());
    }

    public static CompoundNBT writeNBTBoolean(boolean value, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean(key, value);
        return tag;
    }

    public static CompoundNBT writeNBTInt(int value, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(key, value);
        return tag;
    }

    public static CompoundNBT writeNBTDouble(double value, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble(key, value);
        return tag;
    }

    public static CompoundNBT writeNBTString(String value, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(key, value);
        return tag;
    }

    public static CompoundNBT writeDimensionTemps(Map<ResourceLocation, Pair<Double, Temperature.Units>> map, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT mapTag = new CompoundNBT();
        for (Map.Entry<ResourceLocation, Pair<Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundNBT biomeTag = new CompoundNBT();
            biomeTag.putDouble("Temp", entry.getValue().getFirst());
            biomeTag.putString("Units", entry.getValue().getSecond().toString());
            mapTag.put(entry.getKey().toString(), biomeTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ResourceLocation, Pair<Double, Temperature.Units>> readDimensionTemps(CompoundNBT tag, String key)
    {
        Map<ResourceLocation, Pair<Double, Temperature.Units>> map = new HashMap<>();
        CompoundNBT mapTag = tag.getCompound(key);
        for (String biomeID : mapTag.getAllKeys())
        {
            CompoundNBT biomeTag = mapTag.getCompound(biomeID);
            map.put(new ResourceLocation(biomeID), Pair.of(biomeTag.getDouble("Temp"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static CompoundNBT writeBiomeTemps(Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> map, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT mapTag = new CompoundNBT();
        for (Map.Entry<ResourceLocation, Triplet<Double, Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundNBT biomeTag = new CompoundNBT();
            biomeTag.putDouble("Min", entry.getValue().getFirst());
            biomeTag.putDouble("Max", entry.getValue().getSecond());
            biomeTag.putString("Units", entry.getValue().getThird().toString());
            mapTag.put(entry.getKey().toString(), biomeTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> readBiomeTemps(CompoundNBT tag, String key)
    {
        Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();
        CompoundNBT mapTag = tag.getCompound(key);
        for (String biomeID : mapTag.getAllKeys())
        {
            CompoundNBT biomeTag = mapTag.getCompound(biomeID);
            map.put(new ResourceLocation(biomeID), new Triplet<>(biomeTag.getDouble("Min"), biomeTag.getDouble("Max"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static CompoundNBT writeNBTDoubleMap(Map<ResourceLocation, Double> map, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT mapTag = new CompoundNBT();
        for (Map.Entry<ResourceLocation, Double> entry : map.entrySet())
        {
            mapTag.putDouble(entry.getKey().toString(), entry.getValue());
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ResourceLocation, Double> readNBTDoubleMap(CompoundNBT tag, String key)
    {
        Map<ResourceLocation, Double> map = new HashMap<>();
        CompoundNBT mapTag = tag.getCompound(key);
        for (String biomeID : mapTag.getAllKeys())
        {
            map.put(new ResourceLocation(biomeID), mapTag.getDouble(biomeID));
        }
        return map;
    }

    public static CompoundNBT writeNBTItemMap(Map<Item, Pair<Double, Double>> map, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT mapTag = new CompoundNBT();
        for (Map.Entry<Item, Pair<Double, Double>> entry : map.entrySet())
        {
            CompoundNBT itemTag = new CompoundNBT();
            itemTag.putDouble("Value1", entry.getValue().getFirst());
            itemTag.putDouble("Value2", entry.getValue().getSecond());
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(entry.getKey());
            if (itemID != null)
                mapTag.put(itemID.toString(), itemTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<Item, Pair<Double, Double>> readNBTItemMap(CompoundNBT tag, String key)
    {
        Map<Item, Pair<Double, Double>> map = new HashMap<>();
        CompoundNBT mapTag = tag.getCompound(key);
        for (String itemID : mapTag.getAllKeys())
        {
            CompoundNBT itemTag = mapTag.getCompound(itemID);
            map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID)), Pair.of(itemTag.getDouble("Value1"), itemTag.getDouble("Value2")));
        }
        return map;
    }
}
