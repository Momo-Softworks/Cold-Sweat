package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.loot.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
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
        if (!ConfigSettings.USE_CUSTOM_ICE_DROPS.get()) return;

        BlockState state = event.getState();
        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState belowState = level.getBlockState(pos.below());

        if (state.is(Blocks.ICE) && !ForgeHooks.isCorrectToolForDrops(state, event.getPlayer())
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
        if (!ConfigSettings.USE_CUSTOM_ICE_DROPS.get()) return;

        BlockState state = event.getState();
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        float speed = event.getNewSpeed();

        if (isModifiableIceBlock(state)
        && !ForgeHooks.isCorrectToolForDrops(state, player))
        {
            // Increase speed for pickaxes (even if the tier isn't high enough)
            if (tool.is(ItemTags.PICKAXES))
            {   event.setNewSpeed(speed * 2);
            }
            // Non-pickaxes need a huge speed boost
            else event.setNewSpeed(speed * 5);
        }
        if (state.is(Blocks.PACKED_ICE))
        {   event.setNewSpeed(event.getNewSpeed() / 6);
        }
    }

    @SubscribeEvent
    public static void iceHarvestCheck(PlayerEvent.HarvestCheck event)
    {
        if (!ConfigSettings.USE_CUSTOM_ICE_DROPS.get()) return;

        BlockState state = event.getTargetBlock();
        Player player = event.getEntity();
        ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (isModifiableIceBlock(state))
        {   event.setCanHarvest(tool.is(ItemTags.PICKAXES) && event.getEntity().getMainHandItem().isCorrectToolForDrops(state));
        }
    }

    public static boolean isModifiableIceBlock(BlockState state)
    {
        return state.is(Blocks.ICE)
            || state.is(Blocks.PACKED_ICE)
            || state.is(Blocks.BLUE_ICE);
    }

    public static ResourceLocation getLootTableForIce(BlockState state)
    {
        if (state.is(Blocks.ICE))
        {   return ModLootTables.CUSTOM_ICE_DROP;
        }
        if (state.is(Blocks.PACKED_ICE))
        {   return ModLootTables.CUSTOM_PACKED_ICE_DROP;
        }
        if (state.is(Blocks.BLUE_ICE))
        {   return ModLootTables.CUSTOM_BLUE_ICE_DROP;
        }
        return null;
    }
}
