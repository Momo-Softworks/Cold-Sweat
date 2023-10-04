package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.data.tags.ModItemTags;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;

public class HealingEdible extends Edible
{
    @Override
    public int getCooldown()
    {
        return (int) (Math.random() * 20 + 20);
    }

    @Override
    public Result onEaten(Chameleon entity, ItemEntity item)
    {
        if (!entity.level.isClientSide && item.getOwner() != null && entity.isPlayerTrusted(item.getOwner().getUUID()))
        {
            entity.heal(6);
            WorldHelper.spawnParticle(entity.level, ParticleTypes.HEART, entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), 0, 0, 0);
            return Result.SUCCESS;
        }
        return Result.FAIL;
    }

    @Override
    public boolean shouldEat(Chameleon entity, ItemEntity item)
    {   return entity.getHealth() < entity.getMaxHealth() || (item.getOwner() != null && !entity.isPlayerTrusted(item.getOwner().getUUID()));
    }

    @Override
    public TagKey<Item> associatedItems()
    {   return ModItemTags.CHAMELEON_TAMING;
    }
}
