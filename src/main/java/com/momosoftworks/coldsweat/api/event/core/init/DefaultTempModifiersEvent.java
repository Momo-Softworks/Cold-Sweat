package com.momosoftworks.coldsweat.api.event.core.init;

import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

import java.util.*;
import java.util.function.Consumer;

/**
 * Determines the default {@link TempModifier}s that will be applied to an entity upon spawning.<br>
 * This event is fired EVERY TIME the entity joins the world, so make sure to properly check for duplicates.
 */
public class DefaultTempModifiersEvent extends Event
{
    private final Map<Temperature.Trait, List<TempModifier>> modifiers;
    private final LivingEntity entity;

    public DefaultTempModifiersEvent(LivingEntity entity)
    {
        this.entity = entity;
        this.modifiers = new HashMap<>(Temperature.getModifiers(entity));
    }

    public Map<Temperature.Trait, List<TempModifier>> getModifiers()
    {   return modifiers;
    }

    public List<TempModifier> getModifiers(Temperature.Trait trait)
    {   return modifiers.computeIfAbsent(trait, t -> new ArrayList<>());
    }

    public LivingEntity getEntity()
    {   return entity;
    }

    public void addModifier(Temperature.Trait trait, TempModifier modifier)
    {   this.getModifiers(trait).add(modifier);
    }

    public void addModifiers(Temperature.Trait trait, List<TempModifier> modifiers)
    {   this.getModifiers(trait).addAll(modifiers);
    }

    public void addModifier(Temperature.Trait trait, TempModifier modifier, Placement.Duplicates duplicatePolicy, Placement params)
    {   Temperature.addModifier(this.getModifiers(trait), modifier, duplicatePolicy, 1, params);
    }
    public void addModifier(List<Temperature.Trait> traits, TempModifier modifier, Placement.Duplicates duplicatePolicy, Placement params)
    {
        for (Temperature.Trait trait : traits)
        {   this.addModifier(trait, modifier, duplicatePolicy, params);
        }
    }

    public void addModifiers(Temperature.Trait trait, List<TempModifier> modifiers, Placement.Duplicates duplicatePolicy, Placement params)
    {
        for (int i = modifiers.size() - 1; i >= 0; i--)
        {   this.addModifier(trait, modifiers.get(i), duplicatePolicy, params);
        }
    }

    /**
     * Allows for adding a TempModifier by its registered ID.
     * @param id The ID of the TempModifier to add
     * @param modifierBuilder Called on the TempModifier when it is created, for additional processing
     */
    public void addModifierById(Temperature.Trait trait, ResourceLocation id, Consumer<TempModifier> modifierBuilder, Placement.Duplicates duplicatePolicy, Placement params)
    {
        Optional<TempModifier> mod = TempModifierRegistry.getValue(id);
        if (mod.isPresent())
        {   modifierBuilder.accept(mod.get());
            addModifier(trait, mod.get(), duplicatePolicy, params);
        }
    }

    public void addModifierById(List<Temperature.Trait> traits, ResourceLocation id, Consumer<TempModifier> modifierBuilder, Placement.Duplicates duplicatePolicy, Placement params)
    {
        Optional<TempModifier> mod = TempModifierRegistry.getValue(id);
        if (mod.isPresent())
        {   modifierBuilder.accept(mod.get());
            this.addModifier(traits, mod.get(), duplicatePolicy, params);
        }
    }

    public void removeModifiers(TempModifier modifier, Temperature.Trait trait, Placement.Duplicates matchPolicy)
    {   this.getModifiers(trait).removeIf(mod -> matchPolicy.check(mod, modifier));
    }
}
