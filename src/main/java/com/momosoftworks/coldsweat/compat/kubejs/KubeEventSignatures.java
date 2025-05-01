package com.momosoftworks.coldsweat.compat.kubejs;

import com.momosoftworks.coldsweat.api.event.common.insulation.InsulateItemEvent;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TempModifierEvent;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TemperatureChangedEvent;
import com.momosoftworks.coldsweat.api.event.core.init.DefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.event.core.init.GatherDefaultTempModifiersEvent;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;
import me.shedaniel.architectury.event.EventResult;
import net.minecraft.util.registry.DynamicRegistries;

public interface KubeEventSignatures
{
    Event<KubeEventSignatures.Registries> REGISTRIES = EventFactory.createEventResult();
    Event<KubeEventSignatures.GatherModifiers> GATHER_MODIFIERS = EventFactory.createEventResult();
    Event<TemperatureChanged> TEMPERATURE_CHANGED = EventFactory.createEventResult();
    Event<KubeEventSignatures.InsulateItem> INSULATE_ITEM = EventFactory.createEventResult();
    Event<KubeEventSignatures.AddModifier> ADD_MODIFIER = EventFactory.createEventResult();

    interface Registries
    {   void buildRegistries(DynamicRegistries registryAccess);
    }
    interface GatherModifiers
    {   void gatherDefaultModifiers(DefaultTempModifiersEvent event);
    }
    interface TemperatureChanged
    {   EventResult onTemperatureChanged(TemperatureChangedEvent event);
    }
    interface InsulateItem
    {   EventResult insulateItem(InsulateItemEvent event);
    }
    interface AddModifier
    {   EventResult addModifier(TempModifierEvent.Add event);
    }
}
