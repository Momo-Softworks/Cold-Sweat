package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public class ModFluidTags
{
    public static final ITag<Fluid> HOT = createTag("hot");
    public static final ITag<Fluid> COLD = createTag("cold");
    public static final ITag<Fluid> SLUSH = createTag("slush");

    private static ITag<Fluid> createTag(String name)
    {   return FluidTags.bind(new ResourceLocation(ColdSweat.MOD_ID, name).toString());
    }
}
