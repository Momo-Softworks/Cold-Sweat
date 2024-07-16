package com.momosoftworks.coldsweat.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
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
        ServerLevel level = (ServerLevel) event.getLevel();
        ItemStack tool = event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        BlockPos pos = event.getPos();

        if (state.is(Blocks.ICE) && !tool.isCorrectToolForDrops(state))
        {   level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
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
        && tool.is(Tags.Items.TOOLS_PICKAXES))
        {   event.setNewSpeed(event.getNewSpeed() / 2);
        }
    }
}
