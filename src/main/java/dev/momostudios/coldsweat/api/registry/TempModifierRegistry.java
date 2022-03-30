package dev.momostudios.coldsweat.api.registry;

import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;

import java.util.HashMap;
import java.util.Map;

public class TempModifierRegistry
{
    static TempModifierRegistry REGISTRY = new TempModifierRegistry();
    static Map<String, TempModifier> TEMP_MODIFIERS = new HashMap<>();

    public static TempModifierRegistry getRegister() {
        return REGISTRY;
    }

    public final Map<String, TempModifier> getEntries() {
        return TEMP_MODIFIERS;
    }

    public void register(TempModifier modifier)
    {
        TEMP_MODIFIERS.put(modifier.getID(), modifier);
    }

    public static void flush()
    {
        TEMP_MODIFIERS.clear();
    }

    public TempModifier getEntryFor(String id) {
        return getEntries().get(id);
    }
}
