package dev.momostudios.coldsweat.common.entity.data.edible;

import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;

public class HealingEdible extends Edible
{
    @Override
    public int getCooldown()
    {
        return (int) (Math.random() * 20 + 20);
    }

    @Override
    public void onEaten(ChameleonEntity entity, ItemStack stack)
    {
        super.onEaten(entity, stack);
        if (!entity.level.isClientSide)
        {
            entity.heal(6);
            WorldHelper.spawnParticle(entity.level, ParticleTypes.HEART, entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), 0, 0, 0);
        }
    }
}
