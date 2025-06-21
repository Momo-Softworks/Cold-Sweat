package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.common.block.SoulStalkBlock;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoulSproutItem extends BlockNamedItem
{
    public SoulSproutItem(Block block, Properties properties)
    {   super(block, properties);
        DispenserBlock.registerBehavior(this, DISPENSE_BEHAVIOR);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        ActionResultType interactionresult = super.useOn(context);
        if (interactionresult == ActionResultType.CONSUME && context.getPlayer() instanceof ServerPlayerEntity)
        {
            ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
            // Grant the player the "A Seedy Place" advancement
            if (player.getServer() != null)
            {   Advancement seedyPlace = player.getServer().getAdvancements().getAdvancement(new ResourceLocation("minecraft", "husbandry/plant_seed"));
                if (seedyPlace != null)
                {   player.getAdvancements().award(seedyPlace, "nether_wart");
                }
            }
        }
        return interactionresult;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity)
    {   entity.clearFire();
        return super.finishUsingItem(stack, world, entity);
    }

    public static final IDispenseItemBehavior DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior()
    {
        @Override
        protected ItemStack execute(IBlockSource source, ItemStack stack)
        {
            if (stack.getItem() ==  ModItems.SOUL_SPROUT)
            {
                World level = source.getLevel();
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos frontPos = new BlockPos(DispenserBlock.getDispensePosition(source));
                BlockState frontState = level.getBlockState(frontPos);
                BlockState groundState = level.getBlockState(frontPos.below());

                if (frontState.getMaterial().isReplaceable() && frontState.getFluidState().isEmpty()
                && groundState.is(ModBlockTags.SOUL_STALK_PLACEABLE_ON))
                {
                    level.setBlock(frontPos, ModBlocks.SOUL_STALK.defaultBlockState().setValue(SoulStalkBlock.SECTION, SoulStalkBlock.Section.BUD), 3);
                    this.playAnimation(source, direction);
                    this.playSound(source);
                    stack.shrink(1);
                    return stack;
                }
            }
            return super.execute(source, stack);
        }
    };
}
