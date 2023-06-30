package dev.momostudios.coldsweat.api.registry;

import com.google.common.collect.ImmutableMap;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TempModifierRegistry
{
    static Map<String, Supplier<TempModifier>> TEMP_MODIFIERS = new HashMap<>();

    public static ImmutableMap<String, Supplier<TempModifier>> getEntries()
    {
        return ImmutableMap.copyOf(TEMP_MODIFIERS);
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

    public static void register(TempModifier modifier)
    {   register(() -> modifier);
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
     * If the TempModifier class does not have a default constructor (which should never happen), a generic one is given.
     */
    @Nullable
    public static TempModifier getEntryFor(String id)
    {
        return TEMP_MODIFIERS.getOrDefault(id, () ->
        {   ColdSweat.LOGGER.error("Tried to instantiate TempModifier \"" + id + "\", but it is not registered!");
            return null;
        }).get();
    }
}
