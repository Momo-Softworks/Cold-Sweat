package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public class ModBlockTags
{
    public static final ITag.INamedTag<Block> CAMPFIRES = createTag("campfires");
    public static final ITag.INamedTag<Block> SOUL_CAMPFIRES = createTag("soul_campfires");
    public static final ITag.INamedTag<Block> SOUL_STALK_PLACEABLE_ON = createTag("may_place_on/soul_stalk");
    public static final ITag.INamedTag<Block> HEARTH_WHITELIST = createTag("hearth/spread_whitelist");
    public static final ITag.INamedTag<Block> HEARTH_BLACKLIST = createTag("hearth/spread_blacklist");
    public static final ITag.INamedTag<Block> IGNORE_SLEEP_CHECK = createTag("ignore_sleep_check");
    public static final ITag.INamedTag<Block> SOUL_SAND_REPLACEABLE = createTag("soul_sand_replaceable");

    private static ITag.INamedTag<Block> createTag(String name)
    {   return BlockTags.bind(new ResourceLocation(ColdSweat.MOD_ID, name).toString());
    }
}
