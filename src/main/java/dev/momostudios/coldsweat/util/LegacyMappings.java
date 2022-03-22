package dev.momostudios.coldsweat.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

public class LegacyMappings
{
    public static Method getBiomeOld;
    static
    {
        if (Minecraft.getInstance().getLaunchedVersion().contains("1.18.1"))
        try
        {
            getBiomeOld = ObfuscationReflectionHelper.findMethod(BiomeManager.class, "m_47881", BlockPos.class);
        }
        catch (Exception e) {}
    }
}
