package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.common.block.SoulStalkBlock;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SoulSproutItem extends ItemNameBlockItem
{
    public SoulSproutItem(Block block, Properties properties)
    {   super(block, properties);
        DispenserBlock.registerBehavior(this, DISPENSE_BEHAVIOR);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        InteractionResult interactionresult = super.useOn(context);
        if (interactionresult == InteractionResult.CONSUME && context.getPlayer() instanceof ServerPlayer player)
        {
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
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity)
    {   entity.clearFire();
        return super.finishUsingItem(stack, level, entity);
    }

    public static final DispenseItemBehavior DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior()
    {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack)
        {
            if (stack.is(ModItems.SOUL_SPROUT))
            {
                Level level = source.getLevel();
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
