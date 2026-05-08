package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemTempData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ModItemTempTags
{
    public static final TagKey<ItemTempData> DRAINS_WATERSKIN = createTag("drains_waterskin");

    private static TagKey<ItemTempData> createTag(String name)
    {   return TagKey.create(ModRegistries.ITEM_TEMP_DATA.key(), ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, name));
    }
}
