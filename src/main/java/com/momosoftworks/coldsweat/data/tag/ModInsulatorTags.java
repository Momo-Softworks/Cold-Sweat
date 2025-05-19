package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ModInsulatorTags
{
    public static final TagKey<InsulatorData> DRAINS_BACKTANK = createTag("drains_backtank");

    private static TagKey<InsulatorData> createTag(String name)
    {   TagKey<InsulatorData> tag = TagKey.create(ModRegistries.INSULATOR_DATA, new ResourceLocation(ColdSweat.MOD_ID, name));
        return tag;
    }
}
