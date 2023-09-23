package dev.momosoftworks.coldsweat.data.tags;

import dev.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimensionTags
{
    public static final TagKey<DimensionType> SOUL_LAMP_VALID = createTag("soulspring_lamp_valid");

    private static TagKey<DimensionType> createTag(String name)
    {
        return TagKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(ColdSweat.MOD_ID, name));
    }
}
