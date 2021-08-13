package net.momostudios.coldsweat.common.temperature;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.momostudios.coldsweat.core.init.TempModifierInit;
import net.momostudios.coldsweat.core.util.ListNBTHelper;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerTemp
{
    //Returns the player's temperature AFTER the modifiers are calculated.
    public static Temperature getTemperature(PlayerEntity player, Types type)
    {
        return new Temperature(player.getPersistentData().getDouble(getTempTag(type)));
    }



    /**
     * You should try to avoid using these unless you need to set the value to a fixed amount.
     * Use TempModifiers instead.
     */
    public static void setTemperature(PlayerEntity player, Temperature value, Types type)
    {
        player.getPersistentData().putDouble(getTempTag(type), value.get());
    }

    /**
     * Applies the given modifier to the player's temperature directly.
     * This is used for instant temperature-changing items (i.e. Waterskins)
     *
     * @param duplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for pieces of armor that have stacking effects, for example)
     */
    public static void applyModifier(PlayerEntity player, TempModifier modifier, Types type, boolean duplicates, INBT... arguments)
    {
        ListNBT nbt = ListNBTHelper.createIfNull(getModifierTag(type), player);
        if (!ListNBTHelper.doesNBTContain(nbt, modifier) || duplicates)
        {
            ListNBT modifierData = new ListNBT();
            //System.out.println("The modifier is " + modifier + " and the key is " + modifier.getRegistryName());
            modifierData.add(StringNBT.valueOf(modifier.getClass().toString().replaceFirst("class ", "")));

            if (arguments != null)
                for (INBT argument : arguments)
                {
                    modifierData.add(argument);
                    System.out.println(argument);
                }
            nbt.add(modifierData);
        }
        player.getPersistentData().put(getModifierTag(type), nbt);
    }


    /**
     * Gets all TempModifiers of the specified type on the player
     * @param player is the player being sampled
     * @param type determines which TempModifier list to pull from
     * @returns a list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(PlayerEntity player, Types type)
    {
        return ListNBTHelper.getModifierList(ListNBTHelper.createIfNull(getModifierTag(type), player));
    }


    /**
     * Removes the specified number of TempModifiers of the specified type from the player
     * @param player is the player being sampled
     * @param type determines which TempModifier list to pull from
     */
    public static <T> void removeModifier(PlayerEntity player, Class<T> modClass, Types type, int count)
    {
        CompoundNBT nbt = player.getPersistentData();
        List<TempModifier> modifiers = ListNBTHelper.getModifierList(ListNBTHelper.createIfNull(getModifierTag(type), player));

        int modsLeft = 0;
        for (TempModifier modifier : modifiers)
        {
            if (modifier.getClass().equals(modClass) && modsLeft < count)
            {
                modifiers.remove(modifier);
            }
            modsLeft++;
        }
    }


    //Establish the types of temperature
    public enum Types
    {
        AMBIENT,
        BODY,
        BASE,
        RATE,
        COMPOSITE
    }

    public static String getModifierTag(Types type)
    {
        switch (type)
        {
            case BODY : return "body_temp_modifiers";
            case AMBIENT : return "ambient_temp_modifiers";
            case BASE : return "base_temp_modifiers";
            case RATE : return "rate_temp_modifiers";
            default : throw new IllegalArgumentException("PlayerTempHandler.getModifierTag() received illegal Type argument");
        }
    }

    public static String getTempTag(Types type)
    {
        switch (type)
        {
            case BODY : return "body_temperature";
            case AMBIENT : return "ambient_temperature";
            case BASE : return "base_temperature";
            case COMPOSITE : return "composite_temperature";
            default : throw new IllegalArgumentException("PlayerTempHandler.getTempTag() received illegal Type argument");
        }
    }
}
