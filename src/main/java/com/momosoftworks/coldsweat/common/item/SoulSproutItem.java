package com.momosoftworks.coldsweat.common.item;

import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SoulSproutItem extends BlockNamedItem
{
    public SoulSproutItem(Block block, Properties properties)
    {   super(block, properties);
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
}
