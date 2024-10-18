package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.loot.ModLootTables;
import com.momosoftworks.coldsweat.util.item.ItemHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.ForgeHooks;
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
        if (!ConfigSettings.USE_CUSTOM_ICE_DROPS.get()) return;

        BlockState state = event.getState();
        IWorld level = event.getWorld();
        BlockPos pos = event.getPos();
        Material belowMaterial = level.getBlockState(pos.below()).getMaterial();

        if (state.is(Blocks.ICE) && !ForgeHooks.canHarvestBlock(state, event.getPlayer(), level, pos)
        && !event.getPlayer().abilities.instabuild
        && (belowMaterial.blocksMotion() || belowMaterial.isLiquid()))
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
        PlayerEntity player = event.getPlayer();
        ItemStack tool = player.getMainHandItem();
        float speed = event.getNewSpeed();

        if (isModifiableIceBlock(state)
        && !ForgeHooks.canHarvestBlock(state, event.getPlayer(), player.level, event.getPos()))
        {
            // Increase speed for pickaxes (even if the tier isn't high enough)
            if (ItemHelper.isEffectivelyPickaxe(tool))
            {   event.setNewSpeed(speed * 2);
            }
            // Non-pickaxes need a huge speed boost
            else event.setNewSpeed(speed * 5);
        }
        if (state.is(Blocks.PACKED_ICE))
        {   event.setNewSpeed(event.getNewSpeed() / 3);
        }
    }

    @SubscribeEvent
    public static void iceHarvestCheck(PlayerEvent.HarvestCheck event)
    {
        if (!ConfigSettings.USE_CUSTOM_ICE_DROPS.get()) return;

        BlockState state = event.getTargetBlock();
        PlayerEntity player = event.getPlayer();
        ItemStack tool = player.getItemInHand(Hand.MAIN_HAND);

        if (isModifiableIceBlock(state))
        {   event.setCanHarvest(ItemHelper.isEffectivelyPickaxe(tool) && tool.isCorrectToolForDrops(state));
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
