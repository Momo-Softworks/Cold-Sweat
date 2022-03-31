package dev.momostudios.coldsweat.api.registry;

import com.google.common.collect.ImmutableMap;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;

import java.util.HashMap;
import java.util.Map;

public class TempModifierRegistry
{
    static Map<String, TempModifier> TEMP_MODIFIERS = new HashMap<>();

    public static ImmutableMap<String, TempModifier> getEntries()
    {
        return ImmutableMap.copyOf(TEMP_MODIFIERS);
    }

    public static void register(TempModifier modifier)
    {
        TEMP_MODIFIERS.put(modifier.getID(), modifier);
    }

    public static void flush()
    {
        TEMP_MODIFIERS.clear();
    }

    public static TempModifier getEntryFor(String id) {
        return TEMP_MODIFIERS.get(id);
    }
}
