package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomeTags
{
    public static final TagKey<Biome> HAS_HOT_WATER = createForgeTag("has_hot_water");

    private static TagKey<Biome> createTag(String name)
    {   return TagKey.create(Registry.BIOME_REGISTRY, ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, name));
    }

    private static TagKey<Biome> createForgeTag(String name)
    {   return TagKey.create(Registry.BIOME_REGISTRY, ResourceLocation.fromNamespaceAndPath("forge", name));
    }
}