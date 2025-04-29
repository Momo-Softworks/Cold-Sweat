package com.momosoftworks.coldsweat.common.capability.temperature;

import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Holds all the information regarding the entity's temperature. <br>
 * This capability isn't used for players (see {@link PlayerTempCap} instead).
 */
public class EntityTempCap extends AbstractTempCap
{
    @Override
    public int getHurtInterval(LivingEntity entity)
    {   return EntityTempManager.hasClimateData(entity) ? 200 : -1;
    }

    @Override
    public void syncValues(LivingEntity entity)
    {
        super.syncValues(entity);
        if (!(entity instanceof PlayerEntity))
        {   this.syncTimer = 40;
        }
    }
}
