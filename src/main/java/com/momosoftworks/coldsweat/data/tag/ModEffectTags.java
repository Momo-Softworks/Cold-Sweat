package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.block.Block;
import net.minecraft.potion.Effect;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class ModEffectTags
{
    public static final ITag.INamedTag<Effect> HEARTH_BLACKLISTED = createTag("hearth_blacklisted");

    private static ITag.INamedTag<Effect> createTag(String name)
    {
        return new ITag.INamedTag<Effect>()
        {
            @Override
            public ResourceLocation getName()
            {
                return new ResourceLocation(name);
            }

            @Override
            public boolean contains(Effect pElement)
            {
                return false;
            }

            @Override
            public List<Effect> getValues()
            {
                return Collections.emptyList();
            }
        };
    }
}
