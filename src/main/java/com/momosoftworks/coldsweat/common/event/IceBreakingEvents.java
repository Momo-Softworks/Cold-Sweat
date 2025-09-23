package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.loot.ModLootTables;
import com.momosoftworks.coldsweat.util.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolActions;
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

        Player player = event.getPlayer();
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        BlockState state = event.getState();
        LevelAccessor ilevel = event.getWorld();
        BlockPos pos = event.getPos();
        Material belowMaterial = ilevel.getBlockState(pos.below()).getMaterial();

        if (state.is(Blocks.ICE) && !ForgeHooks.isCorrectToolForDrops(state, player)
        && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0
        && !player.getAbilities().instabuild
        && (belowMaterial.blocksMotion() || belowMaterial.isLiquid())
        && !ilevel.dimensionType().ultraWarm())
        {   ilevel.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
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
        Player player = event.getPlayer();
        ItemStack tool = player.getMainHandItem();
        float speed = event.getNewSpeed();

        if (isModifiableIceBlock(state)
        && !ForgeHooks.isCorrectToolForDrops(state, player))
        {
            // Increase speed for pickaxes (even if the tier isn't high enough)
            if (tool.canPerformAction(ToolActions.PICKAXE_DIG))
            {   event.setNewSpeed(speed * 2);
            }
            // Non-pickaxes need a huge speed boost
            else event.setNewSpeed(speed * 3);
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
        Player player = event.getPlayer();
        ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (isModifiableIceBlock(state))
        {   event.setCanHarvest(tool.canPerformAction(ToolActions.PICKAXE_DIG) && event.getPlayer().getMainHandItem().isCorrectToolForDrops(state));
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
