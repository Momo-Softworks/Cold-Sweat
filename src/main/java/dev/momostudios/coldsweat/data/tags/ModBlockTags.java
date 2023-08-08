package dev.momostudios.coldsweat.data.tags;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public class ModBlockTags
{
    public static final ITag.INamedTag<Block> CAMPFIRES = createTag("campfires");
    public static final ITag.INamedTag<Block> SOUL_CAMPFIRES = createTag("soul_campfires");
    public static final ITag.INamedTag<Block> SOUL_STALK_PLACEABLE_ON = createTag("may_place_on/soul_stalk");

    private static ITag.INamedTag<Block> createTag(String name)
    {   return BlockTags.bind(new ResourceLocation(ColdSweat.MOD_ID, name).toString());
    }
}
