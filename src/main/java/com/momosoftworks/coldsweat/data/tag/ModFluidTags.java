package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ModFluidTags
{
    public static final TagKey<Fluid> HOT = createTag("hot");
    public static final TagKey<Fluid> COLD = createTag("cold");
    public static final TagKey<Fluid> SLUSH = createTag("slush");

    private static TagKey<Fluid> createTag(String name)
    {   return FluidTags.create(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, name));
    }
}
