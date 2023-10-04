package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

import java.util.Random;
import java.util.function.Function;

public class SoulSproutTempModifier extends TempModifier
{

    @Override
    protected Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        if (Math.random() < 0.3 && entity.ticksExisted % 5 == 0)
        {   Random rand = new Random();
            entity.worldObj.spawnParticle("magicCrit", entity.posX + rand.nextDouble() - 0.5, entity.posY + rand.nextDouble() * entity.height, entity.posZ + rand.nextDouble() - 0.5, 0, 0, 0);
        }
        return temp -> temp - 20;
    }

    @Override
    public String getID()
    {
        return "cold_sweat:soul_sprout";
    }
}
