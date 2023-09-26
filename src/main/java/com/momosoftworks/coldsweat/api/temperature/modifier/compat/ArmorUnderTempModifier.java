package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.jwaresoftware.mcmods.lib.Armory;

import java.util.function.Function;

/**
 * Special TempModifier class for Armor Underwear
 */
public class ArmorUnderTempModifier extends TempModifier
{
    public ArmorUnderTempModifier() {}

    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        CompoundNBT nbt = this.getNBT();
        double bodyTemp = Temperature.get(entity, Temperature.Type.BODY);
        double totalOffset = 0;

        // If this modifier is applied to MIN, get the cold lining; same for hot
        for (ItemStack stack : entity.getArmorSlots())
        {
            switch (type)
            {
                case FREEZING_POINT :
                {   totalOffset += Math.min(0,
                            Armory.getTLining(stack).getModifier() * 5
                            + nbt.getFloat("OzzyTemp"));
                    break;
                }
                case BURNING_POINT :
                {   totalOffset += Math.max(0,
                            Armory.getTLining(stack).getModifier() * 5
                            + nbt.getFloat("OzzyTemp"));
                    break;
                }
            }

            // Special functionality for certain linings
            if (CompatManager.hasOzzyLiner(stack))
            {   nbt.putFloat("OzzyTemp", (float) (CSMath.blend(-10, 10, bodyTemp, -100, 100)));
            }
        }

        double returnTemp = Temperature.convertUnits(totalOffset, Temperature.Units.F, Temperature.Units.MC, false);
        return temp -> returnTemp;
    }

    @Override
    public String getID()
    {
        return "armorunder:lining";
    }
}
