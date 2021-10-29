package net.momostudios.coldsweat.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.capabilities.ITemperatureCapability;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.core.event.csevents.TempModifierEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerTemp
{
    /**
     * Returns the player's temperature AFTER the modifiers are calculated.
     */
    public static Temperature getTemperature(PlayerEntity player, Types type)
    {
        AtomicReference<Double> temp = new AtomicReference<>(0.0d);
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(capability ->
        {
            temp.set(capability.get(type));
        });
        return new Temperature(temp.get());
    }

    /**
     * You should try to avoid using these unless you need to set the value to a fixed amount.
     * Use TempModifiers instead.
     */
    public static void setTemperature(PlayerEntity player, Temperature value, Types type)
    {
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(capability ->
        {
            capability.set(type, value.get());
        });
    }

    /**
     * Applies the given modifier to the player's temperature directly.
     * This is used for instant temperature-changing items (i.e. Waterskins)
     *
     * @param duplicates allows or disallows duplicate TempModifiers to be applied
     * @param arguments are stored to NBT, then passed to the {@link TempModifier} when it is called from NBT. The argument(s) MUST be a supported {@link INBT} type.
     * (You might use this for things that have stacking effects, for example)
     */
    public static void applyModifier(PlayerEntity player, TempModifier modifier, Types type, boolean duplicates, INBT... arguments)
    {
        MinecraftForge.EVENT_BUS.post(new TempModifierEvent.Add(modifier, player, type, duplicates, arguments));
    }


    /**
     * Gets all TempModifiers of the specified type on the player
     * @param player is the player being sampled
     * @param type determines which TempModifier list to pull from
     * @returns a NEW list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(PlayerEntity player, Types type)
    {
        return ListNBTHelper.getModifierList(ListNBTHelper.createIfNull(getModifierTag(type), player));
    }


    /**
     * Removes the specified number of TempModifiers of the specified type from the player
     * @param player is the player being sampled
     * @param type determines which TempModifier list to pull from
     * @param count is the number of modifiers of the given type to be removed (use Integer.MAX_VALUE to remove all instances)
     */
    public static void removeModifier(PlayerEntity player, Class<? extends TempModifier> modClass, Types type, int count)
    {
        MinecraftForge.EVENT_BUS.post(new TempModifierEvent.Remove(player, modClass, type, count));
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
