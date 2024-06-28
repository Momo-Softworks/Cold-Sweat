package com.momosoftworks.coldsweat.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmokestackBlock extends Block
{
    public static Properties getProperties()
    {
        return Properties
                .of()
                .sound(SoundType.STONE)
                .strength(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .dynamicShape();
    }

    public SmokestackBlock(Properties properties)
    {   super(properties);
        this.registerDefaultState(this.defaultBlockState());
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
    {   return Block.box(4, 0, 4, 12, 16, 12);
    }

    public static Item.Properties getItemProperties()
    {   return new Item.Properties();
    }
}
