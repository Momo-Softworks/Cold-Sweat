package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags
{
    public static final TagKey<Item> BOILER_VALID = createTag("boiler_valid");
    public static final TagKey<Item> ICEBOX_VALID = createTag("icebox_valid");

    public static final TagKey<Item> NOT_INSULATABLE = createTag("not_insulatable");

    public static final TagKey<Item> CHAMELEON_TAMING = createTag("chameleon/taming");
    public static final TagKey<Item> CHAMELEON_HOT = createTag("chameleon/find_hot_biomes");
    public static final TagKey<Item> CHAMELEON_COLD = createTag("chameleon/find_cold_biomes");
    public static final TagKey<Item> CHAMELEON_HUMID = createTag("chameleon/find_humid_biomes");

    private static TagKey<Item> createTag(String name)
    {   return ItemTags.create(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, name));
    }
}
