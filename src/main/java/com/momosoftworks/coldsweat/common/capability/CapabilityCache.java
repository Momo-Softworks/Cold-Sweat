package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.util.math.MappedCache;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CapabilityCache<C, K extends IAttachmentHolder> extends MappedCache<K, C>
{
    protected final Supplier<AttachmentType<C>> capability;

    public CapabilityCache(Supplier<AttachmentType<C>> capability, Predicate<K> invalidator)
    {
        super(e -> e.getData(capability), invalidator);
        this.capability = capability;
    }

    public CapabilityCache(Supplier<AttachmentType<C>> capability)
    {
        super(e -> e.getData(capability));
        this.capability = capability;
    }
}
