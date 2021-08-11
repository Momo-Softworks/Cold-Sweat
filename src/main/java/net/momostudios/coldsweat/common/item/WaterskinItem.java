package net.momostudios.coldsweat.common.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.init.ModItems;

public class WaterskinItem extends Item
{
    public WaterskinItem()
    {
        super(new Item.Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(16));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entity, Hand hand)
    {
        ActionResult<ItemStack> ar = super.onItemRightClick(world, entity, hand);
        ItemStack itemstack = ar.getResult();

        //Get the block the player is looking at
        BlockState lookingAt = world.getFluidState(world.rayTraceBlocks(new RayTraceContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(
                entity.getLook(1f).x * 5,
                entity.getLook(1f).y * 5,
                entity.getLook(1f).z * 5),
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.SOURCE_ONLY, entity)).getPos()).getBlockState();

        if (lookingAt.getMaterial() == Material.WATER)
        {
            //Replace 1 of the stack with a FilledWaterskinItem
            if (!entity.addItemStackToInventory(new ItemStack(ModItems.FILLED_WATERSKIN.get(), 1)))
            {
                ItemEntity itementity = entity.dropItem(new ItemStack(ModItems.FILLED_WATERSKIN.get(), 1), false);
                if (itementity != null) {
                    itementity.setNoPickupDelay();
                    itementity.setOwnerId(entity.getUniqueID());
                }
            }
            //Play filling sound
            world.playSound(null, entity.getPosition(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ambient.underwater.enter")),
            SoundCategory.PLAYERS, 1, (float) Math.random() / 5 + 0.9f);
            itemstack.setCount(itemstack.getCount() - 1);
            entity.swingArm(hand);
        }
        return ar;
    }
}
