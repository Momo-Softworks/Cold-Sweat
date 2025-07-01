package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.BlockTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.compat.CompatManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.valkyrienskies.core.api.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ValkShipBlockTempModifier extends BlockTempModifier
{
    public ValkShipBlockTempModifier() {}

    public ValkShipBlockTempModifier(int range)
    {   super(range);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        List<Function<Double, Double>> shipModifiers = new ArrayList<>();

        World level = entity.level;

        for (Ship ship : VSGameUtilsKt.getShipsIntersecting(level, entity.getBoundingBox().inflate(ConfigSettings.BLOCK_RANGE.get())))
        {
            LivingEntity dummyPlayer = new ArmorStandEntity(EntityType.ARMOR_STAND, level);
            Vector3d translatedPos = CompatManager.Valkyrien.translateToShipCoords(entity.position(), ship).multiply(1, 1, 1);
            dummyPlayer.setPos(translatedPos.x, translatedPos.y, translatedPos.z);
            shipModifiers.add(super.calculate(dummyPlayer, trait));
        }
        return (temp) ->
        {
            for (int i = 0; i < shipModifiers.size(); i++)
            {   temp = shipModifiers.get(i).apply(temp);
            }
            return temp;
        };
    }
}