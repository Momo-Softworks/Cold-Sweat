package com.momosoftworks.coldsweat.api.event.core.registry;

import net.minecraft.core.RegistryAccess;
import net.neoforged.bus.api.Event;

public class FillOptionalHoldersEvent extends Event
{
    private final RegistryAccess registryAccess;

    public FillOptionalHoldersEvent(RegistryAccess registryAccess)
    {   this.registryAccess = registryAccess;
    }

    public RegistryAccess registryAccess()
    {   return registryAccess;
    }
}
