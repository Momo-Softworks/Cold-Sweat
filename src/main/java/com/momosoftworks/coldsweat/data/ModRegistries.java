package com.momosoftworks.coldsweat.data;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.configuration.Insulator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistries
{
    public static final ResourceKey<Registry<Insulator>> INSULATOR = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "insulator"));
}
