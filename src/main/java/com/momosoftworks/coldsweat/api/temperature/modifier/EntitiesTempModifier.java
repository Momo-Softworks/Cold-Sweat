package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Pair;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Nearby entities (defined in config) radiate temperature to this entity, falling off with distance.<br>
 * Simplified 1.7 port of 1.16's {@code EntitiesTempModifier}.
 */
public class EntitiesTempModifier extends TempModifier
{
    public EntitiesTempModifier() {}

    @Override
    protected Function<Double, Double> calculate(EntityLivingBase affectedEnt, Temperature.Type type)
    {
        Map<String, Pair<Double, Double>> climateMap = ConfigSettings.ENTITY_CLIMATE_TEMPS.get();
        if (climateMap.isEmpty()) return temp -> temp;

        World world = affectedEnt.worldObj;
        AxisAlignedBB aabb = affectedEnt.boundingBox.expand(8, 8, 8);
        @SuppressWarnings("unchecked")
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(affectedEnt, aabb);

        double total = 0;
        int count = 0;
        for (Entity nearby : entities)
        {
            // Limit tested entities to 10 (matches 1.16)
            if (count >= 10) break;
            count++;

            String id = EntityList.getEntityString(nearby);
            if (id == null) continue;
            Pair<Double, Double> data = climateMap.get(ConfigHelper.normalizeEntityId(id));
            if (data == null) continue;

            double range = data.getSecond();
            double distance = affectedEnt.getDistanceToEntity(nearby);
            // Falls off linearly from full strength at distance 0 to nothing at "range"
            total += CSMath.blend(data.getFirst(), 0, distance, 0, range);
        }
        final double finalTemp = total;
        return temp -> temp + finalTemp;
    }

    public String getID()
    {   return "cold_sweat:entities";
    }
}
