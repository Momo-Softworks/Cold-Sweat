package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TamingEdible extends Edible
{
    @Override
    public int getCooldown()
    {   return 0;
    }

    @Override
    public Result onEaten(ItemStack item, Chameleon entity, Entity thrower)
    {
        Player player = thrower != null ? entity.level.getPlayerByUUID(thrower.getUUID()) : null;
        if (player != null)
        {
            if (entity.level.isClientSide) return Result.FAIL;
            // For taming
            if (!entity.isPlayerTrusted(player))
            {
                if (player.isCreative() || Math.random() < 0.3)
                {
                    entity.setPersistenceRequired();
                    entity.addTrustedPlayer(thrower.getUUID());
                    this.spawnParticles(entity, ParticleTypes.HEART);
                    return Result.SUCCESS;
                }
                else
                {   this.spawnParticles(entity, ParticleTypes.SMOKE);
                    return Result.FAIL;
                }
            }
            // For breeding & healing
            else
            {
                if (entity.getHealth() < entity.getMaxHealth())
                {   entity.heal(6);
                    WorldHelper.spawnParticle(entity.level, ParticleTypes.HEART, entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), 0, 0, 0);
                }
                else if (entity.canFallInLove())
                {   entity.setInLove(player);
                    this.spawnParticles(entity, ParticleTypes.HEART);
                    return Result.SUCCESS;
                }
            }
        }
        return Result.FAIL;
    }

    @Override
    public boolean shouldEat(ItemStack item, Chameleon entity, Entity thrower)
    {   return !entity.isPlayerTrusted(thrower) || entity.canFallInLove() || entity.getHealth() < entity.getMaxHealth();
    }

    @Override
    public TagKey<Item> associatedItems()
    {   return ModItemTags.CHAMELEON_TAMING;
    }

    private void spawnParticles(Chameleon entity, ParticleOptions particle)
    {   WorldHelper.spawnParticleBatch(entity.level, particle, entity.getBoundingBox().inflate(0.2), 6, 0.01);
    }
}
