package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public class ModEntityTags
{
    // Entities in this tag need more accurate temperature information
    public static final ITag<EntityType<?>> TEMPERATURE_SENSITIVE = createTag("temperature_sensitive");

    private static ITag<EntityType<?>> createTag(String name)
    {   return EntityTypeTags.bind(new ResourceLocation(ColdSweat.MOD_ID, name).toString());
    }
}
