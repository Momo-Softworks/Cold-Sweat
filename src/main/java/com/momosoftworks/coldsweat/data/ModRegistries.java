package com.momosoftworks.coldsweat.data;

import com.momosoftworks.coldsweat.data.configuration.data.BiomeTempData;
import com.momosoftworks.coldsweat.data.configuration.data.BlockTempData;
import com.momosoftworks.coldsweat.data.configuration.data.DimensionTempData;
import com.momosoftworks.coldsweat.data.configuration.data.InsulatorData;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRegistries
{
    public static IForgeRegistry<InsulatorData> INSULATOR_DATA = null;
    public static IForgeRegistry<BlockTempData> BLOCK_TEMP_DATA = null;
    public static IForgeRegistry<BiomeTempData> BIOME_TEMP_DATA = null;
    public static IForgeRegistry<DimensionTempData> DIMENSION_TEMP_DATA = null;
}
