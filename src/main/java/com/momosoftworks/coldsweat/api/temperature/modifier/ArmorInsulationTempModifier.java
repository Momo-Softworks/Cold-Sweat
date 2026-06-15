package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Function;

/**
 * Applies insulation from worn armor. Renamed from {@code InsulationTempModifier} to match 1.16's
 * {@code ArmorInsulationTempModifier} (registry id {@code cold_sweat:armor}).<br>
 * NOTE: the cold/hot values fed into this modifier still come from the simplified ~2.2-era armor scan;
 * the full capability-driven slot/adaptive system (2.4) is ported in Phase 5 alongside the Sewing Table.
 * This modifier's own math, however, now matches 2.4 verbatim.
 */
public class ArmorInsulationTempModifier extends TempModifier
{
    public ArmorInsulationTempModifier()
    {   this(0d, 0d);
    }

    public ArmorInsulationTempModifier(double cold, double hot)
    {   this.getNBT().setDouble("cold", cold);
        this.getNBT().setDouble("hot", hot);
    }

    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        double cold = this.getNBT().getDouble("cold");
        double hot = this.getNBT().getDouble("hot");
        double insulationStrength = ConfigSettings.INSULATION_STRENGTH.get();

        return temp ->
        {   double insulation = (temp > 0 ? hot : cold) * insulationStrength;
            return insulation >= 0 ? temp * Math.pow(0.1, insulation / 40)
                                   : temp * (-insulation / 20 + 1);
        };
    }

    public String getID()
    {   return "cold_sweat:armor";
    }
}
