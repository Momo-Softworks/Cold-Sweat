package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomeTags
{
    private static TagKey<Biome> createTag(String name)
    {   return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, name));
    }

    private static TagKey<Biome> createForgeTag(String name)
    {   return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", name));
    }
}