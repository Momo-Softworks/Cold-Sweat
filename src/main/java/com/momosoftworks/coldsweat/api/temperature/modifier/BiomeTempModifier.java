package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Pair;
import com.momosoftworks.coldsweat.util.math.Triplet;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.function.Function;

public class BiomeTempModifier extends TempModifier
{
    public BiomeTempModifier()
    {   this(16);
    }

    public BiomeTempModifier(int samples)
    {   this.getNBT().setInteger("Samples", samples);
    }

    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        try
        {
            World world = entity.worldObj;
            int dimensionID = entity.worldObj.provider.dimensionId;
            BlockPos entPos = new BlockPos(entity.getPosition(0));
            double time = Math.sin(entity.worldObj.getWorldTime() / (12000 / Math.PI));

            // In the case that the dimension temperature is overridden by config, use that and skip everything else
            Pair<Double, Temperature.Units> dimTempOverride = ConfigSettings.DIMENSION_TEMPS.get().get(dimensionID);
            if (dimTempOverride != null)
            {   return temp -> temp + Temperature.convertUnits(dimTempOverride.getFirst(), dimTempOverride.getSecond(), Temperature.Units.MC, true);
            }

            double worldTemp = 0;
            int biomeCount = 0;
            for (BlockPos blockPos : WorldHelper.getPositionGrid(new BlockPos(entity.getPosition(0)), 36, 10))
            {
                if (blockPos.distSqr(entPos) > 30*30) continue;
                BiomeGenBase biome = entity.worldObj.getBiomeGenForCoords(blockPos.getX(), blockPos.getZ());
                if (biome == null) continue;
                int biomeID = biome.biomeID;
                biomeCount++;

                double biomeVariance = 1 / Math.max(1, 2 + biome.rainfall * 2);
                double baseTemp = biome.temperature;

                // Get the biome's temperature, either overridden by config or calculated
                // Start with biome override
                Triplet<Double, Double, Temperature.Units> cTemp = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeID,
                                                                   new Triplet<>(baseTemp - biomeVariance, baseTemp + biomeVariance, Temperature.Units.MC));
                Triplet<Double, Double, Temperature.Units> cOffset = ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeID,
                                                                     new Triplet<>(0d, 0d, Temperature.Units.MC));
                Pair<Double, Double> configTemp = CSMath.addPairs(Pair.of(cTemp.getFirst(), cTemp.getSecond()), Pair.of(cOffset.getFirst(), cOffset.getSecond()));

                // Biome temp at midnight (bottom of the sine wave)
                double min = configTemp.getFirst();
                // Biome temp at noon (top of the sine wave)
                double max = configTemp.getSecond();

                // If time doesn't exist in the player's dimension, don't use it
                if (!world.provider.isHellWorld)
                {
                    double altitude = entity.posY;
                    double mid = (min + max) / 2;
                    // Biome temp with time of day
                    worldTemp += CSMath.blend(min, max, time, -1, 1)
                               // Altitude calculation
                               + CSMath.blend(0, Math.min(-0.6, (min - mid) * 2), altitude, 63, world.getActualHeight());
                }
                else worldTemp += CSMath.average(max, min);
            }

            worldTemp /= biomeCount;

            // Add dimension offset, if present
            Pair<Double, Temperature.Units> dimTempOffsetConf = ConfigSettings.DIMENSION_OFFSETS.get().get(dimensionID);
            if (dimTempOffsetConf != null)
            {   worldTemp += Temperature.convertUnits(dimTempOffsetConf.getFirst(), dimTempOffsetConf.getSecond(), Temperature.Units.MC, false);
            }
            double finalWorldTemp = worldTemp;
            return temp -> temp + finalWorldTemp;
        }
        catch (Exception e)
        {   return (temp) -> temp;
        }
    }

    public String getID()
    {
        return "cold_sweat:biome_temperature";
    }
}