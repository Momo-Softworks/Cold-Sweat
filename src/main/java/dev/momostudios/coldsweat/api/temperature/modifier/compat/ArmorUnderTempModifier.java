package dev.momostudios.coldsweat.api.temperature.modifier.compat;

import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/**
 * Special TempModifier class for Armor Underwear
 */
public class ArmorUnderTempModifier extends TempModifier
{
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        /*CompoundTag nbt = this.getNBT();
        double bodyTemp = Temperature.get(entity, Temperature.Type.BODY);
        double totalOffset = 0;

        // If this modifier is applied to MIN, get the cold lining; same for hot
        for (ItemStack stack : entity.getArmorSlots())
        {
            switch (type)
            {
                case FLOOR ->
                {   totalOffset += Math.min(0,
                            Armory.getTLining(stack).getModifier() * 5
                            + nbt.getFloat("OzzyTemp"));
                }
                case CEIL ->
                {   totalOffset += Math.max(0,
                            Armory.getTLining(stack).getModifier() * 5
                            + nbt.getFloat("OzzyTemp"));
                }
            }

            // Special functionality for certain linings
            if (CompatManager.hasOzzyLiner(stack))
            {   nbt.putFloat("OzzyTemp", (float) (CSMath.blend(-10, 10, bodyTemp, -100, 100)));
            }
        }

        double returnTemp = CSMath.convertTemp(totalOffset, Temperature.Units.F, Temperature.Units.MC, false);
        return temp -> returnTemp;*/
        return temp -> temp;
    }

    @Override
    public String getID()
    {
        return "armorunder:lining";
    }
}
