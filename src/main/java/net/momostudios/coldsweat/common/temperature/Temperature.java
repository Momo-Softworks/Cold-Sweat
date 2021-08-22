package net.momostudios.coldsweat.common.temperature;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This is the basis for nearly all things relating to temperature in this mod.
 * While {@code Temperature} is not stored onto the player directly, it is very commonly used for calculations
 *
 * It is highly recommended to use Temperature in your code and convert it to Doubles or Integers when needed via {@code get()}
 */
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
    public Temperature add(double amount)
    {
        return new Temperature(temp + amount);
    }
    /**
     * Adds to the actual value of the temperature
     */
    public Temperature add(Temperature amount)
    {
        return new Temperature(temp + amount.get());
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
        return new Temperature(modTemp);
    }
}
