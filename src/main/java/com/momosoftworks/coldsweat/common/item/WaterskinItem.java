package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import com.momosoftworks.coldsweat.util.world.BlockState;
import com.momosoftworks.coldsweat.util.world.ItemHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class WaterskinItem extends Item
{
    public WaterskinItem()
    {}

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        ItemStack result = stack;
        MovingObjectPosition ar = this.getMovingObjectPositionFromPlayer(world, player, false);
        if (ar == null || ar.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
        {   return stack;
        }

        BlockPos hitPos = new BlockPos(ar.blockX, ar.blockY, ar.blockZ).offset(ar.sideHit);
        BlockState lookingAt = WorldHelper.getBlockState(world, hitPos);
        if (lookingAt.getBlock().getMaterial() == Material.water)
        {
            ItemStack filledWaterskin = new ItemStack(ModItems.FILLED_WATERSKIN);
            filledWaterskin.setTagCompound(ItemHelper.getOrCrateTag(stack));
            filledWaterskin.getTagCompound().setDouble("temperature",
                                                                  CSMath.clamp((Temperature.getTemperatureAt(hitPos, world)
                                                                    - (CSMath.average(ConfigSettings.MAX_TEMP.get(), ConfigSettings.MIN_TEMP.get()))) * 15, -50, 50));

            //Replace 1 of the stack with a FilledWaterskinItem
            if (stack.stackSize > 1)
            {
                if (!player.inventory.addItemStackToInventory(filledWaterskin))
                {
                    EntityItem itementity = player.entityDropItem(filledWaterskin, player.getEyeHeight());
                    if (itementity != null)
                    {   itementity.delayBeforeCanPickup = 0;
                    }
                }
                stack.stackSize--;
            }
            else
            {
                player.setCurrentItemOrArmor(0, result = filledWaterskin);
            }
            //Play filling sound
            world.playSoundEffect(hitPos.getX(), hitPos.getY(), hitPos.getZ(), Blocks.water.stepSound.getStepResourcePath(), 1, (float) Math.random() / 5 + 0.9f);
            player.swingItem();
            return result;
        }
        return stack;
    }
}
