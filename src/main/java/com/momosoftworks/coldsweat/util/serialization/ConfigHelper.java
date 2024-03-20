package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.util.ItemData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import oshi.util.tuples.Triplet;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
                CSMath.doIfNotNull(ForgeRegistries.BLOCKS.tags(), tags ->
                {   Optional<ITag<Block>> optionalTag = tags.stream().filter(tag -> tag.getKey() != null && tag.getKey().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(blockITag -> blocks.addAll(blockITag.stream().toList()));
                });
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
            String id = (String) entry.get(0);

            if (id.startsWith("#"))
            {
                final String tagID = id.replace("#", "");
                CSMath.doIfNotNull(ForgeRegistries.BLOCKS.tags(), tags ->
                {
                    Optional<ITag<Block>> optionalTag = tags.stream().filter(tag -> tag.getKey() != null && tag.getKey().location().toString().equals(tagID)).findFirst();

                    optionalTag.ifPresent(itemITag ->
                    {   for (Block block : optionalTag.get().stream().toList())
                        {   map.put(block, (Number) entry.get(1));
                        }
                    });
                });
            }
            else
            {   Block newBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
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
                CSMath.doIfNotNull(ForgeRegistries.ITEMS.tags(), tags ->
                {   Optional<ITag<Item>> optionalTag = tags.stream().filter(tag -> tag.getKey() != null && tag.getKey().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(itemITag -> items.addAll(itemITag.stream().toList()));
                });
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
            CompoundTag nbt = entry.size() > 2 ? NBTHelper.parseCompoundNbt((String) entry.get(2)) : new CompoundTag();

            if (itemId.startsWith("#"))
            {
                final String tagID = itemId.replace("#", "");
                CSMath.doIfNotNull(ForgeRegistries.ITEMS.tags(), tags ->
                {
                    Optional<ITag<Item>> optionalTag = tags.stream().filter(tag -> tag.getKey() != null && tag.getKey().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(itemITag ->
                    {   for (Item item : optionalTag.get().stream().toList())
                        {
                            map.put(new ItemData(item, nbt), ((Number) entry.get(1)).doubleValue());
                        }
                    });
                });
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
    {   return ids.stream().map(id -> WorldHelper.getBiome(new ResourceLocation(id))).toList();
    }

    public static CompoundTag serializeNbtBool(boolean value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, value);
        return tag;
    }

    public static CompoundTag serializeNbtInt(int value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt(key, value);
        return tag;
    }

    public static CompoundTag serializeNbtDouble(double value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(key, value);
        return tag;
    }

    public static CompoundTag serializeNbtString(String value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString(key, value);
        return tag;
    }

    public static CompoundTag serializeDimensionTemps(Map<ResourceLocation, Pair<Double, Temperature.Units>> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Pair<Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundTag biomeTag = new CompoundTag();
            biomeTag.putDouble("Temp", entry.getValue().getFirst());
            biomeTag.putString("Units", entry.getValue().getSecond().toString());
            mapTag.put(entry.getKey().toString(), biomeTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ResourceLocation, Pair<Double, Temperature.Units>> deserializeDimensionTemps(CompoundTag tag, String key)
    {
        Map<ResourceLocation, Pair<Double, Temperature.Units>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String biomeID : mapTag.getAllKeys())
        {
            CompoundTag biomeTag = mapTag.getCompound(biomeID);
            map.put(new ResourceLocation(biomeID), Pair.of(biomeTag.getDouble("Temp"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static CompoundTag serializeBiomeTemps(Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Triplet<Double, Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundTag biomeTag = new CompoundTag();
            biomeTag.putDouble("Min", entry.getValue().getA());
            biomeTag.putDouble("Max", entry.getValue().getB());
            biomeTag.putString("Units", entry.getValue().getC().toString());
            mapTag.put(entry.getKey().toString(), biomeTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> deserializeBiomeTemps(CompoundTag tag, String key)
    {
        Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String biomeID : mapTag.getAllKeys())
        {
            CompoundTag biomeTag = mapTag.getCompound(biomeID);
            map.put(new ResourceLocation(biomeID), new Triplet<>(biomeTag.getDouble("Min"), biomeTag.getDouble("Max"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static CompoundTag serializeItemMap(Map<ItemData, Double> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<ItemData, Double> entry : map.entrySet())
        {
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("Item", entry.getKey().save(new CompoundTag()));
            itemTag.putDouble("Value", entry.getValue());
            mapTag.put(ForgeRegistries.ITEMS.getKey(entry.getKey().getItem()).toString(), itemTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ItemData, Double> deserializeItemMap(CompoundTag tag, String key)
    {
        Map<ItemData, Double> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String itemID : mapTag.getAllKeys())
        {
            CompoundTag itemTag = mapTag.getCompound(itemID);
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
            CompoundTag nbt = entry.size() > 2 ? NBTHelper.parseCompoundNbt(((String) entry.get(2))) : new CompoundTag();
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
            ItemData itemData = entry.getKey();
            if (itemData.getPredicate() != null) continue;
            List<Object> item = new ArrayList<>();
            item.add(ForgeRegistries.ITEMS.getKey(itemData.getItem()).toString());
            item.add(entry.getValue());
            if (!itemData.getTag().isEmpty())
            {   item.add(itemData.getTag().toString());
            }
            list.add(item);
        }
        saver.accept(list);
    }

    public static CompoundTag serializeItemInsulations(Map<ItemData, Insulation> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<ItemData, Insulation> entry : map.entrySet())
        {
            CompoundTag itemTag = new CompoundTag();
            ItemData stack = entry.getKey();
            itemTag.put("Item", stack.save(new CompoundTag()));
            itemTag.putString("Type", entry.getValue() instanceof StaticInsulation ? "static" : "adaptive");

            if (entry.getValue() instanceof StaticInsulation insulation)
            {   itemTag.putDouble("Value1", insulation.getCold());
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

    public static Map<ItemData, Insulation> deserializeItemInsulations(CompoundTag tag, String key)
    {
        Map<ItemData, Insulation> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String itemID : mapTag.getAllKeys())
        {
            CompoundTag itemTag = mapTag.getCompound(itemID);
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
            CompoundTag nbt = entry.size() > 4 ? NBTHelper.parseCompoundNbt(((String) entry.get(4))) : new CompoundTag();

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
            ItemData itemData = entry.getKey();
            List<Object> item = new ArrayList<>();
            item.add(ForgeRegistries.ITEMS.getKey(itemData.getItem()).toString());

            if (entry.getValue() instanceof StaticInsulation insulation)
            {   item.add(insulation.getCold());
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

            if (!itemData.getTag().isEmpty())
            {   item.add(itemData.getTag().toString());
            }

            list.add(item);
        }
        saver.accept(list);
    }
}
