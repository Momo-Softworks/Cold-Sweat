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
import java.util.function.Function;

public class CreateFluidPipeTemp extends BlockTemp
{
    public CreateFluidPipeTemp()
    {   super(AllBlocks.FLUID_PIPE.get());
    }

    @Override
    public double getTemperature(World level, @Nullable LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getTemperature(level, entity, state, pos, distance), 0.0) / 4;
    }

    protected static <T> T mapFluidBlockTemp(World level, BlockPos pos, Function<BlockTemp, T> consumer, T defaultVal)
    {
        FluidStack fluid = getFluid(level, pos);
        if (fluid != null && !fluid.isEmpty())
        {
            BlockState fluidState = fluid.getFluid().defaultFluidState().createLegacyBlock();
            return BlockTempRegistry.getFirstBlockTempFor(fluidState, level, pos).map(consumer).orElse(defaultVal);
        }
        return defaultVal;
    }

    protected static FluidStack getFluid(World level, BlockPos pos)
    {
        FluidPipeTileEntity pipe = (FluidPipeTileEntity) level.getBlockEntity(pos);
        if (pipe == null) return null;
        FluidTransportBehaviour fluidBehavior = pipe.getBehaviour(FluidTransportBehaviour.TYPE);
        AtomicReference<FluidStack> fluidHolder = new AtomicReference<>(null);
        if (fluidBehavior.interfaces == null) return null;
        fluidBehavior.interfaces.forEach((direction, flow) ->
        {
            if (fluidHolder.get() != null) return;
            if (flow != null)
            {   fluidHolder.set(flow.getProvidedFluid());
            }
        });
        return fluidHolder.get();
    }

    @Override
    public double getMinEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getMinEffect(entity, level, pos, state), Double.NEGATIVE_INFINITY);
    }

    @Override
    public double getMaxEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getMaxEffect(entity, level, pos, state), Double.POSITIVE_INFINITY);
    }

    @Override
    public double getMinTemp(LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getMinTemp(entity, level, pos, state), Double.NEGATIVE_INFINITY);
    }

    @Override
    public double getMaxTemp(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getMaxTemp(entity, level, pos, state), Double.POSITIVE_INFINITY);
    }

    @Override
    public double getRange(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getRange(entity, level, pos, state), 7.0);
    }

    @Override
    public boolean isLogarithmic(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.isLogarithmic(entity, level, pos, state), false);
    }

    @Override
    public boolean fades(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.fades(entity, level, pos, state), true);
    }
}
