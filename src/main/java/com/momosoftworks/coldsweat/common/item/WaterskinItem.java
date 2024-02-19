package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class WaterskinItem extends Item
{
    public WaterskinItem()
    {
        super(new Properties().stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        InteractionResultHolder<ItemStack> ar = super.use(level, player, hand);
        ItemStack itemstack = ar.getObject();

        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockState lookingAt = level.getBlockState(blockhitresult.getBlockPos());

        if (blockhitresult.getType() != HitResult.Type.BLOCK)
        {   return InteractionResultHolder.pass(itemstack);
        }
        else
        {
            if (lookingAt.getFluidState().isSource() && lookingAt.getFluidState().getType().isSame(Fluids.WATER))
            {
                ItemStack filledWaterskin = getFilled(itemstack, level, blockhitresult.getBlockPos());

                //Replace 1 of the stack with a FilledWaterskinItem
                if (itemstack.getCount() > 1)
                {
                    if (!player.addItem(filledWaterskin))
                    {
                        ItemEntity itementity = player.drop(filledWaterskin, false);
                        if (itementity != null)
                        {   itementity.setNoPickUpDelay();
                            itementity.setThrower(player.getUUID());
                        }
                    }
                    itemstack.shrink(1);
                }
                else
                {   player.setItemInHand(hand, filledWaterskin);
                }
                //Play filling sound
                level.playSound(null, player, SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.PLAYERS, 1, (float) Math.random() / 5 + 0.9f);
                player.swing(hand);

                player.getCooldowns().addCooldown(ModItems.FILLED_WATERSKIN, 10);
                player.getCooldowns().addCooldown(ModItems.WATERSKIN, 10);
            }
            return ar;
        }
    }

    public static ItemStack getFilled(ItemStack stack, Level level, BlockPos pos)
    {
        ItemStack filledWaterskin = ModItems.FILLED_WATERSKIN.getDefaultInstance();
        // copy NBT to new item
        filledWaterskin.setTag(stack.getTag());
        // Set temperature based on temperature of the biome
        filledWaterskin.getOrCreateTag().putDouble("temperature",
                                                   CSMath.clamp((Temperature.getTemperatureAt(pos, level)
                                                           - (CSMath.average(ConfigSettings.MAX_TEMP.get(), ConfigSettings.MIN_TEMP.get()))) * 15, -50, 50));
        // Set purity of water based on water source, if Thirst Was Taken is loaded
        if (CompatManager.isThirstLoaded())
        {   filledWaterskin = CompatManager.setWaterPurity(filledWaterskin, pos, level);
        }
        return filledWaterskin;
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer)
    {   return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {   return slotChanged;
    }
}
