package net.momostudios.coldsweat.temperature;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.momostudios.coldsweat.nbt.ListNBTHelper;
import net.momostudios.coldsweat.nbt.ObjectNBT;
import net.momostudios.coldsweat.temperature.capabilities.TemperatureCapability;
import net.momostudios.coldsweat.temperature.capabilities.TempModifiersCapability;
import net.momostudios.coldsweat.temperature.modifier.TempModifier;

import java.util.List;

public class PlayerTempHandler
{
    //Returns the player's temperature AFTER the modifiers are calculated.
    public static Temperature getPlayerBodyTemp(PlayerEntity player)
    {
        return player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null).getCoreTemperature();
    }
    public static Temperature getPlayerAmbientTemp(PlayerEntity player)
    {
        return player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).orElse(null).getAmbientTemperature();
    }

    //Returns the player's temperature BEFORE the modifiers are calculated.
    public static Temperature getPlayerBaseTemp(PlayerEntity player)
    {
        return new Temperature(player.getPersistentData().getDouble("tempBase"));
    }

    /**
     * You should try to avoid using these unless you need to set the value to a fixed amount.
     * Use TempModifiers instead.
     */
    public static void setPlayerBodyTemp(PlayerEntity player, Temperature value)
    {
        player.getPersistentData().putDouble("body_temperature", value.get());
    }
    public static void setPlayerAmbientTemp(PlayerEntity player, Temperature value)
    {
        player.getPersistentData().putDouble("ambient_temperature", value.get());
    }
    public static void setPlayerBaseTemp(PlayerEntity player, Temperature value)
    {
        player.getPersistentData().putDouble("base_temperature", value.get());
    }



    /**
     * Applies the given modifier to the rate at which the player's temperature changes.
     */
    public static void applyToRate(PlayerEntity player, TempModifier modifier)
    {

    }

    /**
     * Applies the given modifier to the player's temperature directly.
     * This is used for instant temperature-changing items (i.e. Waterskins)
     */
    public static void applyToPlayer(PlayerEntity player, TempModifier modifier)
    {
        INBT nbt = player.getPersistentData().get("body_temp_modifiers");
        if (nbt != null)
        {
            if (nbt instanceof ListNBT && !ListNBTHelper.doesNBTContain(((ListNBT) nbt), modifier))
            {
                ((ListNBT) nbt).add(new ObjectNBT(modifier));
            }
        }
        else nbt = new ListNBT();
        player.getPersistentData().put("body_temp_modifiers", nbt);
    }

    /**
     * Applies the given modifier to the player's BASE temperature.
     * This can be used to adjust the player's core temperature by a fixed amount
     *
     */
    public static void applyToPlayerBase(PlayerEntity player, TempModifier modifier)
    {
        setPlayerBaseTemp(player, new Temperature(getPlayerBaseTemp(player).get() + modifier.calculate(getPlayerBodyTemp(player), player)));
    }

    /**
     * Applies the given modifier to the overall biome temperature.
     * This is the temperature that's displayed next to the hotbar.
     */
    public static void applyToWorld(PlayerEntity player, TempModifier modifier)
    {
        INBT nbt = player.getPersistentData().get("ambient_temp_modifiers");
        if (nbt != null)
        {
            if (nbt instanceof ListNBT && !ListNBTHelper.doesNBTContain(((ListNBT) nbt), modifier))
            {
                ((ListNBT) nbt).add(new ObjectNBT(modifier));
            }
        }
        else nbt = new ListNBT();
        player.getPersistentData().put("ambient_temp_modifiers", nbt);
    }
}
