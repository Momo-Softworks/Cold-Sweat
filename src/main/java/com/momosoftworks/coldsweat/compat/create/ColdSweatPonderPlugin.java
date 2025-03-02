package com.momosoftworks.coldsweat.compat.create;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class ColdSweatPonderPlugin implements PonderPlugin
{
    @Override
    public String getModId()
    {   return ColdSweat.MOD_ID;
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper)
    {
        helper.addToTag(AllCreatePonderTags.DISPLAY_SOURCES).add(ModBlocks.THERMOLITH.getId());
    }
}
