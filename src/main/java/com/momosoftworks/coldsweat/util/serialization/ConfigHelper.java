package com.momosoftworks.coldsweat.util.serialization;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.Pair;
import com.momosoftworks.coldsweat.util.math.Triplet;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigHelper
{
    private ConfigHelper() {}

    public static Optional<Block> getBlock(String id)
    {   return parseID(id).map(blockID -> GameRegistry.findBlock(blockID.getResourceDomain(), blockID.getResourcePath()));
    }

    public static Optional<Item> getItem(String id)
    {   return parseID(id).map(itemID -> GameRegistry.findItem(itemID.getResourceDomain(), itemID.getResourcePath()));
    }

    public static Optional<ResourceLocation> getBlockID(Block block)
    {   GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(block);
        return Optional.of(new ResourceLocation(uid.modId, uid.name));
    }

    public static Optional<ResourceLocation> getItemID(Item item)
    {   GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item);
        return Optional.of(new ResourceLocation(uid.modId, uid.name));
    }

    public static List<Block> getBlocks(String source)
    {
        List<Block> blocks = new ArrayList<>();
        for (String stringID : source.replace(" ", "").split(","))
        {   getBlock(stringID).ifPresent(blocks::add);
        }
        return blocks;
    }

    public static Map<Block, Number> getBlocksWithValues(String source)
    {
        List<?> parsed = deserializeList(source);

        Map<Block, Number> map = new HashMap<>();
        for (Object entry : parsed)
        {
            if (entry instanceof List<?> && ((List<?>) entry).size() == 2)
            {   List<?> listEntry = (List<?>) entry;
                getBlock((String) listEntry.get(0)).ifPresent(block -> map.put(block, Double.valueOf((String) listEntry.get(1))));
            }
        }
        return map;
    }

    public static List<Item> getItems(String source)
    {
        List<Item> items = new ArrayList<>();
        for (String stringID : source.replace(" ", "").split(","))
        {   getItem(stringID).ifPresent(items::add);
        }
        return items;
    }


    public static Map<Item, Double> getItemsWithValues(String source)
    {
        List<?> parsed = deserializeList(source);

        Map<Item, Double> map = new HashMap<>();
        for (Object entry : parsed)
        {
            if (entry instanceof List<?> && ((List<?>) entry).size() == 2)
            {   List<?> listEntry = (List<?>) entry;
                getItem((String) listEntry.get(0)).ifPresent(item -> map.put(item, Double.valueOf((String) listEntry.get(1))));
            }
        }
        return map;
    }

    public static Map<Integer, Triplet<Double, Double, Temperature.Units>> getBiomesWithValues(String source, boolean absolute)
    {
        Map<Integer, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();

        List<?> parsed = deserializeList(source);
        for (Object entry : parsed)
        {
            if (entry instanceof List<?> && ((List<?>) entry).size() >= 2)
            {
                List<?> listEntry = (List<?>) entry;
                try
                {
                    List<Integer> biomeIDs = new ArrayList<>();
                    // Try to parse an integer from the biome ID string
                    try
                    {   biomeIDs.add(Integer.parseInt((String) listEntry.get(0)));
                    }
                    // Try to match the string against the biome registry
                    catch (Exception e)
                    {   biomeIDs.addAll(Arrays.stream(BiomeGenBase.getBiomeGenArray()).filter(biome -> biome != null && biome.biomeName.equalsIgnoreCase(((String) listEntry.get(0))))
                                                                                      .map(biome -> biome.biomeID).collect(Collectors.toList()));
                    }

                    if (biomeIDs.isEmpty())
                    {   ColdSweat.LOGGER.error("Error parsing biome temperature config entry: " + listEntry + ". Biome not found");
                        continue;
                    }
                    for (Integer biomeID : biomeIDs)
                    {
                        BiomeGenBase biome = BiomeGenBase.getBiome(biomeID);

                        double min;
                        double max;
                        Temperature.Units units;
                        // The config defines a min and max value, with optional unit conversion
                        if (listEntry.size() > 2)
                        {   units = listEntry.size() == 4 ? Temperature.Units.valueOf(((String) listEntry.get(3)).toUpperCase()) : Temperature.Units.MC;
                            min = Temperature.convertUnits(Double.parseDouble(((String) listEntry.get(1))), units, Temperature.Units.MC, absolute);
                            max = Temperature.convertUnits(Double.parseDouble(((String) listEntry.get(2))), units, Temperature.Units.MC, absolute);
                        }
                        // The config only defines a mid-temperature
                        else
                        {   double mid = ((Number) listEntry.get(1)).doubleValue();
                            double variance = 1 / Math.max(1, 2 + biome.rainfall * 2);
                            min = mid - variance;
                            max = mid + variance;
                            units = Temperature.Units.MC;
                        }

                        // Maps the biome ID to the temperature (and variance if present)
                        map.put(biomeID, new Triplet<>(min, max, units));
                    }
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Error parsing biome temperature config entry: " + listEntry, e);
                }
            }
        }
        return map;
    }

    public static Map<Integer, Pair<Double, Temperature.Units>> getDimensionsWithValues(String source)
    {
        Map<Integer, Pair<Double, Temperature.Units>> map = new HashMap<>();
        List<?> parsed = deserializeList(source);
        for (Object obj : parsed)
        {
            if (!(obj instanceof List<?>)) continue;
            List<?> entry = (List<?>) obj;
            try
            {
                Integer dimensionID = Integer.parseInt((String) entry.get(0));
                double temp = Double.parseDouble((String) entry.get(1));
                Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(stripString(entry.get(2)).toUpperCase()) : Temperature.Units.MC;
                map.put(dimensionID, Pair.of(temp, units));
            }
            catch (Exception e)
            {   ColdSweat.LOGGER.error("Error parsing dimension temp config: " + entry + ".", e);
            }
        }
        return map;
    }

    public static List<BiomeGenBase> getBiomes(String source)
    {   List<?> parsed = deserializeList(source);
        return parsed.stream().map(entry -> BiomeGenBase.getBiome(Integer.parseInt((String) entry))).collect(Collectors.toList());
    }

    public static NBTTagCompound writeNBTBoolean(boolean value, String key)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(key, value);
        return tag;
    }

    public static NBTTagCompound writeNBTInt(int value, String key)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(key, value);
        return tag;
    }

    public static NBTTagCompound writeNBTDouble(double value, String key)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble(key, value);
        return tag;
    }

    public static NBTTagCompound writeNBTString(String value, String key)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(key, value);
        return tag;
    }

    public static NBTTagCompound writeDimensionTemps(Map<Integer, Pair<Double, Temperature.Units>> map, String key)
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound mapTag = new NBTTagCompound();
        for (Map.Entry<Integer, Pair<Double, Temperature.Units>> entry : map.entrySet())
        {
            NBTTagCompound biomeTag = new NBTTagCompound();
            biomeTag.setDouble("Temp", entry.getValue().getFirst());
            biomeTag.setString("Units", entry.getValue().getSecond().toString());
            mapTag.setTag(entry.getKey().toString(), biomeTag);
        }
        tag.setTag(key, mapTag);
        return tag;
    }

    public static Map<Integer, Pair<Double, Temperature.Units>> readDimensionTemps(NBTTagCompound tag, String key)
    {
        Map<Integer, Pair<Double, Temperature.Units>> map = new HashMap<>();
        NBTTagCompound mapTag = tag.getCompoundTag(key);
        for (Object obj : mapTag.func_150296_c())
        {
            String biomeID = (String) obj;
            NBTTagCompound biomeTag = mapTag.getCompoundTag(biomeID);
            map.put(Integer.parseInt(biomeID), Pair.of(biomeTag.getDouble("Temp"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static NBTTagCompound writeBiomeTemps(Map<Integer, Triplet<Double, Double, Temperature.Units>> map, String key)
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound mapTag = new NBTTagCompound();
        for (Map.Entry<Integer, Triplet<Double, Double, Temperature.Units>> entry : map.entrySet())
        {
            NBTTagCompound biomeTag = new NBTTagCompound();
            biomeTag.setDouble("Min", entry.getValue().getFirst());
            biomeTag.setDouble("Max", entry.getValue().getSecond());
            biomeTag.setString("Units", entry.getValue().getThird().toString());
            mapTag.setTag(entry.getKey().toString(), biomeTag);
        }
        tag.setTag(key, mapTag);
        return tag;
    }

    public static Map<Integer, Triplet<Double, Double, Temperature.Units>> readBiomeTemps(NBTTagCompound tag, String key)
    {
        Map<Integer, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();
        NBTTagCompound mapTag = tag.getCompoundTag(key);
        for (Object obj : mapTag.func_150296_c())
        {
            String biomeID = (String) obj;
            NBTTagCompound biomeTag = mapTag.getCompoundTag(biomeID);
            map.put(Integer.parseInt(biomeID), new Triplet<>(biomeTag.getDouble("Min"), biomeTag.getDouble("Max"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static NBTTagCompound writeNBTDoubleMap(Map<ResourceLocation, Double> map, String key)
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound mapTag = new NBTTagCompound();
        for (Map.Entry<ResourceLocation, Double> entry : map.entrySet())
        {
            mapTag.setDouble(entry.getKey().toString(), entry.getValue());
        }
        tag.setTag(key, mapTag);
        return tag;
    }

    public static Map<ResourceLocation, Double> readNBTDoubleMap(NBTTagCompound tag, String key)
    {
        Map<ResourceLocation, Double> map = new HashMap<>();
        NBTTagCompound mapTag = tag.getCompoundTag(key);
        for (Object obj : mapTag.func_150296_c())
        {
            String biomeID = (String) obj;
            map.put(new ResourceLocation(biomeID), mapTag.getDouble(biomeID));
        }
        return map;
    }

    public static NBTTagCompound writeNBTItemMap(Map<Item, Pair<Double, Double>> map, String key)
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound mapTag = new NBTTagCompound();
        for (Map.Entry<Item, Pair<Double, Double>> entry : map.entrySet())
        {
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setDouble("Value1", entry.getValue().getFirst());
            itemTag.setDouble("Value2", entry.getValue().getSecond());

            getItemID(entry.getKey()).ifPresent(itemID -> mapTag.setTag(itemID.toString(), itemTag));
        }
        tag.setTag(key, mapTag);
        return tag;
    }

    public static Map<Item, Pair<Double, Double>> readNBTItemMap(NBTTagCompound tag, String key)
    {
        Map<Item, Pair<Double, Double>> map = new HashMap<>();
        NBTTagCompound mapTag = tag.getCompoundTag(key);
        for (Object obj : mapTag.func_150296_c())
        {
            String itemID = (String) obj;
            NBTTagCompound itemTag = mapTag.getCompoundTag(itemID);
            getItem(itemID).ifPresent(item -> map.put(item, Pair.of(itemTag.getDouble("Value1"), itemTag.getDouble("Value2"))));
        }
        return map;
    }

    public static Optional<ResourceLocation> parseID(String id)
    {   String[] parsed = id.split(":");
        if (parsed.length != 2)
        {   ColdSweat.LOGGER.error("Error parsing ID: " + id + ".");
            return Optional.empty();
        }
        return Optional.of(new ResourceLocation(parsed[0], parsed[1]));
    }

    public static List<?> deserializeList(String source)
    {
        // Remove characters that will mess up the parsing
        String fixed = source.replace(", ", ",").replace("\"", "");

        // recursively parse a list from the "source" string
        // Format: [[a, b, c], a, b, c] where "[]" represent a list and "a, b, c" represent list elements
        // Preserve list nesting
        List<Object> list = new ArrayList<>();
        int index = 0;
        while (index < fixed.length())
        {
            int endIndex = index;
            if (fixed.charAt(index) == '[')
            {
                // Find the end of the list
                int depth = 0;
                do
                {   if (fixed.charAt(endIndex) == '[') depth++;
                    else if (fixed.charAt(endIndex) == ']') depth--;
                    endIndex++;
                }
                while (depth > 0);
                // Parse the contents of the list
                list.add(deserializeList(fixed.substring(index + 1, endIndex - 1)));
            }
            else
            {
                // Find the end of the element
                do
                {   endIndex++;
                }
                while (endIndex < fixed.length() && fixed.charAt(endIndex) != ',');
                // Parse the element
                list.add(fixed.substring(index, endIndex));
            }
            index = endIndex + 1;
        }
        if (list.size() == 1 && list.get(0) instanceof List)
        {   return (List<?>) list.get(0);
        }
        return list;
    }

    public static String[] deserializeArray(String source)
    {
        List<?> list = deserializeList(source);
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
        {   Object obj = list.get(i);
            if (obj instanceof String)
            {   array[i] = stripString(obj);
            }
        }
        return array;
    }

    public static String serializeList(List<?> list)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Object obj : list)
        {
            if (obj instanceof List<?>)
                builder.append(serializeList((List<?>) obj));
            else
                builder.append(obj.toString());
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }

    public static String serializeArray(Object[] array)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Object obj : array)
        {
            if (obj instanceof Object[])
                builder.append(serializeArray((Object[]) obj));
            else
                builder.append(obj.toString());
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }

    public static String stripString(Object object)
    {   return object.toString().trim();
    }

    public static ResourceLocation getResourceLocation(GameRegistry.UniqueIdentifier uid)
    {   return new ResourceLocation(uid.modId, uid.name);
    }
}
