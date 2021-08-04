package net.momostudios.coldsweat.temperature;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.momostudios.coldsweat.temperature.modifier.TempModifier;

import javax.annotation.Nonnull;
import java.util.List;

public class Temperature
{
    // Internal variable representing the actual value of the Temperature
    double temp = 0;

    /**
    * Defines an instance of the class with a custom initial double value
    */
    public Temperature(double tempIn)
    {
        temp = tempIn;
    }
    /**
    * Defines an instance of the class with a default value of 0
    */
    public Temperature()
    {
        this(0);
    }


    /**
    * Sets the actual value of the temperature
    */
    public void set(double amount)
    {
        temp = amount;
    }

    /**
    * Adds to the actual value of the temperature
    */
    public void add(double amount)
    {
        temp += amount;
    }

    /**
    * Returns a double representing the actual value of the Temperature
    */
    public double get()
    {
        return temp;
    }

    /**
     * Returns a double representing what the Temperature would be after a TempModifier is applied.
     * @param player MUST NOT be null
     * @param modifier is not applied to the player's Temperature
     */
    public Temperature with(@Nonnull TempModifier modifier, @Nonnull PlayerEntity player)
    {
        PlayerTempHandler pth = new PlayerTempHandler();
        return new Temperature(temp + modifier.calculate(new Temperature(temp), player));
    }

    /**
     * Returns a double representing what the Temperature would be after a list of TempModifier(s) are applied.
     * @param player MUST NOT be null
     * @param modifiers are not applied to the player's Temperature
     */
    public Temperature with(@Nonnull List<TempModifier> modifiers, @Nonnull PlayerEntity player)
    {
        double modTemp = temp;
        if (!modifiers.isEmpty())
        {
            for (TempModifier modifier : modifiers)
            {
                modTemp = modifier.calculate(new Temperature(modTemp), player);
            }
        }
        return new Temperature(temp + modTemp);
    }
}
