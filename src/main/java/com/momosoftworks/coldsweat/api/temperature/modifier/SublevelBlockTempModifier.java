package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Applies the temperature of blocks that are part of "sublevels": movable block structures implemented by mods
 * like Valkyrien Skies, whose blocks are stored in a faraway region of the level and moved/rotated dynamically.<br>
 * <br>
 * Sublevel blocks don't align with the world grid, so scanning them from world space would skip over some of them.
 * Instead, the entity's position is translated into each sublevel's space and the scan is performed there.
 */
public class SublevelBlockTempModifier extends BlockTempModifier
{
    public SublevelBlockTempModifier() {}

    public SublevelBlockTempModifier(int range)
    {   super(range);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        Level level = entity.level();
        int range = this.getNBT().contains("RangeOverride", 3) ? this.getNBT().getInt("RangeOverride") : ConfigSettings.BLOCK_RANGE.get();

        Collection<AABB> sublevelAreas = WorldHelper.worldToSublevel(level, AABB.ofSize(entity.getBoundingBox().getCenter(), range*2, range*2, range*2));
        if (sublevelAreas.isEmpty())
        {   return temp -> temp;
        }

        List<Function<Double, Double>> sublevelModifiers = new ArrayList<>(sublevelAreas.size());
        // Use a non-player dummy; Valkyrien Skies cancels Player#setPosRaw() in the shipyard,
        // moving the player to the ship's world position instead
        LivingEntity dummy = WorldHelper.getDummyEntity(level);
        for (AABB area : sublevelAreas)
        {
            // The transformed area is centered on the entity's position in sublevel space
            Vec3 center = area.getCenter();
            dummy.setPos(center.x, center.y, center.z);
            sublevelModifiers.add(super.calculate(dummy, trait));
        }

        return temp ->
        {
            for (Function<Double, Double> modifier : sublevelModifiers)
            {   temp = modifier.apply(temp);
            }
            return temp;
        };
    }
}
