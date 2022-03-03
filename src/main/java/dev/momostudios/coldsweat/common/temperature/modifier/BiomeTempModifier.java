package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.List;

public class BiomeTempModifier extends TempModifier
{
    public BiomeTempModifier()
    {
        addArgument("value", 0d);
    }

    @Override
    public double getResult(Temperature temp, Player player)
    {
        if (player.level.getGameTime() % 5 == 0)
        {
            Method getTemperature = ObfuscationReflectionHelper.findMethod(Biome.class, "getHeightAdjustedTemperature", BlockPos.class);
            getTemperature.setAccessible(true);

            try
            {
                double worldTemp = 0;
                for (BlockPos blockPos : WorldHelper.getNearbyPositions(player.blockPosition(), 200, 6))
                {
                    Biome biome = player.level.getBiome(blockPos);
                    worldTemp += (float) getTemperature.invoke(biome, blockPos) + getTemperatureOffset(biome.getRegistryName(), player.level.dimension().location());

                    // Should temperature be overridden by config
                    TempOverride biomeOverride = biomeOverride(biome.getRegistryName());
                    TempOverride dimensionOverride = dimensionOverride(player.level.dimension().location());

                    if (dimensionOverride.override)
                    {
                        setArgument("value", dimensionOverride.value);
                        return dimensionOverride.value;
                    }
                    if (biomeOverride.override)
                    {
                        setArgument("value", biomeOverride.value);
                        return biomeOverride.value;
                    }
                }
                setArgument("value", temp.get() + (worldTemp / 200));
            }
            catch (Exception e) {
            }
        }
        return (double) getArgument("value");
    }

    protected double getTemperatureOffset(ResourceLocation biomeID, ResourceLocation dimensionID)
    {
        double offset = 0;
        for (List<String> value : ConfigCache.getInstance().worldOptionsReference.get("biome_offsets"))
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                offset += Double.parseDouble(value.get(1));
        }

        for (List<String> value : ConfigCache.getInstance().worldOptionsReference.get("dimension_offsets"))
        {
            if (new ResourceLocation(value.get(0)).equals(dimensionID))
                offset += Double.parseDouble(value.get(1));
        }
        return offset;
    }

    protected TempOverride biomeOverride(ResourceLocation biomeID)
    {
        for (List<String> value : ConfigCache.getInstance().worldOptionsReference.get("biome_temperatures"))
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                return new TempOverride(true, Double.parseDouble(value.get(1)));
        }
        return new TempOverride(false, 0.0d);
    }

    protected TempOverride dimensionOverride(ResourceLocation biomeID)
    {
        for (List<String> value : ConfigCache.getInstance().worldOptionsReference.get("dimension_temperatures"))
        {
            if (new ResourceLocation(value.get(0)).equals(biomeID))
                return new TempOverride(true, Double.parseDouble(value.get(1)));
        }
        return new TempOverride(false, 0.0d);
    }

    private static class TempOverride
    {
        public boolean override;
        public double value;

        public TempOverride(boolean override, double value)
        {
            this.override = override;
            this.value = value;
        }
    }

    public String getID()
    {
        return "cold_sweat:biome_temperature";
    }
}