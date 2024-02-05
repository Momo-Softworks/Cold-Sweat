package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Collections;
import java.util.List;

public class MinecartInsulationBlock extends Block
{
    public static Properties getProperties()
    {
        return Properties
                .of(Material.WOOL)
                .sound(SoundType.WOOL)
                .strength(0f, 0f);
    }

    public Item asItem()
    {   return ModItems.MINECART_INSULATION;
    }

    public MinecartInsulationBlock(Block.Properties properties)
    {   super(properties);
        this.registerDefaultState(this.defaultBlockState());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {   return Collections.emptyList();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {   return this.defaultBlockState();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, IWorld level, BlockPos blockPos, BlockPos otherBlockPos)
    {   return Blocks.AIR.defaultBlockState();
    }
}
