package net.momostudios.coldsweat.temperature;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.momostudios.coldsweat.nbt.ListNBTHelper;
import net.momostudios.coldsweat.temperature.modifier.TempModifier;
import net.minecraft.nbt.StringNBT;

import java.util.Arrays;
import java.util.List;

public class PlayerTempHandler
{
    //Returns the player's temperature AFTER the modifiers are calculated.
    public static Temperature getBody(PlayerEntity player)
    {
        return new Temperature(player.getPersistentData().getDouble("body_temperature"));
    }
    public static Temperature getAmbient(PlayerEntity player)
    {
        return new Temperature(player.getPersistentData().getDouble("ambient_temperature"));
    }

    //Returns the player's temperature BEFORE the modifiers are calculated.
    public static Temperature getBase(PlayerEntity player)
    {
        return new Temperature(player.getPersistentData().getDouble("base_temperature"));
    }

    /**
     * You should try to avoid using these unless you need to set the value to a fixed amount.
     * Use TempModifiers instead.
     */
    public static void setBody(PlayerEntity player, Temperature value)
    {
        player.getPersistentData().putDouble("body_temperature", value.get());
    }
    public static void setAmbient(PlayerEntity player, Temperature value)
    {
        player.getPersistentData().putDouble("ambient_temperature", value.get());
    }
    public static void setBase(PlayerEntity player, Temperature value)
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
     *
     * @param duplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for pieces of armor that have stacking effects, for example)
     */
    public static void applyToPlayer(PlayerEntity player, TempModifier modifier, boolean duplicates)
    {
        ListNBT nbt = ListNBTHelper.createIfNull("body_temp_modifiers", player);
        if (!ListNBTHelper.doesNBTContain(nbt, modifier) || duplicates)
        {
            String modToAdd = modifier.getClass().toString().replaceFirst("class ", "");
            nbt.add(StringNBT.valueOf(modToAdd));
        }
        player.getPersistentData().put("body_temp_modifiers", nbt);
    }

    /**
     * Applies the given modifier to the player's BASE temperature.
     * This can be used to adjust the player's core temperature by a fixed amount
     *
     * @param duplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for pieces of armor that have stacking effects, for example)
     */
    public static void applyToBase(PlayerEntity player, TempModifier modifier, boolean duplicates)
    {
        setBase(player, new Temperature(getBase(player).get() + modifier.calculate(getBody(player), player)));
    }

    /**
     * Applies the given modifier to the overall biome temperature.
     * This is the temperature that's displayed next to the hotbar.
     *
     * @param duplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for pieces of armor that have stacking effects, for example)
     */
    public static void applyToAmbient(PlayerEntity player, TempModifier modifier, boolean duplicates)
    {
        ListNBT nbt = ListNBTHelper.createIfNull("ambient_temp_modifiers", player);
        if (!ListNBTHelper.doesNBTContain(nbt, modifier) || duplicates)
        {
            String modToAdd = modifier.getClass().toString().replaceFirst("class ", "");
            nbt.add(StringNBT.valueOf(modToAdd));
        }
        player.getPersistentData().put("ambient_temp_modifiers", nbt);
    }

    public static List<TempModifier> getAmbientModifiers(PlayerEntity player)
    {
        return ListNBTHelper.asList(ListNBTHelper.createIfNull("ambient_temp_modifiers", player), TempModifier.class);
    }
}
