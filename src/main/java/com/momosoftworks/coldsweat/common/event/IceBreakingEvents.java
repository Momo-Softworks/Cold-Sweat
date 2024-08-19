package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
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
        LevelAccessor level = event.getWorld();
        ItemStack tool = event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        BlockPos pos = event.getPos();
        Material belowMaterial = level.getBlockState(pos.below()).getMaterial();

        if (state.is(Blocks.ICE) && !tool.isCorrectToolForDrops(state)
        && !event.getPlayer().getAbilities().instabuild
        && belowMaterial.blocksMotion() || belowMaterial.isLiquid())
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
        ItemStack tool = event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);

        if ((state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE))
        && WorldHelper.isEffectivelyPickaxe(tool))
        {   event.setNewSpeed(event.getNewSpeed() / 2);
        }
    }
}
