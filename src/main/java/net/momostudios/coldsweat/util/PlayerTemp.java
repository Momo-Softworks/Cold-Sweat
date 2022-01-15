package net.momostudios.coldsweat.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.core.event.csevents.TempModifierEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PlayerTemp
{
    /**
     * Returns the player's temperature AFTER {@link TempModifier}s are calculated.
     */
    public static Temperature getTemperature(PlayerEntity player, Types type)
    {
        return new Temperature(player.getCapability(PlayerTempCapability.TEMPERATURE).orElse(new PlayerTempCapability()).get(type));
    }

    /**
     * Use {@link TempModifier}s for over-time effects.
     */
    public static void setTemperature(PlayerEntity player, Temperature value, Types type)
    {
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(capability ->
        {
            capability.set(type, value.get());
        });
    }

    /**
     * Applies the given modifier to the player's temperature directly.<br>
     * This is used for instant temperature-changing items (i.e. Waterskins)
     *
     * @param duplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for things that have stacking effects, for example)
     */
    public static void addModifier(PlayerEntity player, TempModifier modifier, Types type, boolean duplicates)
    {
        TempModifierEvent.Add event = new TempModifierEvent.Add(modifier, player, type, duplicates);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled())
        {
            player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
            {
                if (TempModifierEntries.getEntries().getMap().containsKey(event.getModifier().getID()))
                {
                    if (!cap.hasModifier(event.type, event.getModifier().getClass()) || event.duplicatesAllowed)
                    {
                        cap.getModifiers(event.type).add(event.getModifier());
                    }
                }
                else
                    ColdSweat.LOGGER.error("TempModifierEvent.Add: No TempModifier with ID " + modifier.getID() + " found!");
            });
            // Update for player's client
            if (player instanceof ServerPlayerEntity)
            {
                PlayerHelper.updateModifiers(player);
            }
        }
    }

    /**
     * Removes the specified number of TempModifiers of the specified type from the player
     * @param player The player being sampled
     * @param type Determines which TempModifier list to pull from
     * @param count The number of modifiers of the given type to be removed (can be higher than the number of modifiers on the player)
     * @param condition The predicate to determine which TempModifiers to remove
     */
    public static void removeModifiers(PlayerEntity player, Types type, int count, Predicate<TempModifier> condition)
    {
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            List<TempModifier> toRemove = new ArrayList<>();
            int count2 = 0;
            for (int i = 0; i < cap.getModifiers(type).size(); i++)
            {
                TempModifier modifier = cap.getModifiers(type).get(i);
                TempModifierEvent.Remove event = new TempModifierEvent.Remove(player, type, count, condition);
                MinecraftForge.EVENT_BUS.post(event);
                if (!event.isCanceled())
                {
                    if (event.getCondition().test(modifier))
                    {
                        toRemove.add(modifier);

                        count2++;
                        if (count2 >= count)
                            break;
                    }
                }
            }
            cap.getModifiers(type).removeAll(toRemove);
        });
        // Update for player's client
        if (player instanceof ServerPlayerEntity)
        {
            PlayerHelper.updateModifiers(player);
        }
    }

    /**
     * Gets all TempModifiers of the specified type on the player
     * @param player is the player being sampled
     * @param type determines which TempModifier list to pull from
     * @returns a NEW list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(PlayerEntity player, Types type)
    {
        List<TempModifier> mods =  player.getCapability(PlayerTempCapability.TEMPERATURE).orElse(new PlayerTempCapability()).getModifiers(type);
        List<TempModifier> toRemove = new ArrayList<>();
        mods.forEach(mod ->
        {
            if (mod == null || mod.getID() == null)
            {
                ColdSweat.LOGGER.error("Found TempModifier with null ID! Removing...");
                toRemove.add(mod);
            }
        });
        mods.removeAll(toRemove);
        return mods;
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param type The type of TempModifier to check for
     * @return true if the player has the specified TempModifier of the specified type
     */
    public static boolean hasModifier(PlayerEntity player, Class<? extends TempModifier> modClass, Types type)
    {
        return player.getCapability(PlayerTempCapability.TEMPERATURE).map(cap -> cap.hasModifier(type, modClass)).orElse(false);
    }

    /**
     * Iterates through all TempModifiers of the specified type on the player
     * @param type determines which TempModifier list to pull from
     * @param action the action(s) to perform on each TempModifier
     */
    public static void forEachModifier(PlayerEntity player, Types type, Consumer<TempModifier> action)
    {
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            if (cap.getModifiers(type) != null)
            {
                cap.getModifiers(type).forEach(action);
            }
        });
    }

    /**
     * Defines all types of temperature in Cold Sweat. <br>
     * These are used to get the player's temperature and/or to apply modifiers to it. <br>
     * <br>
     * {@link #AMBIENT}: The temperature of the area around the player. Should ONLY be changed by TempModifiers. <br>
     * {@link #BODY}: The temperature of the player's body. <br>
     * {@link #BASE}: A static offset applied to the player's body temperature. <br>
     * {@link #COMPOSITE}: The sum of the player's body and base temperatures. (CANNOT be set) <br>
     * {@link #RATE}: Only used by TempModifiers. Affects the rate at which the player's body temperature changes. <br>
     */
    public enum Types
    {
        AMBIENT,
        BODY,
        BASE,
        COMPOSITE,
        RATE
    }

    /**
     * Used for storing TempModifiers in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of TempModifier to be stored
     * @return The NBT tag name for the given type
     */
    public static String getModifierTag(Types type)
    {
        switch (type)
        {
            case BODY :     return "body_temp_modifiers";
            case AMBIENT :  return "ambient_temp_modifiers";
            case BASE :     return "base_temp_modifiers";
            case RATE :     return "rate_temp_modifiers";
            default : throw new IllegalArgumentException("PlayerTempHandler.getModifierTag() received illegal Type argument");
        }
    }

    /**
     * Used for storing Temperature values in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of Temperature to be stored. ({@link Types#AMBIENT} should only be stored when needed to prevent lag)
     * @return The NBT tag name for the given type
     */
    public static String getTempTag(Types type)
    {
        switch (type)
        {
            case BODY :      return "body_temperature";
            case AMBIENT :   return "ambient_temperature";
            case BASE :      return "base_temperature";
            case COMPOSITE : return "composite_temperature";
            default : throw new IllegalArgumentException("PlayerTempHandler.getTempTag() received illegal Type argument");
        }
    }
}
