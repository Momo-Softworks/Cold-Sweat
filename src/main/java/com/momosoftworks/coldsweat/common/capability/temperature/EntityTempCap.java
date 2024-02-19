package com.momosoftworks.coldsweat.common.capability.temperature;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
        if (!(entity instanceof Player))
        {   this.syncTimer = 40;
        }
    }
}
