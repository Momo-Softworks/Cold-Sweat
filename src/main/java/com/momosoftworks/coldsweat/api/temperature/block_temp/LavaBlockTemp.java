package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.level.material.FluidState;

public class LavaBlockTemp extends BlockTemp
{
    public LavaBlockTemp()
    {
        super(0, 4, -Double.MAX_VALUE, 21.5, 7, true, Blocks.LAVA);
    }

    @Override
    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        int height = state.getFluidState().getAmount();
        return (height / 7f) / (entity.getVehicle() instanceof Strider ? 50d : 5d);
    }
}
