package com.momosoftworks.coldsweat.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class IceBreakingEvents
{
    /**
     * Re-enable spawning water if the player breaks ice without the correct tool
     */
    @SubscribeEvent
    public static void onIceBreak(BlockEvent.BreakEvent event)
    {
        BlockState state = event.getState();
        LevelAccessor level = event.getLevel();
        ItemStack tool = event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        BlockPos pos = event.getPos();
        BlockState belowState = level.getBlockState(pos.below());

        if (state.is(Blocks.ICE) && !tool.isCorrectToolForDrops(state)
        && !event.getPlayer().getAbilities().instabuild
        && (belowState.blocksMotion() || belowState.liquid()))
        {   level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
    }

    /**
     * Make ice blocks a little slower to mine with its preferred tool
     */
    @SubscribeEvent
    public static void onIceMining(PlayerEvent.BreakSpeed event)
    {
        BlockState state = event.getState();
        ItemStack tool = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND);

        if ((state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE))
        && tool.is(ItemTags.PICKAXES))
        {   event.setNewSpeed(event.getNewSpeed() / 2);
        }
    }
}
