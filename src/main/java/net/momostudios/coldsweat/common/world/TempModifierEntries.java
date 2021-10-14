package net.momostudios.coldsweat.common.world;

import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;

import java.util.ArrayList;
import java.util.List;

public class TempModifierEntries
{
    static TempModifierEntries master = new TempModifierEntries();
    static List<TempModifier> entries = new ArrayList<>();

    public static TempModifierEntries getEntries()
    {
        return master;
    }

    public List<TempModifier> getList()
    {
        return new ArrayList<>(entries);
    }

    public void add(TempModifier modifier)
    {
        entries.add(modifier);
    }

    public void remove(TempModifier modifier)
    {
        entries.remove(modifier);
    }

    public void flush()
    {
        entries.clear();
    }

    public String getEntryName(TempModifier modifier)
    {
        for (TempModifier entry : getList())
        {
            if (entry.getClass() == modifier.getClass())
            {
                return entry.getID();
            }
        }
        return null;
    }

    public TempModifier getEntryFor(String id)
    {
        try {
            for (TempModifier entry : getList())
            {
                if (entry.getID().equals(id))
                    return entry;
            }
        }
        catch (Exception e) {
            return null;
        }
        return null;
    }
}
