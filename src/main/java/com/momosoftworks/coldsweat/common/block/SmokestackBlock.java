package com.momosoftworks.coldsweat.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class SmokestackBlock extends Block
{
    public static Properties getProperties()
    {
        return Properties
                .of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(2f, 10f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .dynamicShape();
    }

    public SmokestackBlock(Block.Properties properties)
    {   super(properties);
        this.registerDefaultState(this.defaultBlockState());
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader level, BlockPos pos)
    {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader getter, BlockPos pos, ISelectionContext context)
    {   return Block.box(4, 0, 4, 12, 16, 12);
    }

    public static Item.Properties getItemProperties()
    {   return new Item.Properties();
    }
}
