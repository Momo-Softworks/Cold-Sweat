package com.momosoftworks.coldsweat.compat.kubejs.event;

import com.momosoftworks.coldsweat.api.event.core.init.DefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.event.core.init.GatherDefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.placement.Mode;
import com.momosoftworks.coldsweat.api.util.placement.Order;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.compat.kubejs.KubeBindings;
import dev.latvian.mods.kubejs.entity.LivingEntityEventJS;
import dev.latvian.mods.kubejs.entity.LivingEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

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
    {   return new LivingEntityJS(this.event.getEntity());
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
        event.getModifiers(t).addAll(List.of(modifiers));
    }

    public void addModifier(String trait, TempModifier modifier, Placement params)
    {
        Temperature.Trait t = KubeBindings.getTrait(trait);
        Temperature.addModifier(event.getModifiers(t), modifier, params, null, null);
    }

    public void addModifierById(String trait, ResourceLocation id, Consumer<TempModifier> modifierBuilder, Placement params)
    {
        TempModifierRegistry.getValue(id).ifPresent(mod ->
        {
            modifierBuilder.accept(mod);
            addModifier(trait, mod, params);
        });
    }

    public void removeModifiers(String trait, Predicate<TempModifier> predicate)
    {
        Temperature.Trait t = KubeBindings.getTrait(trait);
        event.getModifiers(t).removeIf(predicate);
    }

    public Placement placed(String mode, String order, Predicate<TempModifier> predicate)
    {   return Placement.of(Mode.byName(mode), Order.byName(order), predicate);
    }
}
