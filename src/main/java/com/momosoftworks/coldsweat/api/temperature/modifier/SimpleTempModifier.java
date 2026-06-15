package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A generic temperature modifier that applies a static temperature change based on its NBT.<br>
 * Useful for commands. Ported from 1.16's {@code SimpleTempModifier}.
 */
public class SimpleTempModifier extends TempModifier
{
    public SimpleTempModifier()
    {   this(0, Operation.ADD);
    }

    public SimpleTempModifier(double temperature, Operation operation)
    {   this.setTemperature(temperature);
        this.setOperation(operation);
    }

    @Override
    protected Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {   return temp -> this.getOperation().apply(temp, this.getTemperature());
    }

    public double getTemperature()
    {   return this.getNBT().getDouble("Temperature");
    }

    public void setTemperature(double temperature)
    {   this.getNBT().setDouble("Temperature", temperature);
        this.markDirty();
    }

    public Operation getOperation()
    {   return Operation.byName(this.getNBT().getString("Operation"));
    }

    public void setOperation(Operation operation)
    {   this.getNBT().setString("Operation", operation.getSerializedName());
        this.markDirty();
    }

    public String getID()
    {   return "cold_sweat:simple";
    }

    public enum Operation implements StringRepresentable
    {
        ADD("add", (temp, val) -> temp + val),
        SUBTRACT("subtract", (temp, val) -> temp - val),
        MULTIPLY("multiply", (temp, val) -> temp * val),
        DIVIDE("divide", (temp, val) -> val == 0 ? temp : temp / val),
        POWER("power", Math::pow),
        ROOT("root", (temp, val) -> val == 0 ? temp : Math.pow(temp, 1 / val)),
        SET("set", (temp, val) -> val),
        MAX("max", Math::max),
        MIN("min", Math::min);

        private final BiFunction<Double, Double, Double> function;
        private final String name;

        Operation(String name, BiFunction<Double, Double, Double> function)
        {   this.name = name;
            this.function = function;
        }

        public BiFunction<Double, Double, Double> function()
        {   return function;
        }

        public double apply(double temp, double value)
        {   return function.apply(temp, value);
        }

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Operation byName(String name)
        {   return EnumHelper.byName(values(), name);
        }
    }
}
