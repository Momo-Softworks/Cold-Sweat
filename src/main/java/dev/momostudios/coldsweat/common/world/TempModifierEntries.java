package dev.momostudios.coldsweat.common.world;

import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;

import java.util.HashMap;
import java.util.Map;

public class TempModifierEntries
{
    static TempModifierEntries master = new TempModifierEntries();
    static Map<String, TempModifier> entries = new HashMap<>();

    public static TempModifierEntries getEntries() {
        return master;
    }

    public final Map<String, TempModifier> getMap() {
        return entries;
    }

    public void add(TempModifier modifier) {
        entries.put(modifier.getID(), modifier);
    }

    public void remove(TempModifier modifier) {
        entries.remove(modifier.getID());
    }

    public void flush() {
        entries.clear();
    }

    public TempModifier getEntryFor(String id) {
        return getMap().get(id);
    }
}
