package dev.momostudios.coldsweat.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

public class LegacyMethodHelper
{
    @Nullable
    public static Biome getBiome(Level level, BlockPos blockPos)
    {
        try
        {
            if (FMLLoader.versionInfo().mcVersion().equals("1.18.1"))
            {
                return (Biome) BiomeManager.class.getDeclaredMethod("m_47881_", BlockPos.class).invoke(level.getBiomeManager(), blockPos);
            }
            else return level.getChunkSource().getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.BIOMES, false).getNoiseBiome(blockPos.getX() & 15, blockPos.getY(), blockPos.getZ() & 15).value();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
