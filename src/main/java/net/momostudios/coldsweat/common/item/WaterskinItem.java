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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.BiomeTempModifier;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.util.ModItems;

public class WaterskinItem extends Item
{
    public WaterskinItem()
    {
        super(new Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(16));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entity, Hand hand)
    {
        ActionResult<ItemStack> ar = super.onItemRightClick(world, entity, hand);
        ItemStack itemstack = ar.getResult();

        //Get the block the player is looking at
        Vector3d lookPos = entity.getEyePosition(1f).add(
                entity.getLook(1f).x * 5,
                entity.getLook(1f).y * 5,
                entity.getLook(1f).z * 5);
        BlockState lookingAt = world.getFluidState(world.rayTraceBlocks(new RayTraceContext(entity.getEyePosition(1f), lookPos,
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.SOURCE_ONLY, entity)).getPos()).getBlockState();

        if (lookingAt.getMaterial() == Material.WATER)
        {
            ItemStack filledWaterskin = ModItems.FILLED_WATERSKIN.getDefaultInstance();
            filledWaterskin.getOrCreateTag().putDouble("temperature", (new BiomeTempModifier().calculate(new Temperature(), entity) - 1) * 25);
            //Replace 1 of the stack with a FilledWaterskinItem
            if (itemstack.getCount() > 1)
            {
                if (!entity.addItemStackToInventory(filledWaterskin))
                {
                    ItemEntity itementity = entity.dropItem(filledWaterskin, false);
                    if (itementity != null)
                    {
                        itementity.setNoPickupDelay();
                        itementity.setOwnerId(entity.getUniqueID());
                    }
                }
                itemstack.setCount(itemstack.getCount() - 1);
            }
            else
            {
                entity.setHeldItem(hand, filledWaterskin);
            }
            //Play filling sound
            world.playSound(null, entity.getPosition(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ambient.underwater.enter")),
            SoundCategory.PLAYERS, 1, (float) Math.random() / 5 + 0.9f);
            entity.swingArm(hand);
        }
        return ar;
    }
}
