package dev.momosoftworks.coldsweat.api.event.core;

import dev.momosoftworks.coldsweat.common.entity.data.edible.ChameleonEdibles;
import dev.momosoftworks.coldsweat.common.entity.data.edible.Edible;
import net.minecraftforge.eventbus.api.Event;

public class EdiblesRegisterEvent extends Event
{
    public final void registerEdible(Edible edible)
    {
        ChameleonEdibles.addEdible(edible);
    }
}
