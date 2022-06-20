package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.WorldSettingsConfig;
import dev.momostudios.coldsweat.util.LegacyMethodHelper;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class BiomeTempModifier extends TempModifier
{
    static Method GET_TEMPERATURE = ObfuscationReflectionHelper.findMethod(Biome.class, "m_47528_", BlockPos.class);
    static
    {
        GET_TEMPERATURE.setAccessible(true);
    }

    static Map<Biome, Number> BIOME_TEMPS = ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().biomeTemperatures());
    static Map<Biome, Number> BIOME_OFFSETS = ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().biomeOffsets());
    static Map<ResourceLocation, Number> DIMENSION_TEMPS = new HashMap<>();
    static Map<ResourceLocation, Number> DIMENSION_OFFSETS = new HashMap<>();
    static
    {
        for (List<?> entry : WorldSettingsConfig.getInstance().dimensionOffsets())
        {
            DIMENSION_OFFSETS.put(new ResourceLocation((String) entry.get(0)), (Number) entry.get(1));
        }
        for (List<?> entry : WorldSettingsConfig.getInstance().dimensionTemperatures())
        {
            DIMENSION_TEMPS.put(new ResourceLocation((String) entry.get(0)), (Number) entry.get(1));
        }
    }
    @Override
    public Temperature getResult(Temperature temp, Player player)
    {
        BiomeManager biomeManager = player.level.getBiomeManager();

        try
        {
            double worldTemp = 0;
            for (BlockPos blockPos : WorldHelper.getNearbyPositions(player.blockPosition(), 100, 10))
            {
                Biome biome = LegacyMethodHelper.getBiome(biomeManager, blockPos);

                // Should temperature be overridden by config
                Number biomeOverride = BIOME_TEMPS.get(biome);
                Number dimensionOverride = DIMENSION_TEMPS.get(player.level.dimension().location());

                if (dimensionOverride != null)
                {
                    worldTemp += dimensionOverride.doubleValue();
                    continue;
                }
                if (biomeOverride != null)
                {
                    worldTemp += biomeOverride.doubleValue();
                    continue;
                }

                Number biomeOffset = BIOME_OFFSETS.get(biome);
                Number dimensionOffset = DIMENSION_OFFSETS.get(player.level.dimension().location());

                // If temperature is not overridden, apply the offsets
                worldTemp += (float) GET_TEMPERATURE.invoke(biome, blockPos);
                if (biomeOffset != null) worldTemp += biomeOffset.doubleValue();
                if (dimensionOffset != null) worldTemp += dimensionOffset.doubleValue();

            }
            return temp.add(worldTemp / 100);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return temp;
        }
    }

    public String getID()
    {
        return "cold_sweat:biome_temperature";
    }
}