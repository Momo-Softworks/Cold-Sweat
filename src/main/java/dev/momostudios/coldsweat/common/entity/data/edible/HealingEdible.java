package dev.momostudios.coldsweat.common.entity.data.edible;

import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import dev.momostudios.coldsweat.util.registries.ModTags;
import dev.momostudios.coldsweat.util.world.WorldHelper;
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
    public Result onEaten(ChameleonEntity entity, ItemEntity item)
    {
        if (!entity.level.isClientSide && item.getThrower() != null && entity.isPlayerTrusted(item.getThrower()))
        {
            entity.heal(6);
            WorldHelper.spawnParticle(entity.level, ParticleTypes.HEART, entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), 0, 0, 0);
            return Result.SUCCESS;
        }
        return Result.FAIL;
    }

    @Override
    public boolean shouldEat(ChameleonEntity entity, ItemEntity item)
    {   return entity.getHealth() < entity.getMaxHealth() || (item.getThrower() != null && !entity.isPlayerTrusted(item.getThrower()));
    }

    @Override
    public TagKey<Item> associatedItems()
    {   return ModTags.Items.CHAMELEON_TAMING;
    }
}
