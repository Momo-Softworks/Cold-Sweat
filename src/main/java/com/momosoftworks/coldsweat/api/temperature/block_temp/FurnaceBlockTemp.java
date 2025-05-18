package com.momosoftworks.coldsweat.api.temperature.block_temp;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class FurnaceBlockTemp extends BlockTemp
{
    public FurnaceBlockTemp()
    {
        super(0, 0.88, Double.NEGATIVE_INFINITY, 12.6, 7, true,
              ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof AbstractFurnaceBlock).toArray(Block[]::new));
    }

    @Override
    public double getTemperature(World world, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {   return 0.33;
    }

    @Override
    public boolean hasBlock(Block block)
    {
        return block instanceof AbstractFurnaceBlock;
    }

    @Override
    public boolean isValid(World level, BlockPos pos, BlockState state)
    {   return state.hasProperty(AbstractFurnaceBlock.LIT) && state.getValue(AbstractFurnaceBlock.LIT);
    }
}
