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

/**
 * Normalizes temperature toward the mid-point as the entity goes underground / is sheltered overhead.<br>
 * Renamed from {@code DepthTempModifier} to match 1.16's {@code ElevationTempModifier}.<br>
 * NOTE: 1.16's full data-driven {@code DEPTH_REGIONS} system is not yet ported; this keeps the
 * skylight + height falloff behaviour and the 1.16 sample-count constructor.
 */
public class ElevationTempModifier extends TempModifier
{
    public ElevationTempModifier()
    {   this(49);
    }

    public ElevationTempModifier(int samples)
    {   this.getNBT().setInteger("Samples", samples);
    }

    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        if (entity.worldObj.provider.hasNoSky) return temp -> temp;

        double midTemp = (ConfigSettings.MAX_TEMP.get() + ConfigSettings.MIN_TEMP.get()) / 2;
        BlockPos playerPos = new BlockPos(entity);
        World world = entity.worldObj;

        List<Pair<Double, Double>> depthTable = new ArrayList<>();

        for (BlockPos pos : WorldHelper.getPositionGrid(playerPos, this.getNBT().getInteger("Samples"), 8))
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
        return "cold_sweat:elevation";
    }
}
