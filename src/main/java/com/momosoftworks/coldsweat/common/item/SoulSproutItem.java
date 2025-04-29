package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.core.init.ModBlocks;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
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
            {   AdvancementHolder seedyPlace = player.getServer().getAdvancements().get(ResourceLocation.withDefaultNamespace("husbandry/plant_seed"));
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
            Level level = source.level();
            Position position = DispenserBlock.getDispensePosition(source);
            BlockPos pos = BlockPos.containing(position);

            if (level.getBlockState(pos).canBeReplaced())
            {
                BlockState state = ModBlocks.SOUL_STALK.get().defaultBlockState();
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
