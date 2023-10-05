package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Pair;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DepthTempModifier extends TempModifier
{
    static int SAMPLES = 25;

    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        if (entity.worldObj.provider.hasNoSky) return temp -> temp;

        double midTemp = (ConfigSettings.MAX_TEMP.get() + ConfigSettings.MIN_TEMP.get()) / 2;
        BlockPos playerPos = new BlockPos(entity);
        World world = entity.worldObj;

        List<Pair<Double, Double>> depthTable = new ArrayList<>();

        for (BlockPos pos : WorldHelper.getPositionGrid(playerPos, SAMPLES, 8))
        {   depthTable.add(Pair.of(Math.max(0d, WorldHelper.getHeight(pos, world) - playerPos.getY()), Math.sqrt(pos.distSqr(playerPos))));
        }

        double finalDepth = CSMath.weightedAverage(depthTable);
        return temp ->
        {
            return CSMath.blend(temp,
                                CSMath.weightedAverage(CSMath.blend(midTemp, temp, world.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, playerPos.getX(), playerPos.getY(), playerPos.getZ()), 0, 15),
                                                       CSMath.blend(temp, midTemp, finalDepth, 4, 20), 1, 2),
                                ConfigSettings.CAVE_INSULATION.get(), 0d, 1d);
        };
    }

    public String getID()
    {
        return "cold_sweat:depth";
    }
}
