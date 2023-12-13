package com.momosoftworks.coldsweat.api.event.common;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class GatherDefaultTempModifiersEvent extends Event
{
    private List<TempModifier> modifiers;
    private LivingEntity entity;
    private Temperature.Type type;

    public GatherDefaultTempModifiersEvent(LivingEntity entity, Temperature.Type type)
    {
        this.entity = entity;
        this.type = type;
        this.modifiers = new ArrayList<>(Temperature.getModifiers(entity, type));
    }

    public List<TempModifier> getModifiers()
    {   return modifiers;
    }

    public LivingEntity getEntity()
    {   return entity;
    }

    public Temperature.Type getType()
    {   return type;
    }

    public void addModifier(TempModifier modifier)
    {   modifiers.add(modifier);
    }

    public void addModifiers(List<TempModifier> modifiers)
    {   this.modifiers.addAll(modifiers);
    }

    public void addModifier(TempModifier modifier, boolean allowDupes, Temperature.Addition params)
    {
        // Add the TempModifier according to the addition params
        Predicate<TempModifier> predicate = params.getPredicate();
        if (predicate == null) predicate = mod -> true;

        boolean replace = params.getRelation()  == Temperature.Addition.Mode.REPLACE || params.getRelation() == Temperature.Addition.Mode.REPLACE_OR_ADD;
        boolean after   = params.getRelation()  == Temperature.Addition.Mode.AFTER;
        boolean forward = params.getOrder() == Temperature.Addition.Order.FIRST;

        if (!allowDupes && modifiers.stream().anyMatch(mod -> mod.getID().equals(modifier.getID())) && !replace)
        {   return;
        }

        // Get the start of the iterator & which direction it's going
        int start = forward ? 0 : (modifiers.size() - 1);

        // Iterate through the list (backwards if "forward" is false)
        for (int i = start; forward ? i < modifiers.size() : i >= 0; i += forward ? 1 : -1)
        {
            TempModifier mod = modifiers.get(i);

            // If the predicate is true, inject the modifier at this position (or after it if "after" is true)
            if (predicate.test(mod))
            {
                if (replace)
                {   modifiers.set(i, modifier);
                }
                else
                {   modifiers.add(i + (after ? 1 : 0), modifier);
                }
                return;
            }
        }

        // Add the modifier if the insertion check fails
        if (params.getRelation() != Temperature.Addition.Mode.REPLACE)
        {   modifiers.add(modifier);
        }
    }
}
