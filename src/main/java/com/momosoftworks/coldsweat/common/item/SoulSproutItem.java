package com.momosoftworks.coldsweat.common.item;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class SoulSproutItem extends ItemNameBlockItem
{
    public SoulSproutItem(Block block, Properties properties)
    {   super(block, properties);
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
}
