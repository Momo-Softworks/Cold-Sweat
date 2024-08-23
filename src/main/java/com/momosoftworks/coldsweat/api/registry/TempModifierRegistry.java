package com.momosoftworks.coldsweat.api.registry;

import com.google.common.collect.ImmutableMap;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.util.exceptions.RegistryFailureException;
import com.momosoftworks.coldsweat.util.math.FastMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class TempModifierRegistry
{
    static Map<ResourceLocation, TempModifierHolder> TEMP_MODIFIERS = new FastMap<>();

    public static ImmutableMap<ResourceLocation, TempModifierHolder> getEntries()
    {   return ImmutableMap.copyOf(TEMP_MODIFIERS);
    }

    public static void register(ResourceLocation id, Supplier<TempModifier> supplier)
    {
        if (TEMP_MODIFIERS.containsKey(id))
        {
            throw new RegistryFailureException(id, "TempModifier", String.format("Found duplicate TempModifier entries: %s (%s) %s (%s)", supplier.get().getClass().getName(), id,
                                                             TEMP_MODIFIERS.get(id).getClass().getName(), id), null);
        }
        TEMP_MODIFIERS.put(id, new TempModifierHolder(supplier));
    }

    /**
     * Clears the registry of all items. This effectively "un-registers" all TempModifiers.
     */
    public static void flush()
    {
        TEMP_MODIFIERS.clear();
    }

    /**
     * Returns a new instance of the TempModifier with the given ID.<br>
     * If a TempModifier with this ID is not in the registry, this method returns null and logs an error.<br>
     */
    public static Optional<TempModifier> getValue(ResourceLocation id)
    {
        return Optional.ofNullable(TEMP_MODIFIERS.get(id).get());
    }

    @Nullable
    public static ResourceLocation getKey(TempModifier modifier)
    {
        for (Map.Entry<ResourceLocation, TempModifierHolder> entry : TEMP_MODIFIERS.entrySet())
        {
            if (entry.getValue().equals(modifier))
            {   return entry.getKey();
            }
        }
        return null;
    }

    public static class TempModifierHolder
    {
        private final Supplier<TempModifier> supplier;

        public TempModifierHolder(Supplier<TempModifier> supplier)
        {   this.supplier = supplier;
        }

        public TempModifier get()
        {   return supplier.get();
        }

        @Override
        public boolean equals(Object obj)
        {
            return this.get().getClass() == obj.getClass()
            || obj instanceof TempModifierHolder && this.get().getClass() == ((TempModifierHolder) obj).get().getClass();
        }
    }
}
