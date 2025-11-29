package com.momosoftworks.coldsweat.api.temperature.block_temp.compat;

import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class CreateFluidPipeTemp extends BlockTemp
{
    public CreateFluidPipeTemp()
    {
        super(-0.75, 0.75,
              Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
              7, true, AllBlocks.FLUID_PIPE.get());
    }

    @Override
    public double getTemperature(World level, @Nullable LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        FluidPipeTileEntity pipe = (FluidPipeTileEntity) level.getBlockEntity(pos);
        if (pipe != null)
        {
            FluidTransportBehaviour fluidBehavior = pipe.getBehaviour(FluidTransportBehaviour.TYPE);
            AtomicReference<FluidStack> fluidHolder = new AtomicReference<>(null);
            if (fluidBehavior.interfaces == null) return 0;
            fluidBehavior.interfaces.forEach((direction, flow) ->
            {
                if (fluidHolder.get() != null) return;
                if (flow != null)
                {   fluidHolder.set(flow.getProvidedFluid());
                }
            });
            FluidStack fluid = fluidHolder.get();
            if (fluid != null && !fluid.isEmpty())
            {
                BlockState fluidState = fluid.getFluid().defaultFluidState().createLegacyBlock();
                return BlockTempRegistry.getFirstBlockTempFor(fluidState, level, pos).map(blockTemp ->
                       {   return blockTemp.getTemperature(level, entity, fluidState, pos, distance);
                       }).orElse(0.0) / 4;
            }
        }
        return 0;
    }
}
