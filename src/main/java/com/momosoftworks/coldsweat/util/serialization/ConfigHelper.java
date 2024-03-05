package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.util.ItemData;
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
import java.util.function.Consumer;
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
        for (String itemId : ids)
        {
            if (itemId.startsWith("#"))
            {
                final String tagID = itemId.replace("#", "");
                ITag<Item> itemTag = ItemTags.getAllTags().getTag(new ResourceLocation(tagID));
                if (itemTag != null)
                {   items.addAll(itemTag.getValues());
                }
            }
            else
            {   Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                if (item != null && item != Items.AIR)
                {   items.add(item);
                }
                else ColdSweat.LOGGER.error("Error parsing item config: item \"" + itemId + "\" does not exist");
            }
        }
        return items;
    }

    public static Map<ItemData, Double> getItemsWithValues(List<? extends List<?>> source)
    {
        Map<ItemData, Double> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String itemId = (String) entry.get(0);
            CompoundNBT nbt = entry.size() > 2 ? NBTHelper.parseCompoundNbt((String) entry.get(2)) : new CompoundNBT();

            if (itemId.startsWith("#"))
            {
                final String tagID = itemId.replace("#", "");
                ITag<Item> itemTag = ItemTags.getAllTags().getTag(new ResourceLocation(tagID));
                if (itemTag != null)
                {
                    for (Item item : itemTag.getValues())
                    {
                        map.put(new ItemData(item, nbt), ((Number) entry.get(1)).doubleValue());
                    }
                }
            }
            else
            {   Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                if (item != null && item != Items.AIR)
                {   map.put(new ItemData(item, nbt), ((Number) entry.get(1)).doubleValue());
                }
                else ColdSweat.LOGGER.error("Error parsing item config: item \"" + itemId + "\" does not exist");
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
            {   ColdSweat.LOGGER.error("Error parsing biome config: biome \"" + entry.toString() + "\" does not exist or is not loaded yet.");
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
            {   ColdSweat.LOGGER.error("Error parsing dimension config: dimension \"" + entry.toString() + "\" does not exist or is not loaded yet");
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

    public static CompoundNBT serializeNbtBool(boolean value, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean(key, value);
        return tag;
    }

    public static CompoundNBT serializeNbtInt(int value, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(key, value);
        return tag;
    }

    public static CompoundNBT serializeNbtDouble(double value, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble(key, value);
        return tag;
    }

    public static CompoundNBT serializeNbtString(String value, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(key, value);
        return tag;
    }

    public static CompoundNBT serializeDimensionTemps(Map<ResourceLocation, Pair<Double, Temperature.Units>> map, String key)
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

    public static Map<ResourceLocation, Pair<Double, Temperature.Units>> deserializeDimensionTemps(CompoundNBT tag, String key)
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

    public static CompoundNBT serializeBiomeTemps(Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> map, String key)
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

    public static Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> deserializeBiomeTemps(CompoundNBT tag, String key)
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

    public static CompoundNBT serializeItemMap(Map<ItemData, Double> map, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT mapTag = new CompoundNBT();
        for (Map.Entry<ItemData, Double> entry : map.entrySet())
        {
            CompoundNBT itemTag = new CompoundNBT();
            itemTag.put("Item", entry.getKey().save(new CompoundNBT()));
            itemTag.putDouble("Value", entry.getValue());
            mapTag.put(ForgeRegistries.ITEMS.getKey(entry.getKey().getItem()).toString(), itemTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ItemData, Double> deserializeItemMap(CompoundNBT tag, String key)
    {
        Map<ItemData, Double> map = new HashMap<>();
        CompoundNBT mapTag = tag.getCompound(key);
        for (String itemID : mapTag.getAllKeys())
        {
            CompoundNBT itemTag = mapTag.getCompound(itemID);
            ItemData stack = ItemData.load(itemTag.getCompound("Item"));
            double value = itemTag.getDouble("Value");
            map.put(stack, value);
        }
        return map;
    }

    public static Map<ItemData, Double> readItemMap(List<? extends List<?>> items)
    {
        Map<ItemData, Double> map = new HashMap<>();
        for (List<?> entry : items)
        {
            String itemID = (String) entry.get(0);
            double value = ((Number) entry.get(1)).doubleValue();
            CompoundNBT nbt = entry.size() > 2 ? NBTHelper.parseCompoundNbt(((String) entry.get(2))) : new CompoundNBT();
            for (Item item : ConfigHelper.getItems(itemID))
            {   map.put(new ItemData(item, nbt), value);
            }
        }
        return map;
    }

    public static void writeItemMap(Map<ItemData, Double> items, Consumer<List<? extends List<?>>> saver)
    {
        List<List<?>> list = new ArrayList<>();
        for (Map.Entry<ItemData, Double> entry : items.entrySet())
        {
            ItemData stack = entry.getKey();
            List<Object> item = new ArrayList<>();
            item.add(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
            item.add(entry.getValue());
            if (!stack.getTag().isEmpty())
            {   item.add(stack.getTag().toString());
            }
            list.add(item);
        }
        saver.accept(list);
    }

    public static CompoundNBT serializeItemInsulations(Map<ItemData, Insulation> map, String key)
    {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT mapTag = new CompoundNBT();
        for (Map.Entry<ItemData, Insulation> entry : map.entrySet())
        {
            CompoundNBT itemTag = new CompoundNBT();
            ItemData stack = entry.getKey();
            itemTag.put("Item", stack.save(new CompoundNBT()));
            itemTag.putString("Type", entry.getValue() instanceof StaticInsulation ? "static" : "adaptive");

            if (entry.getValue() instanceof StaticInsulation)
            {
                StaticInsulation insulation = ((StaticInsulation) entry.getValue());
                itemTag.putDouble("Value1", insulation.getCold());
                itemTag.putDouble("Value2", insulation.getHot());
            }
            else
            {   AdaptiveInsulation insulation = (AdaptiveInsulation) entry.getValue();
                itemTag.putDouble("Value1", insulation.getInsulation());
                itemTag.putDouble("Value2", insulation.getSpeed());
            }
            mapTag.put(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(), itemTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ItemData, Insulation> deserializeItemInsulations(CompoundNBT tag, String key)
    {
        Map<ItemData, Insulation> map = new HashMap<>();
        CompoundNBT mapTag = tag.getCompound(key);
        for (String itemID : mapTag.getAllKeys())
        {
            CompoundNBT itemTag = mapTag.getCompound(itemID);
            ItemData stack = ItemData.load(itemTag.getCompound("Item"));
            double value1 = itemTag.getDouble("Value1");
            double value2 = itemTag.getDouble("Value2");

            if (itemTag.getString("Type").equals("static"))
            {   map.put(stack, new StaticInsulation(value1, value2));
            }
            else
            {   map.put(stack, new AdaptiveInsulation(value1, value2));
            }
        }
        return map;
    }

    public static Map<ItemData, Insulation> readItemInsulations(List<? extends List<?>> items)
    {
        Map<ItemData, Insulation> map = new HashMap<>();
        for (List<?> entry : items)
        {
            String itemID = (String) entry.get(0);
            double value1 = ((Number) entry.get(1)).doubleValue();
            double value2 = ((Number) entry.get(2)).doubleValue();
            String type = entry.size() > 3 ? (String) entry.get(3) : "static";
            CompoundNBT nbt = entry.size() > 4 ? NBTHelper.parseCompoundNbt(((String) entry.get(4))) : new CompoundNBT();

            for (Item item : ConfigHelper.getItems(itemID))
            {
                ItemData data = new ItemData(item, nbt);
                if (type.equals("static"))
                {   map.put(data, new StaticInsulation(value1, value2));
                }
                else if (type.equals("adaptive"))
                {   map.put(data, new AdaptiveInsulation(value1, value2));
                }
                else ColdSweat.LOGGER.error("Error parsing item insulation \"" + itemID + "\": invalid insulation type \"" + type + "\"");
            }
        }
        return map;
    }

    public static void writeItemInsulations(Map<ItemData, Insulation> items, Consumer<List<? extends List<?>>> saver)
    {
        List<List<?>> list = new ArrayList<>();
        for (Map.Entry<ItemData, Insulation> entry : items.entrySet())
        {
            ItemData stack = entry.getKey();
            List<Object> item = new ArrayList<>();
            item.add(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());

            if (entry.getValue() instanceof StaticInsulation)
            {
                StaticInsulation insulation = ((StaticInsulation) entry.getValue());
                item.add(insulation.getCold());
                item.add(insulation.getHot());
            }
            else
            {   AdaptiveInsulation insulation = (AdaptiveInsulation) entry.getValue();
                item.add(insulation.getInsulation());
                item.add(insulation.getSpeed());
            }
            if (entry.getValue() instanceof AdaptiveInsulation)
            {   item.add("adaptive");
            }

            if (!stack.getTag().isEmpty())
            {   item.add(stack.getTag().toString());
            }

            list.add(item);
        }
        saver.accept(list);
    }
}
