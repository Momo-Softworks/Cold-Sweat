package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.core.init.ItemInit;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class HearthTopBlock extends SmokestackBlock
{
    public static Properties getProperties()
    {
        return Properties
                .of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(2, 10)
                .requiresCorrectToolForDrops();
    }

    public HearthTopBlock(Block.Properties properties)
    {   super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == ModItems.SMOKESTACK && level.getBlockState(pos.relative(rayTraceResult.getDirection())).canBeReplaced(new BlockItemUseContext(player, hand, stack, rayTraceResult)))
        {   return ActionResultType.PASS;
        }
        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.getBlock() instanceof HearthBottomBlock)
        {   return belowState.getBlock().use(belowState, level, pos.below(), player, hand, rayTraceResult);
        }
        return ActionResultType.PASS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (world.getBlockState(pos.below()).getBlock() == ModBlocks.HEARTH_BOTTOM)
            {   world.destroyBlock(pos.below(), false);
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public ItemStack getCloneItemStack(IBlockReader getter, BlockPos pos, BlockState state)
    {   return new ItemStack(ItemInit.HEARTH.get());
    }
}
