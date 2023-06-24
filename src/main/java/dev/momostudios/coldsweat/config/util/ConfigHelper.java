package dev.momostudios.coldsweat.config.util;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import oshi.util.tuples.Triplet;

import java.util.*;

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

    public static Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> getBiomesWithValues(List<? extends List<?>> source, boolean absolute)
    {
        Map<ResourceLocation, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                String biomeID = (String) entry.get(0);

                Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeID));
                if (biome == null) continue;

                double min;
                double max;
                Temperature.Units units;
                // The config defines a min and max value, with optional unit conversion
                if (entry.size() > 2)
                {
                    units = entry.size() == 4 ? Temperature.Units.valueOf(((String) entry.get(3)).toUpperCase()) : Temperature.Units.MC;
                    min = CSMath.convertTemp(((Number) entry.get(1)).doubleValue(), units, Temperature.Units.MC, absolute);
                    max = CSMath.convertTemp(((Number) entry.get(2)).doubleValue(), units, Temperature.Units.MC, absolute);
                }
                // The config only defines a mid-temperature
                else
                {
                    double mid = ((Number) entry.get(1)).doubleValue();
                    double variance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
                    min = mid - variance;
                    max = mid + variance;
                    units = Temperature.Units.MC;
                }

                // Maps the biome ID to the temperature (and variance if present)
                map.put(ForgeRegistries.BIOMES.getKey(biome), new Triplet<>(min, max, units));
            }
            catch (Exception ignored) {}
        }
        return map;
    }

    public static List<Biome> getBiomes(List<? extends String> ids)
    {   return ids.stream().map(id -> ForgeRegistries.BIOMES.getValue(new ResourceLocation(id))).toList();
    }

    public static CompoundTag writeNBTBoolean(boolean value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, value);
        return tag;
    }

    public static CompoundTag writeNBTInt(int value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt(key, value);
        return tag;
    }

    public static CompoundTag writeNBTDouble(double value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(key, value);
        return tag;
    }

    public static CompoundTag writeNBTString(String value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString(key, value);
        return tag;
    }

    public static CompoundTag writeNBTPairMap(Map<ResourceLocation, Pair<Double, Double>> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Pair<Double, Double>> entry : map.entrySet())
        {
            CompoundTag biomeTag = new CompoundTag();
            biomeTag.putDouble("min", entry.getValue().getFirst());
            biomeTag.putDouble("max", entry.getValue().getSecond());
            mapTag.put(entry.getKey().toString(), biomeTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ResourceLocation, Pair<Double, Double>> readNBTPairMap(CompoundTag tag, String key)
    {
        Map<ResourceLocation, Pair<Double, Double>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String biomeID : mapTag.getAllKeys())
        {
            CompoundTag biomeTag = mapTag.getCompound(biomeID);
            map.put(new ResourceLocation(biomeID), Pair.of(biomeTag.getDouble("min"), biomeTag.getDouble("max")));
        }
        return map;
    }

    public static CompoundTag writeNBTDoubleMap(Map<ResourceLocation, Double> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Double> entry : map.entrySet())
        {
            mapTag.putDouble(entry.getKey().toString(), entry.getValue());
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<ResourceLocation, Double> readNBTDoubleMap(CompoundTag tag, String key)
    {
        Map<ResourceLocation, Double> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String biomeID : mapTag.getAllKeys())
        {
            map.put(new ResourceLocation(biomeID), mapTag.getDouble(biomeID));
        }
        return map;
    }

    public static CompoundTag writeNBTItemMap(Map<Item, Pair<Double, Double>> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<Item, Pair<Double, Double>> entry : map.entrySet())
        {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putDouble("value1", entry.getValue().getFirst());
            itemTag.putDouble("value2", entry.getValue().getSecond());
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(entry.getKey());
            if (itemID != null)
                mapTag.put(itemID.toString(), itemTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<Item, Pair<Double, Double>> readNBTItemMap(CompoundTag tag, String key)
    {
        Map<Item, Pair<Double, Double>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String itemID : mapTag.getAllKeys())
        {
            CompoundTag itemTag = mapTag.getCompound(itemID);
            map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID)), Pair.of(itemTag.getDouble("value1"), itemTag.getDouble("value2")));
        }
        return map;
    }
}
