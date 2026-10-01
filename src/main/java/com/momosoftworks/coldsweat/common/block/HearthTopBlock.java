package com.momosoftworks.coldsweat.common.block;

import net.minecraft.server.level.ServerLevel;
import javax.annotation.Nullable;
import net.minecraft.world.level.redstone.Orientation;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
        if (!level.isClientSide() && level.getBlockState(pos.below()).getBlock() instanceof HearthBottomBlock hearthBottomBlock
        && !super.useWithoutItem(state, level, pos, player, rayTraceResult).consumesAction())
        {   return hearthBottomBlock.useWithoutItem(level.getBlockState(pos.below()), level, pos.below(), player, rayTraceResult);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (stack.is(ModItems.SMOKESTACK) && level.getBlockState(pos.relative(rayTraceResult.getDirection())).canBeReplaced())
        {   return InteractionResult.PASS;
        }
        InteractionResult baseResult = super.useItemOn(stack, state, level, pos, player, hand, rayTraceResult);
        if (baseResult.consumesAction())
        {   return baseResult;
        }
        if (level.getBlockState(pos.below()).getBlock() instanceof HearthBottomBlock hearthBottomBlock
        && !super.useItemOn(stack, state, level, pos, player, hand, rayTraceResult).consumesAction())
        {   return hearthBottomBlock.useItemOn(stack, level.getBlockState(pos.below()), level, pos.below(), player, hand, rayTraceResult);
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation fromPos, boolean isMoving)
    {   super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.getBlockState(pos.below()).getBlock() != ModBlocks.HEARTH_BOTTOM.value())
        {   this.destroy(level, pos, state);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston)
    {
        if (!movedByPiston && level.getBlockState(pos.below()).getBlock() == ModBlocks.HEARTH_BOTTOM.value())
        {   level.destroyBlock(pos.below(), false);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player)
    {   return new ItemStack(ModItems.HEARTH.get());
    }
}
