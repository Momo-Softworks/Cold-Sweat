package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class SublevelBlockTempModifier extends BlockTempModifier
{
    public SublevelBlockTempModifier() {}

    public SublevelBlockTempModifier(int range)
    {   super(range);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        World level = entity.level;
        int range = this.getNBT().contains("RangeOverride", 3) ? this.getNBT().getInt("RangeOverride") : ConfigSettings.BLOCK_RANGE.get();

        Collection<AxisAlignedBB> sublevelAreas = WorldHelper.worldToSublevel(level, AxisAlignedBB.ofSize(range*2, range*2, range*2).move(entity.getBoundingBox().getCenter()));
        if (sublevelAreas.isEmpty())
        {   return temp -> temp;
        }

        List<Function<Double, Double>> sublevelModifiers = new ArrayList<>(sublevelAreas.size());
        LivingEntity dummy = WorldHelper.getDummyEntity(level);
        for (AxisAlignedBB area : sublevelAreas)
        {
            // The transformed area is centered on the entity's position in sublevel space
            Vector3d center = area.getCenter();
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
