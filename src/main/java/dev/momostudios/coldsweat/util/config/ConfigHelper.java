package dev.momostudios.coldsweat.util.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.*;

public class ConfigHelper
{
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
            blocks.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id)));
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
            items.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)));
        }
        return items;
    }


    public static Map<Item, Number> getItemsWithValues(List<? extends List<?>> source)
    {
        Map<Item, Number> map = new HashMap<>();
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
                        map.put(item, (Number) entry.get(1));
                    }
                });
            }
            else
            {
                Item newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID));

                if (newItem != null) map.put(newItem, (Number) entry.get(1));
            }
        }
        return map;
    }

    public static List<Biome> getBiomes(String... ids)
    {
        List<Biome> blocks = new ArrayList<>();
        for (String id : ids)
        {
            if (id.startsWith("#"))
            {
                final String tagID = id.replace("#", "");
                Optional<ITag<Biome>> optionalTag = ForgeRegistries.BIOMES.tags().stream().filter(tag ->
                                                    tag.getKey().location().toString().equals(tagID)).findFirst();
                optionalTag.ifPresent(blockITag -> blocks.addAll(blockITag.stream().toList()));
            }
            blocks.add(ForgeRegistries.BIOMES.getValue(new ResourceLocation(id)));
        }
        return blocks;
    }

    public static Map<ResourceLocation, Number> getBiomesWithValues(List<? extends List<?>> source)
    {
        Map<ResourceLocation, Number> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String biomeID = (String) entry.get(0);

            if (biomeID.startsWith("#"))
            {
                final String tagID = biomeID.replace("#", "");
                Optional<ITag<Biome>> optionalTag = ForgeRegistries.BIOMES.tags().stream().filter(tag -> tag.getKey().location().toString().equals(tagID)).findFirst();

                if (optionalTag.isPresent())
                {
                    for (Biome biome : optionalTag.get().stream().toList())
                    {
                        map.put(biome.getRegistryName(), (Number) entry.get(1));
                    }
                }
            }
            else if (ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeID)) != null)
            {
                map.put(new ResourceLocation(biomeID), (Number) entry.get(1));
            }
        }
        return map;
    }
}
