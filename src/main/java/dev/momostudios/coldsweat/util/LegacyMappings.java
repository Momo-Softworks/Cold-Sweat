package dev.momostudios.coldsweat.util;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.BiomeManager;

import java.lang.reflect.Method;

public class LegacyMappings
{
    public static Method getBiomeOld;

    static
    {
        try
        {
            getBiomeOld = BiomeManager.class.getDeclaredMethod("m_47881_", BlockPos.class);
        }
        catch (Exception e)
        {
            ColdSweat.LOGGER.warn("Failed to find method getBiome (m_47881) in BiomeManager");
            ColdSweat.LOGGER.warn(e.getMessage(), e);
        }
    }
}
