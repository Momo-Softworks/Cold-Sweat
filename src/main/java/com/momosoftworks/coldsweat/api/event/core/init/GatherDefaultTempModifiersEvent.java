package com.momosoftworks.coldsweat.api.event.core.init;

import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Use {@link DefaultTempModifiersEvent} instead, which is fired for all traits at once.
 * This event is deprecated and will be removed in a future version.
 */
@Deprecated(since = "2.4", forRemoval = true)
public class GatherDefaultTempModifiersEvent extends Event
{
    private final List<TempModifier> modifiers;
    private final LivingEntity entity;
    private final Temperature.Trait trait;

    public GatherDefaultTempModifiersEvent(LivingEntity entity, List<TempModifier> modifiers, Temperature.Trait trait)
    {
        this.entity = entity;
        this.trait = trait;
        this.modifiers = modifiers;
    }

    public List<TempModifier> getModifiers()
    {   return modifiers;
    }

    public LivingEntity getEntity()
    {   return entity;
    }

    public Temperature.Trait getTrait()
    {   return trait;
    }

    public void addModifier(TempModifier modifier)
    {   modifiers.add(modifier);
    }

    public void addModifiers(List<TempModifier> modifiers)
    {   this.modifiers.addAll(modifiers);
    }

    public void addModifier(TempModifier modifier, Matcher duplicatePolicy, Placement params)
    {   Temperature.addModifier(modifiers, modifier, params.limitDuplicates(duplicatePolicy, 1), null, null);
    }

    public void addModifiers(List<TempModifier> modifiers, Matcher duplicatePolicy, Placement placement)
    {
        for (int i = modifiers.size() - 1; i >= 0; i--)
        {   this.addModifier(modifiers.get(i), duplicatePolicy, placement);
        }
    }

    /**
     * Allows for adding a TempModifier by its registered ID.
     * @param id The ID of the TempModifier to add
     * @param modifierBuilder Called on the TempModifier when it is created for additional processing
     */
    public void addModifierById(ResourceLocation id, Consumer<TempModifier> modifierBuilder, Matcher duplicatePolicy, Placement placement)
    {
        Optional<TempModifier> mod = TempModifierRegistry.getValue(id);
        if (mod.isPresent())
        {   modifierBuilder.accept(mod.get());
            addModifier(mod.get(), duplicatePolicy, placement);
        }
    }

    public void removeModifiers(TempModifier modifier, Matcher matchPolicy)
    {   modifiers.removeIf(mod -> matchPolicy.check(mod, modifier));
    }
}
