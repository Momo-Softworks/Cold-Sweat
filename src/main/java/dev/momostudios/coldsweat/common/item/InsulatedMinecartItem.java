package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InsulatedMinecartItem extends Item
{
    public InsulatedMinecartItem(Item.Properties itemProperties)
    {
        super(itemProperties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        World world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(BlockTags.RAILS))
        {
            ItemStack itemstack = context.getItemInHand();
            if (!world.isClientSide)
            {
                MinecartEntity minecart = new MinecartEntity(world, blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D);
                if (itemstack.hasCustomHoverName())
                {   minecart.setCustomName(itemstack.getHoverName());
                }
                minecart.setDisplayBlockState(ModBlocks.MINECART_INSULATION.defaultBlockState());
                minecart.setDisplayOffset(5);
                world.addFreshEntity(minecart);
            }
            itemstack.shrink(1);
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        else return ActionResultType.PASS;
    }
}
