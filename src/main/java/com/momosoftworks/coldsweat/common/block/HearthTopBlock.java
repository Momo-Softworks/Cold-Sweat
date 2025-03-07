package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class HearthTopBlock extends SmokestackBlock
{
    public static Properties getProperties()
    {
        return Properties
                .of()
                .sound(SoundType.STONE)
                .strength(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops();
    }

    public HearthTopBlock(Properties properties)
    {   super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult rayTraceResult)
    {
        if (!level.isClientSide && level.getBlockState(pos.below()).getBlock() instanceof HearthBottomBlock hearthBottomBlock
        && !super.useWithoutItem(state, level, pos, player, rayTraceResult).consumesAction())
        {   return hearthBottomBlock.useWithoutItem(level.getBlockState(pos.below()), level, pos.below(), player, rayTraceResult);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (stack.is(ModItems.SMOKESTACK) && level.getBlockState(pos.relative(rayTraceResult.getDirection())).canBeReplaced())
        {   return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        ItemInteractionResult baseResult = super.useItemOn(stack, state, level, pos, player, hand, rayTraceResult);
        if (baseResult.consumesAction())
        {   return baseResult;
        }
        if (level.getBlockState(pos.below()).getBlock() instanceof HearthBottomBlock hearthBottomBlock
        && !super.useItemOn(stack, state, level, pos, player, hand, rayTraceResult).consumesAction())
        {   return hearthBottomBlock.useItemOn(stack, level.getBlockState(pos.below()), level, pos.below(), player, hand, rayTraceResult);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {   super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.getBlockState(pos.below()).getBlock() != ModBlocks.HEARTH_BOTTOM.value())
        {   this.destroy(level, pos, state);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockState(pos.below()).getBlock() == ModBlocks.HEARTH_BOTTOM.value())
            {   level.destroyBlock(pos.below(), false);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player)
    {   return new ItemStack(ModItems.HEARTH.get());
    }
}
