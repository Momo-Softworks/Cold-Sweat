package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;

/**
 * This is the basis of all ways that a Temperature can be changed.
 * For example, biome temperature, time of day, depth, and waterskins are all {@link net.momostudios.coldsweat.common.temperature.modifier.TempModifier}
 * This is basically a simple in-out system. It is given a Temperature, and returns a new Temperature based on the PlayerEntity
 *
 * It is up to you to apply and remove these modifiers manually.
 */
public class TempModifier
{
    /**
     * Determines what the provided temperature would be, given the player it is being applied to
     * @param temp should usually represent the player's body temperature or ambient temperature
     */
    public double calculate(Temperature temp, PlayerEntity player)
    {
        return temp.get();
    }
}
