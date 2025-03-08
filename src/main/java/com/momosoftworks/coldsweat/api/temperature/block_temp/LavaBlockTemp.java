package com.momosoftworks.coldsweat.api.temperature.block_temp;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LavaBlockTemp extends BlockTemp
{
    public LavaBlockTemp()
    {
        super(0, 4, -Double.MAX_VALUE, 21.5, 7, true, Blocks.LAVA);
    }

    @Override
    public double getTemperature(World world, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        int height = state.getFluidState().getAmount();
        return (height / 7f) / (entity.getVehicle() instanceof StriderEntity ? 50d : 5d);
    }
}
