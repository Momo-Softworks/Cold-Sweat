package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.Function;

public class FoodTempModifier extends TempModifier
{
    public FoodTempModifier()
    {
        this(0);
    }

    public FoodTempModifier(double temperature)
    {
        this.getNBT().putDouble("temperature", temperature);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (this.isOverridden(trait))
        {   return temp -> temp;
        }
        return temp -> temp + this.getNBT().getDouble("temperature");
    }

    public boolean isOverridden(Temperature.Trait trait)
    {   return this.getNBT().getCompound("Overridden").getInt(trait.getSerializedName()) > 0;
    }

    private void incrementOverridden(Temperature.Trait trait, boolean add)
    {
        if (!this.getNBT().contains("Overridden"))
        {   this.getNBT().put("Overridden", new CompoundNBT());
        }
        NBTHelper.incrementTag(this.getNBT().getCompound("Overridden"), trait.getSerializedName(), add ? 1 : -1, o -> true);
    }

    private void updateOverridden(LivingEntity entity, Temperature.Trait trait, TempModifier sibling, boolean add)
    {
        double temperature = this.getNBT().getDouble("temperature");
        if (sibling instanceof FoodTempModifier)
        {
            double siblingTemp = sibling.getNBT().getDouble("temperature");
            if (CSMath.sign(temperature) == CSMath.sign(siblingTemp) && Math.abs(siblingTemp) > Math.abs(temperature))
            {   this.incrementOverridden(trait, add);
                this.markDirty();
            }
            this.update(this.getLastInput(trait), entity, trait);
        }
    }

    @Override
    public void onSiblingAdded(LivingEntity entity, Temperature.Trait trait, TempModifier sibling)
    {   this.updateOverridden(entity, trait, sibling, true);
    }

    @Override
    public void onAdded(LivingEntity entity, Temperature.Trait trait)
    {   Temperature.getModifiers(entity, trait).forEach(mod -> this.updateOverridden(entity, trait, mod, true));
    }

    @Override
    public void onSiblingRemoved(LivingEntity entity, Temperature.Trait trait, TempModifier sibling)
    {   this.updateOverridden(entity, trait, sibling, false);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof TempModifier
        && this.getClass().equals(((TempModifier) obj).getClass()))
        {
            CompoundNBT thisNBT = this.getNBT();
            thisNBT.remove("Overridden");
            CompoundNBT otherNBT = ((TempModifier) obj).getNBT();
            otherNBT.remove("Overridden");
            return otherNBT.equals(thisNBT);
        }
        return false;
    }
}
