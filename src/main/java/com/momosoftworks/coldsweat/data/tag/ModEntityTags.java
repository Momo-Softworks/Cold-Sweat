package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class ModEntityTags
{
    // Entities in this tag need more accurate temperature information
    public static final TagKey<EntityType<?>> TEMPERATURE_SENSITIVE = createTag("temperature_sensitive");

    private static TagKey<EntityType<?>> createTag(String name)
    {   return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(ColdSweat.MOD_ID, name));
    }
}
