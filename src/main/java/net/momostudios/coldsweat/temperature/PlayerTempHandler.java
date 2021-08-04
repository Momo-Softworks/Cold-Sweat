package net.momostudios.coldsweat.temperature;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.temperature.capabilities.TemperatureCapability;
import net.momostudios.coldsweat.temperature.capabilities.TempModifiersCapability;
import net.momostudios.coldsweat.temperature.modifier.TempModifier;

import java.util.List;

public class PlayerTempHandler
{
    //Returns the player's temperature AFTER the modifiers are calculated.
    public Temperature getPlayerBodyTemp(PlayerEntity player)
    {
        return player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null).getCoreTemperature();
    }
    public Temperature getPlayerAmbientTemp(PlayerEntity player)
    {
        return player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null).getAmbientTemperature();
    }

    //Returns the player's temperature BEFORE the modifiers are calculated.
    public Temperature getPlayerBaseTemp(PlayerEntity player)
    {
        return new Temperature(player.getPersistentData().getDouble("tempBase"));
    }

    /**
     * YOU SHOULD NOT USE THESE!
     * Use TempModifiers instead of outright setting the player's temperature.
     */
    public void setPlayerBodyTemp(PlayerEntity player, double value)
    {
        if (player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null) != null)
        player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null).setCoreTemperature(new Temperature(value));
    }
    public void setPlayerAmbientTemp(PlayerEntity player, double value)
    {
        if (player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null) != null)
        player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null).setAmbientTemperature(new Temperature(value));
    }


    /**
     * Applies the given modifier to the rate at which the player's temperature changes.
     */
    public void applyToRate(PlayerEntity player, TempModifier modifier)
    {

    }

    /**
     * Applies the given modifier to the player's temperature directly.
     * This is used for instant temperature-changing items (i.e. Waterskins)
     */
    public void applyToPlayer(PlayerEntity player, TempModifier modifier)
    {
        Temperature temp = player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null).getCoreTemperature();
        player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null).setCoreTemperature(temp.with(modifier, player));
    }

    /**
     * Applies the given modifier to the player's BASE temperature.
     * This can be used to adjust the player's core temperature by a fixed amount
     */
    public void applyToPlayerBase(PlayerEntity player, TempModifier modifier)
    {
        player.getPersistentData().putDouble("tempBase", modifier.calculate(getPlayerBodyTemp(player), player));
    }

    /**
     * Applies the given modifier to the overall biome temperature.
     * This is the temperature that's displayed next to the hotbar.
     */
    public void applyToWorld(PlayerEntity player, TempModifier modifier)
    {
        TempModifiersCapability.Data temp = player.getCapability(TempModifiersCapability.CAPABILITY_TEMP_MODIFIERS).orElse(null);
        temp.add(modifier);
    }
}
