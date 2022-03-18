package dev.momostudios.coldsweat.api.registry;

import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;

import java.util.HashMap;
import java.util.Map;

public class TempModifierRegistry
{
    static TempModifierRegistry register = new TempModifierRegistry();
    static Map<String, TempModifier> entries = new HashMap<>();

    public static TempModifierRegistry getRegister() {
        return register;
    }

    public final Map<String, TempModifier> getEntries() {
        return entries;
    }

    public void register(TempModifier modifier)
    {
        entries.put(modifier.getID(), modifier);
    }

    /**
     * This should rarely be used, as some mods might rely on TempModifiers being registered.
     */
    public void unregister(String id)
    {
        entries.remove(id);
    }

    public static void flush()
    {
        entries.clear();
    }

    public TempModifier getEntryFor(String id) {
        return getEntries().get(id);
    }
}
