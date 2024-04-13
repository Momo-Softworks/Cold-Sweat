package com.momosoftworks.coldsweat.api.event.common;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class GatherDefaultTempModifiersEvent extends Event
{
    private List<TempModifier> modifiers;
    private LivingEntity entity;
    private Temperature.Trait trait;

    public GatherDefaultTempModifiersEvent(LivingEntity entity, Temperature.Trait trait)
    {
        this.entity = entity;
        this.trait = trait;
        this.modifiers = new ArrayList<>(Temperature.getModifiers(entity, trait));
    }

    public List<TempModifier> getModifiers()
    {   return modifiers;
    }

    public LivingEntity getEntity()
    {   return entity;
    }

    public Temperature.Trait getType()
    {   return trait;
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
        Predicate<TempModifier> predicate = params.predicate();
        if (predicate == null) predicate = mod -> true;

        boolean replace = params.mode()  == Temperature.Addition.Mode.REPLACE || params.mode() == Temperature.Addition.Mode.REPLACE_OR_ADD;
        boolean after   = params.mode()  == Temperature.Addition.Mode.AFTER;
        boolean forward = params.order() == Temperature.Addition.Order.FIRST;

        if (!allowDupes && !replace
        && modifiers.stream().anyMatch(mod -> mod.equals(modifier)))
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
        if (params.mode() != Temperature.Addition.Mode.REPLACE)
        {   modifiers.add(modifier);
        }
    }
}
