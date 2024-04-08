package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class ModItemTags
{
    public static final ITag.INamedTag<Item> BOILER_VALID = createTag("boiler_valid");
    public static final ITag.INamedTag<Item> ICEBOX_VALID = createTag("icebox_valid");
    public static final ITag.INamedTag<Item> CHAMELEON_TAMING = createTag("chameleon/taming");
    public static final ITag.INamedTag<Item> CHAMELEON_HOT = createTag("chameleon/find_hot_biomes");
    public static final ITag.INamedTag<Item> CHAMELEON_COLD = createTag("chameleon/find_cold_biomes");
    public static final ITag.INamedTag<Item> CHAMELEON_HUMID = createTag("chameleon/find_humid_biomes");

    private static ITag.INamedTag<Item> createTag(String name)
    {   return ItemTags.bind(new ResourceLocation(ColdSweat.MOD_ID, name).toString());
    }
}
