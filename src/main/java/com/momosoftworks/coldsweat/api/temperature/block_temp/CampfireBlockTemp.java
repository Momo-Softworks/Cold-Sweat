package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class CampfireBlockTemp extends BlockTemp
{
    public CampfireBlockTemp()
    {
        super(ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof CampfireBlock && block != Blocks.SOUL_CAMPFIRE).toArray(Block[]::new));
    }

    @Override
    public double getTemperature(World world, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        if (state.getValue(CampfireBlock.LIT))
        {   return CSMath.blend(0.476, 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public double maxEffect() {
        return Temperature.convertUnits(40, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return Temperature.convertUnits(400, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
