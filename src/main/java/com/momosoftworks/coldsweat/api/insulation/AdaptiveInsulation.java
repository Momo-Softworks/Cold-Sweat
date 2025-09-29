package com.momosoftworks.coldsweat.api.insulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdaptiveInsulation extends Insulation
{
    public static final Codec<AdaptiveInsulation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("value").forGetter(AdaptiveInsulation::getInsulation),
            Codec.DOUBLE.optionalFieldOf("factor", 0d).forGetter(AdaptiveInsulation::getFactor),
            Codec.DOUBLE.fieldOf("adapt_speed").forGetter(AdaptiveInsulation::getSpeed)
    ).apply(instance, AdaptiveInsulation::new));

    private final double insulation;
    private final double speed;
    private double factor;

    public AdaptiveInsulation(double insulation, double speed)
    {   this(insulation, 0, speed);
    }

    public AdaptiveInsulation(double insulation, double factor, double speed)
    {   this.insulation = insulation;
        this.factor = factor;
        this.speed = speed;
    }

    public static double calculateChange(AdaptiveInsulation insulation, double worldTemp, double minTemp, double maxTemp)
    {
        double factor = insulation.getFactor();
        double adaptSpeed = insulation.getSpeed();

        double newFactor;
        double tempFactor = CSMath.blend(-1, 1, worldTemp, minTemp, maxTemp);
        if (CSMath.betweenInclusive(tempFactor, -0.5, 0.5))
        {   newFactor = CSMath.shrink(factor, adaptSpeed);
        }
        else
        {
            if (CSMath.sign(factor) != CSMath.sign(tempFactor))
            {   adaptSpeed *= 2;
            }
            newFactor = CSMath.clamp(factor + adaptSpeed * CSMath.sign(tempFactor), -1, 1);
        }
        return newFactor;
    }

    public static double getFactorFromArmor(ItemStack stack)
    {   return NBTHelper.getTagOrEmpty(stack).getDouble("InsulationAdaptation");
    }
    public static void setFactorToArmor(ItemStack stack, double factor)
    {   NBTHelper.getTagOrEmpty(stack).putDouble("InsulationAdaptation", factor);
    }
    public static void readFactorFromArmor(AdaptiveInsulation insulation, ItemStack stack)
    {
        double storedFactor = getFactorFromArmor(stack);
        if (storedFactor != 0)
        {   insulation.setFactor(storedFactor);
        }
    }

    @Override
    public double getValue()
    {   return insulation;
    }

    public double getInsulation()
    {   return insulation;
    }

    public double getFactor()
    {   return factor;
    }

    public void setFactor(double factor)
    {   this.factor = factor;
    }

    public double getSpeed()
    {   return speed;
    }

    @Override
    public double getCold()
    {   return CSMath.blend(insulation * 0.75, 0, factor, -1, 1);
    }

    @Override
    public double getHeat()
    {   return CSMath.blend(0, insulation * 0.75, factor, -1, 1);
    }

    @Override
    public <T extends Insulation> T copy()
    {   return (T) new AdaptiveInsulation(insulation, factor, speed);
    }

    @Override
    public boolean isEmpty()
    {   return insulation == 0;
    }

    @Override
    public List<Insulation> split()
    {
        List<Insulation> insulation = new ArrayList<>();
        for (int i = 0; i < CSMath.ceil(Math.abs(this.insulation) / 2); i++)
        {   double insul = CSMath.minAbs(CSMath.shrink(this.insulation, i * 2), 2 * CSMath.sign(this.insulation));
            insulation.add(new AdaptiveInsulation(insul, factor, speed));
        }
        return insulation;
    }

    @Override
    public String toString()
    {   return "AdaptiveInsulation{" + "insulation=" + insulation + ", factor=" + factor + ", speed=" + speed + '}';
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        return obj instanceof AdaptiveInsulation insul
            && insulation == insul.insulation
            && factor == insul.factor
            && speed == insul.speed;
    }
}
