package dev.momostudios.coldsweat.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

public class LegacyMethodHelper
{
    @Nullable
    public static Biome getBiome(BiomeManager biomeManager, BlockPos blockPos)
    {
        try
        {
            if (FMLLoader.versionInfo().mcVersion().equals("1.18.1"))
            {
                return (Biome) BiomeManager.class.getDeclaredMethod("m_47881_", BlockPos.class).invoke(biomeManager, blockPos);
            }
            else return biomeManager.getBiome(blockPos).value();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
