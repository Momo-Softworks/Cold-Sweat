package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
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
            World level = source.getLevel();
            IPosition position = DispenserBlock.getDispensePosition(source);
            BlockPos pos = new BlockPos(position);

            if (level.getBlockState(pos).getMaterial().isReplaceable())
            {
                BlockState state = ModBlocks.SOUL_STALK.defaultBlockState();
                if (state.canSurvive(level, pos))
                {
                    level.setBlock(pos, state, 3);
                    stack.shrink(1);
                    return stack;
                }
            }
            return super.execute(source, stack);
        }
    };
}
