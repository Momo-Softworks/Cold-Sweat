package com.momosoftworks.coldsweat.compat.kubejs.event;

import com.momosoftworks.coldsweat.api.event.core.init.DefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.compat.kubejs.KubeBindings;
import dev.latvian.kubejs.entity.LivingEntityEventJS;
import dev.latvian.kubejs.entity.LivingEntityJS;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DefaultModifiersEventJS extends LivingEntityEventJS
{
    private final DefaultTempModifiersEvent event;

    public DefaultModifiersEventJS(DefaultTempModifiersEvent event)
    {   this.event = event;
    }

    @Override
    public LivingEntityJS getEntity()
    {   return new LivingEntityJS(this.worldOf(this.event.getEntity()), this.event.getEntity());
    }

    public Map<Temperature.Trait, List<TempModifier>> getModifiers()
    {   return this.event.getModifiers();
    }

    public void addModifier(String trait, TempModifier modifier)
    {
        Temperature.Trait t = KubeBindings.getTrait(trait);
        event.getModifiers(t).add(modifier);
    }

    public void addModifiers(String trait, TempModifier... modifiers)
    {
        Temperature.Trait t = KubeBindings.getTrait(trait);
        event.getModifiers(t).addAll(Arrays.asList(modifiers));
    }

    public void addModifier(String trait, TempModifier modifier, String duplicatePolicy, Placement params)
    {
        Temperature.Trait t = KubeBindings.getTrait(trait);
        Temperature.addModifier(event.getModifiers(t), modifier, Placement.Duplicates.byName(duplicatePolicy), 1, params);
    }

    public void addModifierById(String trait, ResourceLocation id, Consumer<TempModifier> modifierBuilder, String duplicatePolicy, Placement params)
    {
        TempModifierRegistry.getValue(id).ifPresent(mod ->
        {
            modifierBuilder.accept(mod);
            addModifier(trait, mod, duplicatePolicy, params);
        });
    }

    public void removeModifiers(String trait, TempModifier modifier, String matchPolicy)
    {
        Temperature.Trait t = KubeBindings.getTrait(trait);
        Placement.Duplicates policy = Placement.Duplicates.byName(matchPolicy);
        event.getModifiers(t).removeIf(mod -> policy.check(mod, modifier));
    }

    public Placement placed(String mode, String order, Predicate<TempModifier> predicate)
    {   return Placement.of(Placement.Mode.byName(mode), Placement.Order.byName(order), predicate);
    }
}
