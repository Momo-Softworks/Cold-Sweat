package com.momosoftworks.coldsweat.api.temperature.block_temp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class FurnaceBlockTemp extends BlockTemp
{
    public FurnaceBlockTemp()
    {
        super(0, 0.88, Double.NEGATIVE_INFINITY, 12.6, 7, true,
              ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof AbstractFurnaceBlock).toArray(Block[]::new));
    }

    @Override
    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {   return 0.33;
    }

    @Override
    public boolean hasBlock(Block block)
    {
        return block instanceof AbstractFurnaceBlock;
    }

    @Override
    public boolean isValid(Level level, BlockPos pos, BlockState state)
    {   return state.hasProperty(AbstractFurnaceBlock.LIT) && state.getValue(AbstractFurnaceBlock.LIT);
    }
}
