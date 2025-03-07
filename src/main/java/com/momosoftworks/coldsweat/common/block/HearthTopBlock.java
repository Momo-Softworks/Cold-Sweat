package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.core.init.ItemInit;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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

    public HearthTopBlock(Block.Properties properties)
    {   super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == ModItems.SMOKESTACK && level.getBlockState(pos.relative(rayTraceResult.getDirection())).canBeReplaced())
        {   return InteractionResult.PASS;
        }
        InteractionResult baseResult = super.use(state, level, pos, player, hand, rayTraceResult);
        if (baseResult.consumesAction())
        {   return baseResult;
        }
        if (level.getBlockState(pos.below()).getBlock() instanceof HearthBottomBlock hearthBottomBlock)
        {   return hearthBottomBlock.use(level.getBlockState(pos.below()), level, pos.below(), player, hand, rayTraceResult);
        }
        return InteractionResult.PASS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {   super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.getBlockState(pos.below()).getBlock() != ModBlocks.HEARTH_BOTTOM)
        {   level.destroyBlock(pos, false);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter getter, BlockPos pos, BlockState state)
    {   return new ItemStack(ItemInit.HEARTH.get());
    }
}
