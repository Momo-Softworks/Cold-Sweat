package com.momosoftworks.coldsweat.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.util.exceptions.RegistryFailureException;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class TempModifierRegistry
{
    static volatile BiMap<ResourceLocation, TempModifierHolder> TEMP_MODIFIERS = ImmutableBiMap.of();

    public static BiMap<ResourceLocation, TempModifierHolder> getEntries()
    {   return TEMP_MODIFIERS;
    }

    /**
     * Adds a TempModifier to the current registry.<br>
     * Entries added this way are discarded by the next {@link #rebuild()}.
     *
     * @deprecated Register TempModifiers through {@link TempModifierRegisterEvent} instead.
     */
    @Deprecated(since = "2.4.5", forRemoval = true)
    public static synchronized void register(ResourceLocation id, Supplier<TempModifier> supplier)
    {
        TempModifierHolder holder = new TempModifierHolder(supplier, id);
        assertUnique(TEMP_MODIFIERS, holder);

        BiMap<ResourceLocation, TempModifierHolder> entries = HashBiMap.create(TEMP_MODIFIERS);
        entries.put(id, holder);
        TEMP_MODIFIERS = ImmutableBiMap.copyOf(entries);
    }

    /**
     * Throws if {@code entries} already has a TempModifier with the holder's ID or class.
     */
    public static void assertUnique(BiMap<ResourceLocation, TempModifierHolder> entries, TempModifierHolder holder)
    {
        TempModifierHolder existing = entries.containsKey(holder.getId())
                                      ? entries.get(holder.getId())
                                      : entries.values().stream().filter(holder::equals).findFirst().orElse(null);
        if (existing != null)
        {
            throw ColdSweat.LOGGER.throwing(new RegistryFailureException(holder.getId(), "TempModifier", String.format("Found duplicate TempModifier entries: %s (%s) %s (%s)", holder.getModifierClass().getName(), holder.getId(),
                                                                                       existing.getModifierClass().getName(), existing.getId()), null));
        }
    }

    /**
     * Fires {@link TempModifierRegisterEvent} and replaces the registry with its results all at once.<br>
     * The previous registry stays readable until then, and is kept if registration fails.
     */
    public static void rebuild()
    {
        TempModifierRegisterEvent event = new TempModifierRegisterEvent();
        try { NeoForge.EVENT_BUS.post(event); }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Registering TempModifiers failed!");
            throw e;
        }
        synchronized (TempModifierRegistry.class)
        {   TEMP_MODIFIERS = ImmutableBiMap.copyOf(event.getModifiers());
        }
    }

    /**
     * Clears the registry of all items. This effectively "un-registers" all TempModifiers.
     */
    public static synchronized void flush()
    {   TEMP_MODIFIERS = ImmutableBiMap.of();
    }

    /**
     * Returns a new instance of the TempModifier with the given ID.<br>
     * If a TempModifier with this ID is not in the registry, this method returns null and logs an error.<br>
     */
    public static Optional<TempModifier> getValue(ResourceLocation id)
    {
        return Optional.ofNullable(TEMP_MODIFIERS.get(id)).map(TempModifierHolder::get);
    }

    public static ResourceLocation getKey(TempModifier modifier)
    {   return CSMath.getIfNotNull(getHolder(modifier), TempModifierHolder::getId, null);
    }

    public static boolean containsKey(ResourceLocation id)
    {   return TEMP_MODIFIERS.containsKey(id);
    }

    @Nullable
    public static TempModifierHolder getHolder(TempModifier modifier)
    {
        for (TempModifierHolder holder : TEMP_MODIFIERS.values())
        {
            if (holder.getModifierClass() == modifier.getClass())
            {   return holder;
            }
        }
        return null;
    }

    public static class TempModifierHolder
    {
        private final Supplier<TempModifier> supplier;
        private final Class<? extends TempModifier> clazz;
        private final ResourceLocation id;

        public TempModifierHolder(Supplier<TempModifier> supplier, ResourceLocation id)
        {   this.supplier = supplier;
            this.clazz = supplier.get().getClass();
            this.id = id;
        }

        public TempModifier get()
        {   return supplier.get();
        }

        public Class<? extends TempModifier> getModifierClass()
        {   return clazz;
        }

        public ResourceLocation getId()
        {   return id;
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof TempModifierHolder holder && holder.clazz == clazz;
        }

        @Override
        public String toString()
        {   return "TempModifierHolder{" + clazz.getName() + "}";
        }
    }
}
