package com.momosoftworks.coldsweat.api.event.core.registry;

import net.minecraft.core.RegistryAccess;
import net.minecraftforge.eventbus.api.Event;

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
