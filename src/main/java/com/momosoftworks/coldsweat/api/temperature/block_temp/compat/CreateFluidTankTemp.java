package com.momosoftworks.coldsweat.api.temperature.block_temp.compat;

import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CreateFluidTankTemp extends BlockTemp
{
    public CreateFluidTankTemp()
    {   super(AllBlocks.FLUID_TANK.get(), AllBlocks.CREATIVE_FLUID_TANK.get());
    }

    @Override
    public double getTemperature(World level, @Nullable LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        double fillFraction = getFillFraction(level, pos);
        return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getTemperature(level, entity, state, pos, distance) / 2, 0.0) * fillFraction;
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
        FluidTankTileEntity controller = getController(level, pos);
        if (controller == null) return null;

        FluidStack fluid = controller.getTankInventory().getFluid();
        // Fluid level hasn't risen high enough to reach this block yet
        if (fluid.isEmpty() || getFillFraction(controller, pos) <= 0)
        {   return null;
        }
        return fluid;
    }

    /**
     * The fraction (0-1) of this specific tank block that is submerged in fluid, based on the overall
     * fluid level of the multiblock structure. Ranges from 0 (fluid level at or below this block's
     * minimum Y) to 1 (fluid level at or above this block's maximum Y), interpolating in between.
     */
    protected static double getFillFraction(World level, BlockPos pos)
    {
        FluidTankTileEntity controller = getController(level, pos);
        return controller == null ? 0 : getFillFraction(controller, pos);
    }

    protected static double getFillFraction(FluidTankTileEntity controller, BlockPos pos)
    {
        FluidStack fluid = controller.getTankInventory().getFluid();
        if (fluid.isEmpty()) return 0;

        // How many blocks tall the fluid column is, from the bottom of the structure
        // (getTankSize(0) is a flat per-block constant, not the multiblock's total capacity, so it can't be used here)
        double totalCapacity = controller.getTankInventory().getCapacity();
        double fluidHeight = (double) fluid.getAmount() / totalCapacity;
        // This block's minimum Y, relative to the bottom of the structure
        double blockMinY = pos.getY() - controller.getBlockPos().getY();

        return CSMath.clamp(fluidHeight - blockMinY, 0, 1);
    }

    @Nullable
    protected static FluidTankTileEntity getController(World level, BlockPos pos)
    {
        FluidTankTileEntity tank = (FluidTankTileEntity) level.getBlockEntity(pos);
        return tank == null ? null : tank.getControllerTE();
    }

    @Override
    public double getMinEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getMinEffect(entity, level, pos, state), Double.NEGATIVE_INFINITY);
    }

    @Override
    public double getMaxEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   double val = mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getMaxEffect(entity, level, pos, state), Double.POSITIVE_INFINITY);
        return val;
    }

    @Override
    public double getMinTemp(LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getMinTemp(entity, level, pos, state), Double.NEGATIVE_INFINITY);
    }

    @Override
    public double getMaxTemp(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   double val =  mapFluidBlockTemp(level, pos, blockTemp -> blockTemp.getMaxTemp(entity, level, pos, state), Double.POSITIVE_INFINITY);
        return val;
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
