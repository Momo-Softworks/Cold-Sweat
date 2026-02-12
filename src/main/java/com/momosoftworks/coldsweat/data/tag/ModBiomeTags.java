package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomeTags
{
    private static TagKey<Biome> createTag(String name)
    {   return TagKey.create(Registries.BIOME, new ResourceLocation(ColdSweat.MOD_ID, name));
    }

    private static TagKey<Biome> createForgeTag(String name)
    {   return TagKey.create(Registries.BIOME, new ResourceLocation("forge", name));
    }
}