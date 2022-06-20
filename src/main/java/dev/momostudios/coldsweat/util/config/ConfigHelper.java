package dev.momostudios.coldsweat.util.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigHelper
{
    public static List<Block> getBlocks(String id)
    {
        if (id.startsWith("#"))
        {
            final String tagID = id.replace("#", "");
            Optional<ITag<Block>> optionalTag = ForgeRegistries.BLOCKS.tags().stream().filter(tag ->
                                                tag.getKey().location().toString().equals(tagID)).findFirst();
            if (optionalTag.isPresent())
            {
                return optionalTag.get().stream().toList();
            }
        }
        return List.of(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id)));
    }

    public static Map<Block, Number> getBlocksWithValues(List<? extends List<?>> source)
    {
        Map<Block, Number> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String blockID = (String) entry.get(0);

            map.put(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockID)), (Number) entry.get(1));
        }
        return map;
    }

    public static List<Item> getItems(String id)
    {
        if (id.startsWith("#"))
        {
            final String tagID = id.replace("#", "");
            Optional<ITag<Item>> optionalTag = ForgeRegistries.ITEMS.tags().stream().filter(tag ->
                                               tag.getKey().location().toString().equals(tagID)).findFirst();
            if (optionalTag.isPresent())
            {
                return optionalTag.get().stream().toList();
            }
        }
        return List.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)));
    }

    public static Map<Item, Number> getItemsWithValues(List<? extends List<?>> source)
    {
        Map<Item, Number> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String itemID = (String) entry.get(0);

            map.put(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID)), (Number) entry.get(1));
        }
        return map;
    }

    public static List<Biome> getBiomes(String id)
    {
        if (id.startsWith("#"))
        {
            final String tagID = id.replace("#", "");
            Optional<ITag<Biome>> optionalTag = ForgeRegistries.BIOMES.tags().stream().filter(tag ->
                                                tag.getKey().location().toString().equals(tagID)).findFirst();
            if (optionalTag.isPresent())
            {
                return optionalTag.get().stream().toList();
            }
        }
        return List.of(ForgeRegistries.BIOMES.getValue(new ResourceLocation(id)));
    }

    public static Map<Biome, Number> getBiomesWithValues(List<? extends List<?>> source)
    {
        Map<Biome, Number> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String biomeID = (String) entry.get(0);

            map.put(ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeID)), (Number) entry.get(1));
        }
        return map;
    }
}
