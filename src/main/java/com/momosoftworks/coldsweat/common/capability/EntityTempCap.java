package com.momosoftworks.coldsweat.common.capability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Holds all the information regarding the entity's temperature. <br>
 * This capability isn't used for players (see {@link PlayerTempCap} instead).
 */
public class EntityTempCap extends AbstractTempCap
{
    @Override
    public void tickHurting(LivingEntity entity, double heatResistance, double coldResistance)
    {}

    @Override
    public void syncValues(LivingEntity entity)
    {
        super.syncValues(entity);
        if (!(entity instanceof PlayerEntity))
        {   this.syncTimer = 40;
        }
    }
}
