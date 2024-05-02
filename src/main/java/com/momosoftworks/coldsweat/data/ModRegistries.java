package com.momosoftworks.coldsweat.data;

import com.momosoftworks.coldsweat.data.configuration.SpawnBiomeData;
import com.momosoftworks.coldsweat.data.configuration.*;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRegistries
{
    // Item Registries
    public static IForgeRegistry<InsulatorData> INSULATOR_DATA = null;
    public static IForgeRegistry<FuelData> FUEL_DATA = null;
    public static IForgeRegistry<ItemData> FOOD_DATA = null;

    // World Registries
    public static IForgeRegistry<BlockTempData> BLOCK_TEMP_DATA = null;
    public static IForgeRegistry<BiomeTempData> BIOME_TEMP_DATA = null;
    public static IForgeRegistry<DimensionTempData> DIMENSION_TEMP_DATA = null;
    public static IForgeRegistry<StructureTempData> STRUCTURE_TEMP_DATA = null;

    // Entity Registries
    public static IForgeRegistry<MountData> MOUNT_DATA = null;
    public static IForgeRegistry<SpawnBiomeData> ENTITY_SPAWN_BIOME_DATA = null;
}
