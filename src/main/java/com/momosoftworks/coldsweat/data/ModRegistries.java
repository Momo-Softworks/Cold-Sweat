package com.momosoftworks.coldsweat.data;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.configuration.data.BiomeTempData;
import com.momosoftworks.coldsweat.data.configuration.data.BlockTempData;
import com.momosoftworks.coldsweat.data.configuration.data.DimensionTempData;
import com.momosoftworks.coldsweat.data.configuration.data.InsulatorData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistries
{
    public static final ResourceKey<Registry<InsulatorData>> INSULATOR_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "item/insulator"));
    public static final ResourceKey<Registry<BlockTempData>> BLOCK_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "block/block_temp"));
    public static final ResourceKey<Registry<BiomeTempData>> BIOME_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "world/biome_temp"));
    public static final ResourceKey<Registry<DimensionTempData>> DIMENSION_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "world/dimension_temp"));
}
