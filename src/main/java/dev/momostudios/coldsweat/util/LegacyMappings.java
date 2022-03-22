package dev.momostudios.coldsweat.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

public class LegacyMappings
{
    public static Method getBiomeOld;
    static
    {
        try
        {
            getBiomeOld = ObfuscationReflectionHelper.findMethod(BiomeManager.class, "m_47881", BlockPos.class);
        }
        catch (Exception e) {}
    }
}
