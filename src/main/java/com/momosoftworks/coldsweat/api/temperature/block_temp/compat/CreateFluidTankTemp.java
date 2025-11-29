package com.momosoftworks.coldsweat.api.temperature.block_temp.compat;

import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class CreateFluidTankTemp extends BlockTemp
{
    public CreateFluidTankTemp()
    {
        super(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 7, true, AllBlocks.FLUID_TANK.get(), AllBlocks.CREATIVE_FLUID_TANK.get());
    }

    @Override
    public double getTemperature(Level level, @Nullable LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        FluidTankBlockEntity tank = (FluidTankBlockEntity) level.getBlockEntity(pos);
        if (tank != null)
        {
            FluidStack fluid;
            if (tank.isController())
            {
                if (tank.boiler != null)
                {
                    int heat = tank.boiler.passiveHeat ? 1 : tank.boiler.getTheoreticalHeatLevel();
                    if (heat > 0)
                    {   return Math.min(3, heat / 4.0);
                    }
                }
                fluid = tank.getFluid(0);
            }
            // Not controller; check controller for fluid
            else
            {
                FluidTankBlockEntity controller = tank.getControllerBE();
                if (controller == null) return 0;
                fluid = controller.getFluid(0);
                // Check if combined tank has enough fluid to reach this block
                double distToController = CSMath.getDistance(pos, controller.getBlockPos());
                double fluidHeight = (double) fluid.getAmount() / tank.getTankSize(0);
                if (distToController > fluidHeight) return 0;
            }
            if (!fluid.isEmpty())
            {
                BlockState fluidState = fluid.getFluid().defaultFluidState().createLegacyBlock();
                return BlockTempRegistry.getFirstBlockTempFor(fluidState, level, pos).map(blockTemp ->
                       {   return blockTemp.getTemperature(level, entity, fluidState, pos, distance);
                       }).orElse(0.0);
            }
        }
        return 0;
    }
}
