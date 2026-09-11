package com.momosoftworks.coldsweat.api.temperature.block_temp;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class FurnaceBlockTemp extends BlockTemp
{
    public FurnaceBlockTemp()
    {
        super(ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof AbstractFurnaceBlock).toArray(Block[]::new));
    }

    @Override
    public double getTemperature(World world, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {   return 0.33;
    }

    @Override
    public double getMinEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return 0;
    }

    @Override
    public double getMaxEffect(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return 0.88;
    }

    @Override
    public double getMinTemp(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double getMaxTemp(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return 12.6;
    }

    @Override
    public boolean hasBlock(Block block)
    {   return block instanceof AbstractFurnaceBlock;
    }

    @Override
    public boolean isLogarithmic(@Nullable LivingEntity entity, World level, BlockPos pos, BlockState state)
    {   return true;
    }

    @Override
    public boolean isValid(World level, BlockPos pos, BlockState state)
    {   return state.hasProperty(AbstractFurnaceBlock.LIT) && state.getValue(AbstractFurnaceBlock.LIT);
    }
}
