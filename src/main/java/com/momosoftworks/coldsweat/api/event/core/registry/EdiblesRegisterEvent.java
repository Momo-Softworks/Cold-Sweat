package com.momosoftworks.coldsweat.api.event.core.registry;

import com.momosoftworks.coldsweat.common.entity.data.edible.ChameleonEdibles;
import com.momosoftworks.coldsweat.common.entity.data.edible.Edible;
import net.minecraftforge.eventbus.api.Event;

public class EdiblesRegisterEvent extends Event
{
    public final void registerEdible(Edible edible)
    {
        ChameleonEdibles.addEdible(edible);
    }
}
