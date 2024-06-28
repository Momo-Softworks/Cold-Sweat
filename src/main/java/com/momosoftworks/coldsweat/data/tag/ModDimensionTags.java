package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimensionTags
{
    public static final TagKey<DimensionType> SOUL_LAMP_VALID = createTag("soulspring_lamp_valid");

    private static TagKey<DimensionType> createTag(String name)
    {
        return TagKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, name));
    }
}
