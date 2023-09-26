package com.momosoftworks.coldsweat.api.registry;

import com.google.common.collect.ImmutableMap;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class TempModifierRegistry
{
    static Map<String, Supplier<TempModifier>> TEMP_MODIFIERS = new HashMap<>();

    public static ImmutableMap<String, Supplier<TempModifier>> getEntries()
    {   return ImmutableMap.copyOf(TEMP_MODIFIERS);
    }

    public static void register(Supplier<TempModifier> supplier)
    {
        TempModifier modifier = supplier.get();
        if (TEMP_MODIFIERS.containsKey(modifier.getID()))
        {
            ColdSweat.LOGGER.error("""
                                   Found duplicate TempModifier entries:
                                   {} ({})
                                   {} ({})""", modifier.getClass().getName(), modifier.getID(),
                                   TEMP_MODIFIERS.get(modifier.getID()).getClass().getName(), modifier.getID());
            throw new RuntimeException("A TempModifier with the ID \"" + modifier.getID() + "\" already exists!");
        }
        TEMP_MODIFIERS.put(modifier.getID(), supplier);
    }

    /**
     * Clears the registry of all items. This effectively "un-registers" all TempModifiers.
     */
    public static void flush()
    {
        TEMP_MODIFIERS.clear();
    }

    /**
     * Returns a new instance of the TempModifier with the given ID.<br>
     * If a TempModifier with this ID is not in the registry, this method returns null and logs an error.<br>
     */
    public static Optional<TempModifier> getEntryFor(String id)
    {
        Supplier<TempModifier> mod = TEMP_MODIFIERS.get(id);
        if (mod != null)
        {   return Optional.of(mod.get());
        }
        else
        {   ColdSweat.LOGGER.error("Tried to instantiate TempModifier \"" + id + "\", but it is not registered!");
            return Optional.empty();
        }
    }
}
