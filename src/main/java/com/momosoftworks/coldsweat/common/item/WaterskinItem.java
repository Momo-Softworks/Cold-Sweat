package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class WaterskinItem extends Item
{
    public WaterskinItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(16));
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand)
    {
        ActionResult<ItemStack> ar = super.use(level, player, hand);
        ItemStack itemstack = ar.getObject();

        BlockRayTraceResult blockhitresult = getPlayerPOVHitResult(level, player, RayTraceContext.FluidMode.SOURCE_ONLY);
        BlockState lookingAt = level.getBlockState(blockhitresult.getBlockPos());

        if (blockhitresult.getType() != RayTraceResult.Type.BLOCK)
        {   return ActionResult.pass(itemstack);
        }
        else
        {
            if (lookingAt.getFluidState().isSource() && lookingAt.getMaterial() == Material.WATER)
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
                            itementity.setOwner(player.getUUID());
                        }
                    }
                    itemstack.shrink(1);
                }
                else
                {   player.setItemInHand(hand, filledWaterskin);
                }
                //Play filling sound
                level.playSound(null, player, SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.PLAYERS, 1, (float) Math.random() / 5 + 0.9f);
                player.swing(hand);

                player.getCooldowns().addCooldown(ModItems.FILLED_WATERSKIN, 10);
                player.getCooldowns().addCooldown(ModItems.WATERSKIN, 10);
            }
            return ar;
        }
    }

    public static ItemStack getFilled(ItemStack stack, World level, BlockPos pos)
    {
        ItemStack filledWaterskin = ModItems.FILLED_WATERSKIN.getDefaultInstance();
        // copy NBT to new item
        filledWaterskin.setTag(stack.getTag());
        // Set temperature based on temperature of the biome
        filledWaterskin.getOrCreateTag().putDouble("temperature",
                                                   CSMath.clamp((Temperature.getTemperatureAt(pos, level)
                                                           - (CSMath.average(ConfigSettings.MAX_TEMP.get(), ConfigSettings.MIN_TEMP.get()))) * 15, -50, 50));
        return filledWaterskin;
    }

    @Override
    public boolean canAttackBlock(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer)
    {   return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {   return slotChanged;
    }
}
