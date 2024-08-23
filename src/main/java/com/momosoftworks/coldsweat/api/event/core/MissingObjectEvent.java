package com.momosoftworks.coldsweat.api.event.core;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public class MissingObjectEvent<T> extends Event
{
    private final ResourceLocation missingId;
    private ResourceLocation remappedId = null;

    public MissingObjectEvent(ResourceLocation missingId)
    {
        this.missingId = missingId;
    }

    public ResourceLocation getKey()
    {   return missingId;
    }

    public ResourceLocation getRemappedKey()
    {   return remappedId;
    }

    public void remap(ResourceLocation remappedId)
    {   this.remappedId = remappedId;
    }
}
