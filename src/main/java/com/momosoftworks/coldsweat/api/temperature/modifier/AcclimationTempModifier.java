package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class AcclimationTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        double minTemp = Temperature.get(entity, Temperature.Trait.FREEZING_POINT);
        double maxTemp = Temperature.get(entity, Temperature.Trait.BURNING_POINT);
        double tempFactor = CSMath.blend(-1, 1, Temperature.get(entity, Temperature.Trait.WORLD), minTemp, maxTemp);
        double acclimateSpeed = (ConfigSettings.ACCLIMATION_SPEED.get() * this.getTickRate()) / 20;

        switch (trait)
        {
            case FREEZING_POINT ->
            {
                Pair<Double, Double> minAcclimationRange = ConfigSettings.MIN_ACCLIMATION_RANGE.get();
                double minAcclimation = this.getNBT().getDouble("MinAcclimation");
                double acclimationDelta = tempFactor < -0.5 ? -acclimateSpeed
                                        : tempFactor < 0.5 ? acclimateSpeed / 2
                                        : acclimateSpeed;

                double lowerBound = minAcclimationRange.getFirst();
                double upperBound = Math.max(minAcclimation, tempFactor > 0.5 ? minAcclimationRange.getSecond() : 0);

                double newMinAcclimation = CSMath.clamp(minAcclimation + acclimationDelta, lowerBound, upperBound);
                this.getNBT().putDouble("MinAcclimation", newMinAcclimation);

                return temp -> temp + this.getNBT().getDouble("MinAcclimation");
            }
            case BURNING_POINT ->
            {
                Pair<Double, Double> maxAcclimationRange = ConfigSettings.MAX_ACCLIMATION_RANGE.get();
                double maxAcclimation = this.getNBT().getDouble("MaxAcclimation");
                double acclimationDelta = tempFactor > 0.5 ? acclimateSpeed
                                        : tempFactor > -0.5 ? -acclimateSpeed / 2
                                        : -acclimateSpeed;

                double lowerBound = Math.min(maxAcclimation, tempFactor < -0.5 ? maxAcclimationRange.getFirst() : 0);
                double upperBound = maxAcclimationRange.getSecond();

                double newMaxAcclimation = CSMath.clamp(maxAcclimation + acclimationDelta, lowerBound, upperBound);
                this.getNBT().putDouble("MaxAcclimation", newMaxAcclimation);

                return temp -> temp + this.getNBT().getDouble("MaxAcclimation");
            }
            default ->
            {   return temp -> temp;
            }
        }
    }
}
